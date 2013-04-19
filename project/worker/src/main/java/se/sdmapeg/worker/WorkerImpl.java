package se.sdmapeg.worker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.common.tasks.TaskPerformer;
import se.sdmapeg.serverworker.communication.*;
import se.sdmapeg.serverworker.TaskId;

/**
 * Actual implementation of the Worker in the worker module.
 */
public class WorkerImpl implements Worker {
	private final ExecutorService serverListenerExecutor;
    private final TaskExecutor taskExecutor;
	private final Server server;
	private final TaskPerformer taskPerformer;
	private final int poolSize;
	private final Map<TaskId, FutureTask<Void>> taskMap =
		new ConcurrentHashMap<>();
	private final Map<Runnable, TaskId> idMap = new ConcurrentHashMap<>();
	private static final Logger LOG = LoggerFactory.getLogger(WorkerImpl.class);
	

	private WorkerImpl(int poolSize, Server server,
					  TaskPerformer taskPerformer) {
    	this.serverListenerExecutor = Executors.newSingleThreadExecutor();
		this.taskExecutor = TaskExecutor.newTaskQueue(poolSize);
		this.server = server;
		this.taskPerformer = taskPerformer;
		this.poolSize = poolSize;
	}

	private void listen() {
	    
		try {
		    while (true) {
			ServerToWorkerMessage message = server.receive();
			// Send a message handler to the accept method, and let the
			// message worry about calling the right method.
			message.accept(new MessageHandler());
		    }
		} catch (ConnectionClosedException ex) {
		    server.disconnect();
		    LOG.warn("Connection closed.");
		} catch (CommunicationException ex) {
			LOG.warn("Communication error.");
		    server.disconnect();
		}
	
	}

	private void cancelTask(TaskId taskId) {
		FutureTask<Void> futureTask = taskMap.remove(taskId);
		idMap.remove(futureTask);
		if(futureTask == null) {
			return;
		}
		futureTask.cancel(true);
	}

	private void runTask(TaskId taskId, Task<?> task) {
		FutureTask<Void> futureTask = new FutureTask<>(new TaskRunner(taskId,
				task), null);
		LOG.info("Queueing task {}", taskId);
		taskMap.put(taskId, futureTask);
		idMap.put(futureTask, taskId);
		taskExecutor.submit(futureTask);
	}

	private <R> Result<R> performTask(Task<R> task) {
		Result<R> result;
		try {
			result = task.perform(taskPerformer);
		} catch (ExecutionException ex) {
			result = new SimpleFailure<>(ex);
		}
		return result;
	}

	private void completeTask(TaskId taskId, Result<?> result) {
	    WorkerToServerMessage resultMessage = ResultMessage.newResultMessage(taskId, result);
	    FutureTask<Void> futureTask = taskMap.remove(taskId); 
	    idMap.remove(futureTask);
		try {
		server.send(resultMessage);
		LOG.info("Sending result for {}", taskId);
		} catch (CommunicationException ex) {
			server.disconnect();
			LOG.error("Disconnected from server before the result could be sent");
		}
	}

	private void stealTasks(int desired) {
		LOG.info("Attempting to steal {} tasks from queue", desired);
	    Set<Runnable> runnables = taskExecutor.stealTasks(desired);
	    Set<TaskId> stolenTasks = new HashSet<>();
	    for (Runnable runnable : runnables) {
			TaskId taskId = idMap.remove(runnable);
			if (taskId != null) {
				taskMap.remove(taskId);
			    stolenTasks.add(taskId);
			}
	    }
		LOG.info("Stole {} tasks from queue", stolenTasks.size());
	    WorkerToServerMessage message = 
	    		WorkStealingResponse.newWorkStealingResponse(stolenTasks);
	    try {
			LOG.info("Sending stolen tasks {} to server", stolenTasks);
			server.send(message);
		} catch (CommunicationException ex) {
			LOG.error("Connection to server lost");
			server.disconnect();
		}
	}
	
	public static WorkerImpl newWorkerImpl(int poolSize, Server server,
					  TaskPerformer taskPerformer){
	    return new WorkerImpl(poolSize, server, taskPerformer);
	}

	private final class MessageListener implements Runnable {
		@Override
		public void run() {
			listen();
		}
	}

	/* 
	 * This class handles messages received from the server. It should have one
	 * method for every concrete message type. Each method should typically be
	 * short, delegating work to other methods, and then return null at the end.
	 */
	private final class MessageHandler
			implements ServerToWorkerMessage.Handler<Void> {

		@Override
		public Void handle(TaskMessage message) {
			runTask(message.getTaskId(), message.getTask());
			return null;
		}

		@Override
		public Void handle(TaskCancellationMessage message) {
			cancelTask(message.getTaskId());
			return null;
		}

		@Override
		public Void handle(WorkStealingRequest message) {
			stealTasks(message.getDesired());
			return null;
		}
	}

	/**
	 * Class for running a task. 
	 */
	private final class TaskRunner implements Runnable {
		private final TaskId taskId;
		private final Task<?> task;

		public TaskRunner(TaskId taskId, Task<?> task) {
		    this.taskId = taskId;
		    this.task = task;
		}

		@Override
		public void run() {
			LOG.info("Performing task {}", taskId );
			Result<?> result = performTask(task);
			completeTask(taskId, result);
		}
	}

	@Override
	public void start() {
	    serverListenerExecutor.submit(new MessageListener());
		try {
			server.send(new WorkerIdentification(poolSize));
		} catch (CommunicationException ex) {
			server.disconnect();
			LOG.error("Connection to server lost");
		}
	}

	@Override
	public void stop() {
	    server.disconnect();
	    serverListenerExecutor.shutdown();
		taskExecutor.shutDown();
	    //TODO: Work out better soloution. 
	}
}
