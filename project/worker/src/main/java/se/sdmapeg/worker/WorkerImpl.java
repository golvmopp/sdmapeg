package se.sdmapeg.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.common.tasks.TaskPerformer;
import se.sdmapeg.serverworker.*;

/**
 * Actual implementation of the Worker in the worker module.
 */
public class WorkerImpl implements Worker {
	private final ExecutorService workerThreadPool;
	private final Server server;
	private final TaskPerformer taskPerformer;
	private final Map<TaskId, FutureTask<Void>> taskMap =
		new ConcurrentHashMap<>();

	public WorkerImpl(ExecutorService workerThreadPool, Server server,
					  TaskPerformer taskPerformer) {
		this.workerThreadPool = workerThreadPool;
		this.server = server;
		this.taskPerformer = taskPerformer;
	}

	public void run() {
		new TaskMessageListener().run();
	}

	private void cancelTask(TaskId taskId) {
		taskMap.get(taskId).cancel(true);
	}

	private void runTask(TaskId taskId, Task<?> task) {
		FutureTask<Void> futureTask = new FutureTask<>(new TaskRunner(taskId,
				task), null);
		taskMap.put(taskId, futureTask);
		workerThreadPool.submit(futureTask);
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
	    taskMap.remove(taskId); 
	}

	private final class TaskMessageListener implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					ServerToWorkerMessage message = server.receive();
					// Send a message handler to the accept method, and let the
					// message worry about calling the right method.
					message.accept(new MessageHandler());
				} catch (ConnectionClosedException ex) {
					break;
				} catch (CommunicationException ex) {
					try {
					    // Sleep for one second if no task is found. 
						Thread.sleep(1000);
					} catch (InterruptedException ex1) {
						break;
					}
				}
			}
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
}
