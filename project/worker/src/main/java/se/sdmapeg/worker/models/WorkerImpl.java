package se.sdmapeg.worker.models;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.common.tasks.TaskPerformer;
import se.sdmapeg.serverworker.TaskId;

/**
 * Actual implementation of the Worker in the worker module.
 */
public final class WorkerImpl implements Worker {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerImpl.class);
	private final Listeners listeners;
	private final ExecutorService listenerExecutor;
	private final ExecutorService serverListenerExecutor;
    private final TaskExecutor taskExecutor;
	private final Server server;
	private String host;
	private final TaskPerformer taskPerformer;
	private final int poolSize;
	private final Map<TaskId, FutureTask<Void>> futureTaskMap =
		new ConcurrentHashMap<>();
	private final Map<Runnable, TaskId> idMap = new ConcurrentHashMap<>();
	private final Map<TaskId, Task> taskMap = new ConcurrentHashMap<>(); // TODO: Zarth, check this. 
	

	private WorkerImpl(int poolSize, Server server, String host,
					  TaskPerformer taskPerformer) {
		this.listenerExecutor = Executors.newSingleThreadExecutor();
    	this.serverListenerExecutor = Executors.newSingleThreadExecutor();
		this.taskExecutor = TaskExecutor.newTaskQueue(poolSize);
		this.listeners = new Listeners(listenerExecutor);
		this.server = server;
		this.taskPerformer = taskPerformer;
		this.poolSize = poolSize;
		this.host = host;
	}
	
	@Override
	public String getTaskName(TaskId taskId){
		return taskMap.get(taskId).getName();
	}
	
	@Override
	public String getTypeName(TaskId taskId){
		return taskMap.get(taskId).getTypeName();
	}

	private void cancelTask(TaskId taskId) {
		FutureTask<Void> futureTask = futureTaskMap.remove(taskId);
		idMap.remove(futureTask);
		if(futureTask == null) {
			return;
		}
		futureTask.cancel(true);
		LOG.info("Cancelled task {}", taskId);
		listeners.taskCancelled(taskId);
	}

	private void performTask(TaskId taskId, Task<?> task) {
		FutureTask<Void> futureTask = new FutureTask<>(new TaskRunner(taskId,
				task), null);
		LOG.info("Queueing task {}", taskId);
		futureTaskMap.put(taskId, futureTask);
		idMap.put(futureTask, taskId);
		taskMap.put(taskId, task); 
		taskExecutor.submit(futureTask);
		listeners.taskAdded(taskId);
	}

	private <R> Result<R> runTask(Task<R> task) {
		Result<R> result = task.perform(taskPerformer);
		return result;
	}

	private void completeTask(TaskId taskId, Result<?> result) {
	    FutureTask<Void> futureTask = futureTaskMap.remove(taskId); 
	    idMap.remove(futureTask);
	    taskMap.remove(taskId); 
		server.taskCompleted(taskId, result);
		listeners.taskFinished(taskId);
	}

	private void stealTasks(int desired) {
		LOG.info("Attempting to steal {} tasks from queue", desired);
	    Set<Runnable> runnables = taskExecutor.stealTasks(desired);
	    Set<TaskId> stolenTasks = new HashSet<>();
	    for (Runnable runnable : runnables) {
			TaskId taskId = idMap.remove(runnable);
			if (taskId != null) {
				futureTaskMap.remove(taskId);
			    stolenTasks.add(taskId);
				listeners.taskStolen(taskId);
			}
	    }
		LOG.info("Stole {} tasks from queue", stolenTasks.size());
		server.tasksStolen(stolenTasks);
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
		listenerExecutor.shutdown();
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void addListener(WorkerListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(WorkerListener listener) {
		listeners.removeListener(listener);
	}
	
	
	public static WorkerImpl newWorkerImpl(int poolSize, Server server, String host,
					  TaskPerformer taskPerformer){
	    return new WorkerImpl(poolSize, server, host, taskPerformer);
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
			LOG.info("Performing task {}", taskId);
			listeners.taskStarted(taskId);
			Result<?> result = runTask(task);
			completeTask(taskId, result);
		}
		
	}

	private static final class Listeners
			implements WorkerListener, Listenable<WorkerListener> {
		private final ListenerSupport<WorkerListener> listenerSupport;

		public Listeners(Executor notificationExecutor) {
			listenerSupport = ListenerSupport.newListenerSupport(
					notificationExecutor);
		}

		@Override
		public void addListener(WorkerListener listener) {
			listenerSupport.addListener(listener);
		}

		@Override
		public void removeListener(WorkerListener listener) {
			listenerSupport.removeListener(listener);
		}

		@Override
		public void taskAdded(final TaskId taskId) {
			// This class would look so much nicer with lambda expressions :(
			listenerSupport.notifyListeners(new Notification<WorkerListener>() {
				@Override
				public void notifyListener(WorkerListener listener) {
					listener.taskAdded(taskId);
				}
			});
		}

		@Override
		public void taskStarted(final TaskId taskId) {
			listenerSupport.notifyListeners(new Notification<WorkerListener>() {
				@Override
				public void notifyListener(WorkerListener listener) {
					listener.taskStarted(taskId);
				}
			});
		}

		@Override
		public void taskFinished(final TaskId taskId) {
			listenerSupport.notifyListeners(new Notification<WorkerListener>() {
				@Override
				public void notifyListener(WorkerListener listener) {
					listener.taskFinished(taskId);
				}
			});
		}

		@Override
		public void taskCancelled(final TaskId taskId) {
			listenerSupport.notifyListeners(new Notification<WorkerListener>(){
				@Override
				public void notifyListener(WorkerListener listener) {
					listener.taskCancelled(taskId);
				}
			});
		}

		@Override
		public void taskStolen(final TaskId taskId) {
			listenerSupport.notifyListeners(new Notification<WorkerListener>() {
				@Override
				public void notifyListener(WorkerListener listener) {
					listener.taskStolen(taskId);
				}
			});
		}
	}
}
