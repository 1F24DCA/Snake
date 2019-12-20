
package kr.pe.firstfloor.snake.data;

import java.util.ArrayList;
import java.util.Arrays;

import kr.pe.firstfloor.annotation.Constant;
import kr.pe.firstfloor.network.*;
import kr.pe.firstfloor.network.data.*;
import kr.pe.firstfloor.util.StackTrace;

public class Room extends Data implements Comparable<Room> {
	@Constant
    public static final int REQUEST_ROOM_LIST = 9;
	@Constant
    public static final int RESPONSE_ROOM_LIST = 10;
	@Constant
    public static final int REQUEST_ROOM_CREATE = 11;
	@Constant
    public static final int REQUEST_ROOM_JOIN = 12;
	@Constant
    public static final int REQUEST_ROOM_LEAVE = 13;

	// Member variables
	private int roomId;
	private ArrayList<String> listUserId = new ArrayList<>();
	private String name;
	private int maxUsers;


	static {
		addEndpointRegisterListener(new EndpointRegisterListener() {
			@Override
			public void onEndpointRegistered(final Endpoint endpoint) {
				if (endpoint instanceof Server) {
					((Server) endpoint).addCommandListener(new Server.CommandListener() {
						@Override
						public void onCommandReceived(User user, Command command) {
							switch(command.get()) {
								case REQUEST_ROOM_LIST:
									try {
										int page = command.getInt(1) - 1;

										Room[] rooms = new Room[10];
										System.arraycopy(getRooms(), page * rooms.length, rooms, 0, rooms.length);

										command = new Command(RESPONSE_ROOM_LIST);
										command.put(getRooms().length);
										command.put(Arrays.asList(rooms), Room.class);
									} catch (Exception exception) {
										StackTrace.print(this, exception);
									}

									break;
								case REQUEST_ROOM_CREATE:
									try {
										int maxUsers = command.getInt(1);
										String name = command.getString(2, true);

										Room room = (Room) Data.newInstance(Room.class);
										room.setRoomId(findEmptyRoomId());
										room.setHost(user);
										room.setMaxUsers(maxUsers);
										room.setName(name);
									} catch (Exception exception) {
										StackTrace.print(this, exception);
									}

									break;
								case REQUEST_ROOM_JOIN:
									{
										int roomId = command.getInt(1);

										Room room = Room.findByRoomId(roomId);
										if (room != null)
											room.addUser(user);
									}

									break;
								case REQUEST_ROOM_LEAVE:
									{
										Room room = Room.findByUser(user);
										room.removeUser(user);
									}

									break;
							}
						}
					});
				} else if (endpoint instanceof Client) {
					((Client) endpoint).addCommandListener(new Client.CommandListener() {
						@Override
						public void onCommandReceived(Command command) {
							switch (command.get()) {
								case RESPONSE_ROOM_LIST:
									// TODO: use roomsCount variable.
									// int roomsCount = command.getInt(1);

									command.getObject(2);

									break;
							}
						}
					});
				}
			}
		});
	}


	protected Room() {}


	public static Room[] getRooms() {
		ArrayList<Room> rooms = new ArrayList<>();

		for (Data data : Data.get().values())
			if (data instanceof User)
				rooms.add((Room) data);

		return rooms.toArray(new Room[]{});
	}

	public static Room findByRoomId(int roomId) {
		for (Data data : Data.get().values())
			if (data instanceof Room)
				if (((Room) data).getRoomId() == roomId)
					return (Room) data;

		return null;
	}

	private static Room findByUser(User user) {
		for (Room room : getRooms())
			if (room.containUser(user))
				return room;

		return null;
	}

	private static int findEmptyRoomId() {
		int id = 1;

		ArrayList<Integer> listRoomId = new ArrayList<>();
		for (Room room : getRooms())
			listRoomId.add(room.getRoomId());

		while (true)
			if (listRoomId.contains(id))
				id++;
			else
				break;

		return id;
	}


	// Room check methods
	private boolean isEmpty() { return getListUserId().size() == 0; }
	private boolean isFull() { return getListUserId().size() >= getMaxUsers(); }

	// Host check/handling methods
	private User getHost() {
		if (isEmpty()) return null;

		return User.findByUserId(getListUserId().get(0));
	}
	private void setHost(User user) {
		getListUserId().remove(user.getUserId());
		getListUserId().add(0, user.getUserId());
	}

	// User check/handling methods
	private boolean addUser(User user) {
		if (isFull()) return false;
		if (containUser(user)) return false;

		return getListUserId().add(user.getUserId());
	}
	private boolean containUser(User user) {
		return getListUserId().contains(user.getUserId());
	}
	private boolean removeUser(User user) {
		if (!containUser(user)) return false;

		return getListUserId().remove(user.getUserId());
	}


	// Default getters and setters
	public int getRoomId() { return roomId; }
	public ArrayList<String> getListUserId() { return listUserId; }
	public String getName() { return name; }
	public int getMaxUsers() { return maxUsers; }
	public void setRoomId(int roomId) { this.roomId = roomId; }
	public void setListUserId(ArrayList<String> listUserId) { this.listUserId = listUserId; }
	public void setName(String name) { this.name = name; }
	public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }


	@Override
	public int compareTo(Room another) {
		return this.getRoomId() - another.getRoomId();
	}
}