package se.sdmapeg.worker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
	private BlockingDeque<Runnable> queue;
	private ExecutorService workerThreadPool;

	private TaskExecutor(int poolSize) {
		this.queue = new LinkedBlockingDeque<>();
		this.workerThreadPool = new ThreadPoolExecutor(poolSize, poolSize, 0L,
			TimeUnit.MILLISECONDS, queue);		
		for (int i = 0; i < poolSize; i++) {
			workerThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							queue.take().run();
						}
						catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
				}
			});
		}
	}

	public void submit(Runnable task) {
		queue.add(task);
	}

	public Set<Runnable> stealTasks(int desired) {
		Set<Runnable> stolenTasks = new HashSet<>();
		for (int i = 0; i < desired; i++) {
			Runnable task = queue.pollLast();
			if (task == null) {
				break;
			}
			stolenTasks.add(task);
		}
		return stolenTasks;
	}

	public static TaskExecutor newTaskQueue(int poolSize) {
		return new TaskExecutor(poolSize);
	}

	void shutDown() {
		workerThreadPool.shutdownNow();
	}
}
