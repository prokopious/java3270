package j3270;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

public class TerminalCommander implements Closeable, AutoCloseable {

	private int scriptPort;
	public String statusMessage;

	private Socket socket = null;
	private Writer writer = null;
	private BufferedReader reader = null;

	public TerminalCommander(int scriptPort) {
		super();
		this.scriptPort = scriptPort;
	}

	public String execute(String command) {
		try {
			writer.write(command);
			writer.write("\n");
			writer.flush();
			return processData(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String processData(BufferedReader reader) throws IOException {
		
		StringBuilder buffer = new StringBuilder("");
		String line = reader.readLine();
		if (!line.startsWith("data:")) {
		} else {
			buffer.append(line.substring(6) + "\n");
		}
		line = reader.readLine();
		if (!line.startsWith("data:")) {
		} else {
			buffer.append(line.substring(6) + "\n");
			line = reader.readLine();
		} if (!line.startsWith("data:")) {
		} else {
			boolean go = true;
			while (go) {
				line = reader.readLine();
				if (!line.startsWith("data:")) {
					go = false;
				} else {
					buffer.append(line.substring(6) + "\n");
				}
			}
		}
		return buffer.toString();
	}

	public void connect() throws UnknownHostException, IOException, TimeoutException {
		try {
			waitForSocket();
		} catch (InterruptedException e) {
			throw new TimeoutException("Script socket connection timed out.");
		}
		writer = new OutputStreamWriter(socket.getOutputStream(), Charset.forName("ASCII"));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("ASCII")));
	}

	private void waitForSocket() throws InterruptedException, TimeoutException {
		int attempts = 0;
		while (attempts < 10) {
			try {
				socket = new Socket("[::1]", scriptPort);
				System.out.println();
				return;
			} catch (IOException e) {
				Thread.sleep(200);
				attempts++;
			}
		}
		throw new TimeoutException("Emulator socket connection timed out.");
	}

	public void disconnect() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				// Nothing to do.
			}
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// Nothing to do.
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// Nothing to do.
			}
		}

	}

	@Override
	public void close() throws IOException {
		disconnect();
	}

}