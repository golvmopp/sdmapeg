package se.sdmapeg.worker.taskperformers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Taskperformer for PythonTask.
 */
public final class PythonTaskPerformer {
	/**
	 * An output stream that ignores all data written to it.
	 */
	private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
		@Override
		public void write(int b) {
			// ignore
		}
	};
	/**
	 * An input stream that does not contain any data.
	 */
	private static final InputStream EMPTY_INPUT_STREAM = new InputStream() {
		@Override
		public int read() throws IOException {
			// signal that the end of the stream has been reached
			return -1;
		}
	};
	static {
		/*
		 * When this class is loaded, we set our own security manager in an
		 * attempt to prevent malicious client-provided scripts from doing any
		 * real damage. This is unlikely to be bulletproof, but will hopefully
		 * still be capable of catching some of the potential mischief.
		 */
		System.setSecurityManager(JythonSandbox.getSecurityManager());
	}

	private PythonTaskPerformer() {
		// prevent instantiation
		throw new AssertionError();
	}

	public static String execute(final String script) throws ExecutionException {
		PythonInterpreter pythonInterpreter = newInterpreter();
		FutureTask<PyObject> task = new FutureTask<>(
				new PythonRunner(pythonInterpreter, script));
		Thread interpreterThread = new JythonSandbox(task, "Sandbox");
		interpreterThread.start();
		return getResult(task, interpreterThread);
	}

	private static void cancelPythonTask(FutureTask<PyObject> task,
			Thread interpreterThread) {
		task.cancel(true);
		try {
			// Give the task some time to terminate normally.
			interpreterThread.join(1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			if (interpreterThread.isAlive()) {
				// Thread is not responding, we have no choice but to kill it
				killPythonThread(interpreterThread);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void killPythonThread(Thread thread) {
		thread.stop();
	}

	private static PythonInterpreter newInterpreter() {
		PySystemState pySystemState = new PySystemState();
		pySystemState.setClassLoader(new RestrictedClassLoader());
		PythonInterpreter pythonInterpreter = new PythonInterpreter(
				new PyDictionary(), pySystemState);
		disableIO(pythonInterpreter);
		importSupportedModules(pythonInterpreter);
		return pythonInterpreter;
	}

	private static void disableIO(PythonInterpreter pythonInterpreter) {
		pythonInterpreter.setIn(EMPTY_INPUT_STREAM);
		pythonInterpreter.setOut(NULL_OUTPUT_STREAM);
		pythonInterpreter.setErr(NULL_OUTPUT_STREAM);
	}

	private static void importSupportedModules(PythonInterpreter pythonInterpreter) {
		pythonInterpreter.exec("from org.python.modules import math, cmath,"
				+ " itertools");
	}

	private static String getResult(FutureTask<PyObject> task,
			Thread interpreterThread) throws ExecutionException {
		try {
			String result = Objects.toString(task.get(1, TimeUnit.MINUTES),
				"None");
			return result;
		} catch (TimeoutException | InterruptedException ex) {
			cancelPythonTask(task, interpreterThread);
			throw new ExecutionException(ex);
		}
	}

	private static class JythonSandbox extends Thread {
		{
			setDaemon(true);
		}

		public JythonSandbox(Runnable target, String name) {
			super(target, name);
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
			private static final Set<Permission> SANDBOX_PERMISSIONS =
				Collections.unmodifiableSet(new HashSet<Permission>(
					Arrays.asList(new ReflectPermission("suppressAccessChecks"),
								  new RuntimePermission("createClassLoader"),
								  new RuntimePermission("getProtectionDomain"))));
			private final SecurityManager delegate;

			public SandboxSecurityManager(SecurityManager delegate) {
				this.delegate = initializeDelegate(delegate);
			}

			private SecurityManager getDelegate() {
				return delegate;
			}

			@Override
			public void checkPermission(Permission perm) {
				if (isSandbox()) {
					if (SANDBOX_PERMISSIONS.contains(perm)) {
						return;
					}
					throw new SecurityException(perm.toString());
				} else {
					delegate.checkPermission(perm);
				}
			}

			@Override
			public void checkPermission(Permission perm, Object context) {
				if (isSandbox()) {
					checkPermission(perm);
				} else {
					delegate.checkPermission(perm, context);
				}
			}

			@Override
			public void checkMemberAccess(Class<?> clazz, int which) {
				if (isSandbox()) {
					String className = clazz.getCanonicalName();
					if (className.startsWith("java.lang.")) {
						return;
					}
					if (className.startsWith("org.python.")) {
						return;
					}
					throw new SecurityException(className);
				}
			}

			private boolean isSandbox() {
				return Thread.currentThread() instanceof JythonSandbox;
			}

			private static SecurityManager initializeDelegate(
					SecurityManager delegate) {
				if (delegate == null) {
					return new NullSecurityManager();
				} else if (delegate instanceof SandboxSecurityManager) {
					return ((SandboxSecurityManager) delegate).getDelegate();
				} else {
					return delegate;
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
				"org.python.modules.math",
				"org.python.modules.cmath",
				"org.python.modules.itertools")));

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			if (!whitelisted(name)) {
				throw new ClassNotFoundException();
			}
			return super.loadClass(name);
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
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

	private static class PythonRunner implements Callable<PyObject> {
		private final PythonInterpreter pythonInterpreter;
		private final String script;

		public PythonRunner(PythonInterpreter pythonInterpreter, String script) {
			this.pythonInterpreter = pythonInterpreter;
			this.script = script;
		}

		@Override
		public PyObject call() {
			pythonInterpreter.exec(script);
			return pythonInterpreter.get("result");
		}
	}
}
