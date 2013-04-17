package se.sdmapeg.worker.taskperformers;

import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import javax.swing.*;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.*;

/**
 * Taskperformer for PythonTask.
 */
public class PythonTaskPerformer {
	static {
		System.setSecurityManager(JythonSandbox.getSecurityManager());
	}

	public static String execute(final String script) throws ExecutionException {
		PySystemState pySystemState = new PySystemState();
		pySystemState.setClassLoader(
				new RestrictedClassLoader());
		final PythonInterpreter pythonInterpreter =
				new PythonInterpreter(
						new PyDictionary(), pySystemState);
		pythonInterpreter.exec("from org.python.modules import"
				                       + " math, cmath, itertools");;
		FutureTask<PyObject> task =
				new FutureTask<>(new Callable<PyObject>() {
					@Override
					public PyObject call() {
						pythonInterpreter.exec(script);
						return pythonInterpreter.get("result");
					}
				});
		Thread interpreterThread = new JythonSandbox(task,
		                                             "Sandbox");
		interpreterThread.start();
		try {
			String result = Objects.toString(task.get(1, TimeUnit.MINUTES), "None");
			return result;
		} catch (TimeoutException | InterruptedException ex) {
			// Try to cancel the task.
			task.cancel(true);
			try {
				// Give the task some time to terminate normally.
				interpreterThread.join(1000);
			} catch (InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
			} finally {
				if (interpreterThread.isAlive()) {
					// All right, we gave you a fair chance...
					killUninterruptibleThread(interpreterThread);
				}
			}
			throw new ExecutionException(ex);
		}
	}

	private static class JythonSandbox extends Thread {
		{
			setDaemon(true);
		}

		public JythonSandbox() {
		}

		public JythonSandbox(Runnable target) {
			super(target);
		}

		public JythonSandbox(String name) {
			super(name);
		}

		public JythonSandbox(Runnable target, String name) {
			super(target, name);
		}

		public JythonSandbox(ThreadGroup group, Runnable target) {
			super(group, target);
		}

		public JythonSandbox(ThreadGroup group, String name) {
			super(group, name);
		}

		public JythonSandbox(ThreadGroup group, Runnable target, String name) {
			super(group, target, name);
		}

		public JythonSandbox(ThreadGroup group, Runnable target, String name,
		                     long stackSize) {
			super(group, target, name, stackSize);
		}

		public static SecurityManager getSecurityManager() {
			return new SandboxSecurityManager(System.getSecurityManager());
		}

		/**
		 * A security manages that restricts the permissions of JythonSandbox
		 * threads to the absolute minimum, while letting another security
		 * manager handle the permissions of all other threads.
		 */
		private static class SandboxSecurityManager extends SecurityManager {
			private final SecurityManager delegate;
			private static final ThreadLocal<Boolean> isSandbox =
					new InheritableThreadLocal<Boolean>() {
						@Override
						protected Boolean initialValue() {
							return (Thread.currentThread() instanceof JythonSandbox);
						}

						@Override
						public Boolean get() {
							return (Thread.currentThread() instanceof JythonSandbox)
									|| super.get();
						}

						@Override
						protected Boolean childValue(Boolean parentValue) {
							if (Thread.currentThread() instanceof JythonSandbox) {
								throw new SecurityException("Sandboxes must not create"
										                            + " any child threads.");
							}
							return parentValue;
						}
					};

			public SandboxSecurityManager(SecurityManager delegate) {
				this.delegate = (delegate != null)
				                ? ((delegate instanceof SandboxSecurityManager)
				                   ? ((SandboxSecurityManager) delegate)
						                   .getDelegate()
				                   : delegate)
				                : new NullSecurityManager();
			}

			private SecurityManager getDelegate() {
				return delegate;
			}

			@Override
			public void checkPermission(Permission perm) {
				if (isSandbox.get().booleanValue()) {
					if (perm instanceof ReflectPermission
							&& perm.getName().equals("suppressAccessChecks")) {
						return;
					}
					if (perm instanceof RuntimePermission
							&& perm.getName().matches(
							"createClassLoader|getProtectionDomain")) {
						return;
					}
					throw new SecurityException(perm.toString());
				} else {
					delegate.checkPermission(perm);
				}
			}

			@Override
			public void checkPermission(Permission perm, Object context) {
				if (isSandbox.get().booleanValue()) {
					checkPermission(perm);
				} else {
					delegate.checkPermission(perm, context);
				}
			}

			@Override
			public void checkMemberAccess(Class<?> clazz, int which) {
				if (isSandbox.get().booleanValue()) {
					if (clazz.getCanonicalName().matches("java\\.lang\\..+")) {
						return;
					}
					if (clazz.getCanonicalName().matches("org\\.python\\..+")) {
						return;
					}
					throw new SecurityException(clazz.getCanonicalName());
				}
			}

			/**
			 * Security manager that allows access to everything.
			 */
			private static class NullSecurityManager extends SecurityManager {

				@Override
				public void checkPermission(Permission perm) {
				}

				@Override
				public void checkPermission(Permission perm, Object context) {
				}
			}
		}
	}

	private static class RestrictedClassLoader extends ClassLoader {
		private static final Set<String> WHITELIST =
				Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
				/* org.python.modules */
				"org.python.modules.math",
				"org.python.modules.cmath",
				"org.python.modules.itertools"
				                                                       )));

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			if (!whitelisted(name)) {
				throw new ClassNotFoundException();
			}
			return super.loadClass(name);
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if (!whitelisted(name)) {
				throw new ClassNotFoundException();
			}
			return super.loadClass(name, resolve);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			if (!whitelisted(name)) {
				throw new ClassNotFoundException();
			}
			return super.findClass(name);
		}

		private static boolean whitelisted(String name) {
			return WHITELIST.contains(name);
		}
	}

	@SuppressWarnings("deprecation")
	private static void killUninterruptibleThread(Thread thread) {
		thread.stop();
	}
}
