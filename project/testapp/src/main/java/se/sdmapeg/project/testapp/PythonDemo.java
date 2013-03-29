package se.sdmapeg.project.testapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 *
 * @author niclas
 */
public class PythonDemo {
	static {
		System.setSecurityManager(JythonSandbox.getSecurityManager());
	}

	public static void runInterpreter() {
		final ExecutorService codeExecutorService =
							  Executors.newSingleThreadExecutor();
		final JFrame frame = new JFrame("Jython Interpreter") {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				super.dispose();
				codeExecutorService.shutdown();
			}
		};
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		Box mainContainer = new Box(BoxLayout.PAGE_AXIS);
		frame.add(mainContainer);
		final JTextArea textArea = new RSyntaxTextArea(
				new RSyntaxDocument(RSyntaxDocument.SYNTAX_STYLE_PYTHON));
		textArea.setFont(Font.decode("Monospaced"));
		frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
		Box box = new Box(BoxLayout.LINE_AXIS);
		final JLabel resultLabel = new JLabel("value = null");
		resultLabel.setFont(Font.decode("Monospaced-bold"));
		JScrollPane resultScrollPane = new JScrollPane(resultLabel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultScrollPane.setBorder(null);
		box.add(resultScrollPane);
		box.add(new Box.Filler(new Dimension(0, 35), new Dimension(0, 35),
							   new Dimension(0, 35)));
		JButton confirmButton = new JButton("Compile and run");
		box.add(confirmButton);
		frame.add(box, BorderLayout.SOUTH);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		frame.setSize(1024, 800);
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resultLabel.setText("Running...");
				codeExecutorService.execute(new Runnable() {
					@Override
					public void run() {
						PySystemState pySystemState = new PySystemState();
						pySystemState.setClassLoader(
								new RestrictedClassLoader());
						final PythonInterpreter pythonInterpreter =
							new PythonInterpreter(
								new PyDictionary(), pySystemState);
						pythonInterpreter.exec("from org.python.modules import"
								+ " math, cmath, itertools");
						final String script = textArea.getText();
						FutureTask<PyObject> task =
										   new FutureTask<>(new Callable<PyObject>() {
							@Override
							public PyObject call() {
								pythonInterpreter.exec(script);
								return pythonInterpreter.get(
										"value");
							}
						});
						Thread interpreterThread = new JythonSandbox(task,
								"Sandbox");
						interpreterThread.start();
						try {
							final String result = Objects.toString(task.get(1, TimeUnit.MINUTES), "None");
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									resultLabel.setText("value = " + result);
								}
							});
						}
						catch (TimeoutException | InterruptedException ex) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									resultLabel.setText(
											"Computation timed out.");
								}
							});
							// Try to cancel the task.
							task.cancel(true);
							try {
								// Give the task some time to terminate normally.
								interpreterThread.join(1000);
							}
							catch (InterruptedException interruptedException) {
								Thread.currentThread().interrupt();
							}
							finally {
								if (interpreterThread.isAlive()) {
									// All right, we gave you a fair chance...
									killUninterruptibleThread(interpreterThread);
								}
							}
						}
						catch (final ExecutionException ex) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									Throwable exception = ex;
									while (exception
											instanceof ExecutionException) {
										exception = exception.getCause();
									}
									resultLabel.setText("Error: "
														+ exception.toString());
								}
							});
							throw new AssertionError(ex);
						}
					}
				});
			}
		});
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
				}
				else {
					delegate.checkPermission(perm);
				}
			}

			@Override
			public void checkPermission(Permission perm, Object context) {
				if (isSandbox.get().booleanValue()) {
					checkPermission(perm);
				}
				else {
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