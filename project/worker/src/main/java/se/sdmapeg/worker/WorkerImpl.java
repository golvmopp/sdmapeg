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

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.common.tasks.TaskPerformer;
import se.sdmapeg.serverworker.TaskId;

/**
 * Actual implementation of the Worker in the worker module.
 */
public final class WorkerImpl implements Worker {
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
	    server.listen(null);
	}

	private void cancelTask(TaskId taskId) {
		FutureTask<Void> futureTask = taskMap.remove(taskId);
		idMap.remove(futureTask);
		if(futureTask == null) {
			return;
		}
		futureTask.cancel(true);
		LOG.info("Cancelled task {}", taskId);
	}

	private void performTask(TaskId taskId, Task<?> task) {
		FutureTask<Void> futureTask = new FutureTask<>(new TaskRunner(taskId,
				task), null);
		LOG.info("Queueing task {}", taskId);
		taskMap.put(taskId, futureTask);
		idMap.put(futureTask, taskId);
		taskExecutor.submit(futureTask);
	}

	private <R> Result<R> runTask(Task<R> task) {
		Result<R> result = task.perform(taskPerformer);
		return result;
	}

	private void completeTask(TaskId taskId, Result<?> result) {
	    FutureTask<Void> futureTask = taskMap.remove(taskId); 
	    idMap.remove(futureTask);
		server.taskCompleted(taskId, result);
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
		server.tasksStolen(stolenTasks);
	}
	
	public static WorkerImpl newWorkerImpl(int poolSize, Server server,
					  TaskPerformer taskPerformer){
	    return new WorkerImpl(poolSize, server, taskPerformer);
	}

	private final class MessageListener implements Runnable {
		@Override
		public void run() {
			try {
				server.listen(new ServerEventCallback());
			} catch (Exception ex) {
				LOG.error("An uncaught exception was encountered", ex);
			}
		}
	}

	private final class ServerEventCallback implements ServerCallback {
		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			performTask(taskId, task);
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			cancelTask(taskId);
		}

		@Override
		public void workStealingRequested(int desired) {
			stealTasks(desired);
		}

		@Override
		public void connectionClosed() {
			stop();
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
			Result<?> result = runTask(task);
			completeTask(taskId, result);
		}
	}

	@Override
	public void start() {
	    serverListenerExecutor.submit(new MessageListener());
		server.identify(poolSize);
	}

	@Override
	public void stop() {
	    server.disconnect();
	    serverListenerExecutor.shutdown();
		taskExecutor.shutDown();
	}
}
