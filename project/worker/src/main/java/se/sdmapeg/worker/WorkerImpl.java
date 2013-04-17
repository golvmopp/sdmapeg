package se.sdmapeg.worker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.common.tasks.TaskPerformer;
import se.sdmapeg.serverworker.communication.ResultMessage;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.TaskMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 * Actual implementation of the Worker in the worker module.
 */
public class WorkerImpl implements Worker {
	private final ExecutorService serverListenerExecutor;
    	private final TaskExecutor taskExecutor;
	private final Server server;
	private final TaskPerformer taskPerformer;
	private final Map<TaskId, FutureTask<Void>> taskMap =
		new ConcurrentHashMap<>();
	private final Map<Runnable, TaskId> idMap = new ConcurrentHashMap<>();
	

	private WorkerImpl(int poolSize, Server server,
					  TaskPerformer taskPerformer) {
	    	this.serverListenerExecutor = Executors.newSingleThreadExecutor();
		this.taskExecutor = TaskExecutor.newTaskQueue(poolSize);
		this.server = server;
		this.taskPerformer = taskPerformer;
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
		    //TODO: Log it!
		} catch (CommunicationException ex) {
		    //TODO: Log it!
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
		taskMap.put(taskId, futureTask);
		idMap.put(futureTask, taskId);
		taskExecutor.submit(futureTask);
	}	//TODO: Make an adapter for task -> runnable

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
	}

	private void stealTasks(int desired) {
	    Set<Runnable> runnables = taskExecutor.stealTasks(desired);
	    Set<TaskId> stolenTasks = new HashSet<>();
	    for (Runnable runnable : runnables) {
		TaskId taskId = idMap.remove(runnable);
		taskMap.remove(taskId);
		if (taskId != null) {
		    stolenTasks.add(taskId);
		}
	    }
	    // TODO: Send stolen tasks to server
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
			Result<?> result = performTask(task);
			completeTask(taskId, result);
		}
	}

	@Override
	public void start() {
	    serverListenerExecutor.submit(new MessageListener());
	}

	@Override
	public void stop() {
	    server.disconnect();
	    serverListenerExecutor.shutdown();
		taskExecutor.shutDown();
	    //TODO: Work out better soloution. 
	}
}
