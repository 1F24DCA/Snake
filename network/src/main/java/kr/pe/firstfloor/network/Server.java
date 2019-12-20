
package kr.pe.firstfloor.network;

import java.util.*;
import java.net.*;

import kr.pe.firstfloor.annotation.Constant;
import kr.pe.firstfloor.network.data.Command;
import kr.pe.firstfloor.network.data.Converter;
import kr.pe.firstfloor.network.data.Data;
import kr.pe.firstfloor.network.data.LoginRequest;
import kr.pe.firstfloor.network.data.LoginResponse;
import kr.pe.firstfloor.network.data.User;
import kr.pe.firstfloor.util.StackTrace;

public class Server implements Endpoint {
	@Constant
    static final int SIGNAL_LOGIN_ACCEPT = 1;
	@Constant
    static final int SIGNAL_KICK = 2;
	@Constant
    static final int SIGNAL_CLOSE = 3;

	@Constant
    public static final int COMMAND_DATA = 4;


	private int maxAccepts;

	private Map<User, IOHandler> handlers = Collections.synchronizedMap(new HashMap<User, IOHandler>());

	private List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<ConnectionListener>());
	private List<CommandListener> commandListeners = Collections.synchronizedList(new ArrayList<CommandListener>());

	
	public Server(int port, int maxAccepts) {
		this.maxAccepts = maxAccepts;

		Data.register(this);

		new ConnectionThread(port);
	}


	public interface ConnectionListener {
		void onConnected(User user, boolean isReconnected);
		void onDisconnected(User user, String reason);
	}
	
	public void addConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.add(connectionListener);
	}


	public interface CommandListener { void onCommandReceived(User user, Command command); }
	
	public void addCommandListener(CommandListener listener) {
		commandListeners.add(listener);
	}
	
	public void sendCommand(User user, Command command) {
		if (handlers.get(user) != null)
			handlers.get(user).sendCommand(command);
	}

	public void sendCommand(ArrayList<User> users, Command command) {
		for (User user : users)
			sendCommand(user, command);
	}
	
	public void broadcastCommand(Command command) {
		Set<User> users = Collections.unmodifiableSet(this.handlers.keySet());
		for (User user : users)
			sendCommand(user, command);
	}


	public void disconnect(IOHandler handler, User user, String reason) {
		if (handler != null) {
			handler.sendSignal(Server.SIGNAL_KICK, reason);
			handler.terminate(true);
		}

		onDisconnected(user, reason);
	}
	

	private class ConnectionThread extends Thread {
		private int port;
		
		
		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(port);

				while (true) {
					final IOHandler handler = new IOHandler(serverSocket.accept());

					handler.setSignalListener(new IOHandler.SignalListener() {
						@Override
						public void onSignalReceived(int signal, String option) {
							switch (signal) {
								case Client.SIGNAL_LOGIN_REQUEST:
									LoginRequest loginRequest = (LoginRequest) Converter.toObject(option); // AuthorizeHelper.login(option); // option -> id
									String connectedUserId = loginRequest.getUserId();

									try {
										boolean isReconnected = User.findByUserId(connectedUserId) != null;

										final User user = isReconnected
												? User.findByUserId(connectedUserId)
												: (User) Data.newInstance(User.class);

										user.setUserId(connectedUserId);

										if (handlers.size() < maxAccepts) {
											if (handlers.get(user) == null) {
												handlers.put(user, handler);

												handler.sendSignal(Server.SIGNAL_LOGIN_ACCEPT, Converter.toString(user));

												handler.setCommandListener(new IOHandler.CommandListener() {
													@Override
													public void onCommandReceived(Command command) {
														Server.this.onCommandReceived(user, command);
													}
												});

												Server.this.onConnected(user, isReconnected);
											} else {
												Server.this.disconnect(handler, user, "User already login");
											}
										} else {
											Server.this.disconnect(handler, user, "Server is full");
										}
									} catch (Exception exception) {
										StackTrace.print(this, exception);

										Server.this.disconnect(handler, null, "Invalid user");
									}

									break;
								case Client.SIGNAL_DISCONNECT:
									User user = null;
									for (User found : handlers.keySet())
										if (handlers.get(found).equals(handler))
											user = found;

									handler.terminate(true);
									if (handlers.get(user) != null)
										handlers.remove(user);

									Server.this.onDisconnected(user, option);

									break;
							}
						}

						@Override
						public void onTerminated(boolean isDisconnect) {}
					});
				}
			} catch (Exception exception) {
				StackTrace.print(this, exception);
			}
		}
		
		ConnectionThread(int port) {
			this.port = port;
			
			this.start();
			this.setPriority(Thread.MIN_PRIORITY);
		}
	}
	
	private void onConnected(User user, boolean isReconnected) {
		List<ConnectionListener> connectionListeners = Collections.unmodifiableList(this.connectionListeners);
		for (ConnectionListener connectionListener : connectionListeners)
			connectionListener.onConnected(user, isReconnected);
	}

	private void onCommandReceived(User user, Command command) {
		List<CommandListener> commandListeners = Collections.unmodifiableList(this.commandListeners);
		for (CommandListener commandListener : commandListeners)
			commandListener.onCommandReceived(user, command);
	}
	
	private void onDisconnected(User user, String reason) {
		List<ConnectionListener> connectionListeners = Collections.unmodifiableList(this.connectionListeners);
		for (ConnectionListener connectionListener : connectionListeners)
			connectionListener.onDisconnected(user, reason);
	}
}