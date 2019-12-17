package kr.pe.firstfloor.network;

import java.io.*;
import java.net.*;

import kr.pe.firstfloor.network.data.Command;
import kr.pe.firstfloor.util.StackTrace;

public class IOHandler extends Thread {
	private final Socket socket;
	private final BufferedReader input;
	private final BufferedWriter output;

	private boolean disconnect = false;
	
	private SignalListener signalListener;
	private CommandListener commandListener;


	IOHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		this.setPriority(Thread.MAX_PRIORITY);
		this.start();
	}
	
	
	public interface CommandListener { void onCommandReceived(Command command); }
	
	public void setCommandListener(CommandListener commandListener) { this.commandListener = commandListener; }
	
	synchronized public void sendCommand(Command command) {
		if (command.isSignal()) return;
		
		String packet = command.toString();
		synchronized(socket) {
			try {
				output.write(packet);
				output.newLine();
				output.flush();
			} catch (IOException exception) {
				onDisconnected();
			} catch (Exception exception) {
				StackTrace.print(this, exception);
			}
		}
	}
	
	
	interface SignalListener {
		void onSignalReceived(int signal, String option);
		void onTerminated(boolean isDisconnect);
	}
	
	void setSignalListener(SignalListener signalListener) {
		this.signalListener = signalListener;
	}
	
	synchronized void sendSignal(int signal) { sendSignal(signal, null); }
	synchronized void sendSignal(int signal, String option) {
		Command command = new Command(signal, true);
		command.put(option);

		String packet = command.toString();
		synchronized(socket) {
			try {
				output.write(packet);
				output.newLine();
				output.flush();
			} catch (IOException exception) {
				onDisconnected();
			} catch (Exception exception) {
				StackTrace.print(this, exception);
			}
		}
	}

	synchronized void terminate() { terminate(true); }
	synchronized void terminate(boolean isDisconnect) {
		disconnect = isDisconnect;

		try {
			input.close();
			output.close();
			
			socket.close();
		} catch (Exception exception) {
			StackTrace.print(this, exception);
		}
	}
	
	
	@Override
	public void run() {
		try {
			String packet = "";
			while (true) {
				Command command = new Command();

				int characterInteger = input.read();
				if (characterInteger == -1)
					throw new IOException("End of stream");

				char character = (char)characterInteger;
				if (character!='\r'&&character!='\n')
					packet = packet+character;
				else
					command = Command.parse(packet);

				if (!command.isEmpty()) {
					if (command.isSignal()) {
						onSignalReceived(command.get(), command.getString(2, true));
					} else {
						onCommandReceived(command);
					}

					packet = "";
				}
			}
		} catch (IOException exception) {
			onDisconnected();
		} catch (Exception exception) {
			StackTrace.print(this, exception);
		}
	}
	
	private void onSignalReceived(int signal, String option) {
		if (signalListener != null)
			signalListener.onSignalReceived(signal, option);
	}
	
	private void onCommandReceived(Command command) {
		if (commandListener != null)
			commandListener.onCommandReceived(command);
	}

	private void onDisconnected() {
		signalListener.onTerminated(disconnect);

		terminate();
	}
}