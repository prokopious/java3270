package j3270;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.testng.Assert;

public class Emulator implements Closeable, AutoCloseable {

	private ExecutorService executorService;
	private boolean ownsExecutor = false;

	private Emulator3270Runner runner;
	private TerminalCommander commander;

	public Emulator(int scriptPort) {
		super();
		this.executorService = Executors.newFixedThreadPool(1);
		this.runner = new Emulator3270Runner(scriptPort);
		this.commander = new TerminalCommander(scriptPort);
		ownsExecutor = true;
	}

	public void start() throws UnknownHostException, IOException, TimeoutException {

		this.executorService.submit(runner);
		try {
			waitForEmulator();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		this.commander.connect();
	}

	private void waitForEmulator() throws InterruptedException, TimeoutException {
		int attempts = 0;
		while (!runner.isStarted()) {
			Thread.sleep(50);
			attempts++;

			if (attempts > 10) {
				throw new TimeoutException("Emulator start timed out.");
			}
		}
	}

	@Override
	public void close() {
		if (runner != null) {
			runner.stop();
		
		}
		if (commander != null) {
			try {
				commander.close();
			} catch (IOException e) {
				// Nothing to do.
			}
		}
		if (ownsExecutor) {
			executorService.shutdown();
		
		}
	}
	
	public void endSession() {
		exec("Quit()");
	}

	public void connect(String host) {
		exec("Connect(" + host + ")");
	}

	public String getScreen() {
		return exec("PrintText(string)");
	}

	public void printScreen() {
		String screenText = getScreen();
		System.out.println("================================================================================");
		System.out.println("\n");
		System.out.println(screenText);
		System.out.println("================================================================================");
	}
	
	public void moveTo(int row, int col) {
		row -= 1;
		col -= 1;
		exec("MoveCursor(" + row  + ", " + col + ")");
	}

	public void trySendTextToField(String text, int row, int col) {
		row -= 1;
		col -= 1;
		exec("MoveCursor(" + row  + ", " + col + ")");
		exec("String(\"" + text + "\")");
		awaitTextAtPosition(text, row += 1, col += 1, 60);
	}

	public void sendEnter() {
		exec("Enter");
	}

	public void sendPF(int F_key) {
		exec("PF(" + F_key + ")");
	}

	public void sendF(int F_key) {
		if (F_key < 13) {
			exec("PA(1)");
			exec("PF(" + F_key + ")");
		} else {
			exec("PA(2)");
			exec("PF(" + F_key + ")");
		}
	}

	public void pageUp() {
		exec("PF(7)");
	}

	public void pageDown() {
		exec("PF(8)");
	}

	public String readTextAtPosition(int row, int col, int length) {
		row -= 1;
		col -= 1;
		return exec("Ascii(" + row + ", " + col + ", " + length + ")");
	}

	public void awaitTextAtPosition(String expectedText, int row, int col, int timeoutSeconds) {

		Supplier<String> getActualText = () -> readTextAtPosition(row, col, expectedText.length());
		MethodPoller<String> poller = new MethodPoller<>();
		String actualText = null;
		try {
			actualText = poller.poll(timeoutSeconds, 200).method(getActualText).untilCondition(n -> n.contains(expectedText))
					.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(actualText.contains(expectedText), "Text was not found at specified location.");
	}

	public void awaitTextOnScreen(String expectedTextOnScreen, int timeoutSeconds) {
		Supplier<String> getScreenText = () -> getScreen();
		MethodPoller<String> poller = new MethodPoller<>();
		String actualScreenText = null;
		try {
			actualScreenText = poller.poll(timeoutSeconds, 100).method(getScreenText)
					.untilCondition(n -> n.contains(expectedTextOnScreen)).execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(actualScreenText.contains(expectedTextOnScreen), "Text was not found on screen.");
	}

	public String exec(String command) {
		if (this.commander == null) {
			throw new IllegalStateException("Emulator not started.");
		}
		return this.commander.execute(command);
	}
}