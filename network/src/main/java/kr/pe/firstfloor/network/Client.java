
package kr.pe.firstfloor.network;

import java.util.*;
import java.net.*;

import kr.pe.firstfloor.annotation.Constant;
import kr.pe.firstfloor.network.data.Command;
import kr.pe.firstfloor.network.data.Converter;
import kr.pe.firstfloor.network.data.Data;
import kr.pe.firstfloor.network.data.LoginRequest;
import kr.pe.firstfloor.network.data.User;
import kr.pe.firstfloor.util.StackTrace;

public class Client implements Endpoint {
	@Constant
    static final int SIGNAL_LOGIN_REQUEST = 5;
	@Constant
    static final int SIGNAL_DISCONNECT = 6;


	private IOHandler handler;

	private List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<ConnectionListener>());
	private List<CommandListener> commandListeners = Collections.synchronizedList(new ArrayList<CommandListener>());

	
	public Client(LoginRequest request, String address, int port) {
		try {
			handler = new IOHandler(new Socket(address, port));

			Data.register(this);

			handler.sendSignal(Client.SIGNAL_LOGIN_REQUEST, Converter.toString(request));
			handler.setSignalListener(new IOHandler.SignalListener() {
				@Override
				public void onSignalReceived(int signal, String option) {
					switch (signal) {
						case Server.SIGNAL_LOGIN_ACCEPT:
							Client.this.onConnected((User) Converter.toObject(option));

							break;
						case Server.SIGNAL_KICK:
						case Server.SIGNAL_CLOSE:
							Client.this.onDisconnected(option);

							break;
					}
				}

				@Override
				public void onTerminated(boolean isDisconnect) {
					if (!isDisconnect)
						Client.this.onDisconnected("Connection lost");
				}
			});
			handler.setCommandListener(new IOHandler.CommandListener() {
				@Override
				public void onCommandReceived(Command command) {
					Client.this.onCommandReceived(command);
				}
			});
		} catch (Exception exception) {
			StackTrace.print(this, exception);
		}
	}


	public interface ConnectionListener {
		void onConnected(User user);
		void onDisconnected(String reason);
	}

	public void addConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.add(connectionListener);
	}


	public interface CommandListener { void onCommandReceived(Command command); }
	
	public void addCommandListener(CommandListener listener) {
		commandListeners.add(listener);
	}
	
	public void sendCommand(Command command) {
		if (handler != null)
			handler.sendCommand(command);
	}

	public void disconnect() { disconnect(true); }
	public void disconnect(boolean isDisconnect) {
		if (handler != null) {
			if (isDisconnect) {
				handler.sendSignal(Client.SIGNAL_DISCONNECT, "Disconnected by user");

				onDisconnected("Disconnected by user");
			}

			handler.terminate(isDisconnect);
		}
	}
	
	
	private void onConnected(User user) {
		List<ConnectionListener> connectionListeners = Collections.unmodifiableList(this.connectionListeners);
		for (ConnectionListener connectionListener : connectionListeners)
			connectionListener.onConnected(user);
	}

	private void onCommandReceived(Command command) {
		List<CommandListener> commandListeners = Collections.unmodifiableList(this.commandListeners);
		for (CommandListener commandListener : commandListeners)
			commandListener.onCommandReceived(command);
	}
	
	private void onDisconnected(String reason) {
		List<ConnectionListener> connectionListeners = Collections.unmodifiableList(this.connectionListeners);
		for (ConnectionListener connectionListener : connectionListeners)
			connectionListener.onDisconnected(reason);
	}
}