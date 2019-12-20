
package kr.pe.firstfloor.network.data;

import java.util.ArrayList;
import java.util.Arrays;

import kr.pe.firstfloor.annotation.Constant;
import kr.pe.firstfloor.network.Client;
import kr.pe.firstfloor.network.Endpoint;
import kr.pe.firstfloor.network.Server;
import kr.pe.firstfloor.util.StackTrace;

public class User extends Data {
    @Constant
    public static final int REQUEST_USER_LIST = 7;
    @Constant
    public static final int RESPONSE_USER_LIST = 8;


    private String userId = null;
    private long removeTime = 0;


    protected User() {}


    static {
        Data.addEndpointRegisterListener(new EndpointRegisterListener() {
            @Override
            public void onEndpointRegistered(Endpoint endpoint) {
                if (endpoint instanceof Server) {
                    ((Server) endpoint).addCommandListener(new Server.CommandListener() {
                        @Override
                        public void onCommandReceived(User user, Command command) {
                            switch (command.get()) {
                                case REQUEST_USER_LIST:
                                    try {
                                        int page = command.getInt(1) - 1;

                                        User[] users = new User[10];
                                        System.arraycopy(getUsers(), page * users.length, users, 0, users.length);

                                        command = new Command(REQUEST_USER_LIST);
                                        command.put(getUsers().length);
                                        command.put(Arrays.asList(users), User.class);
                                    } catch (Exception exception) {
                                        StackTrace.print(this, exception);
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
                                case RESPONSE_USER_LIST:
                                    // TODO: use usersCount variable.
                                    // int usersCount = command.getInt(1);

                                    command.getObject(2);

                                    break;
                            }
                        }
                    });
                }

                Thread checkThread = new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            for (User user : getUsers()) {
                                if (user.getRemoveTime() > 0 && user.getRemoveTime() < System.currentTimeMillis()) {
                                    Data.remove(user);

                                    break;
                                } else {
                                    try { Thread.yield(); } catch (Exception e) {}
                                }
                            }
                        }
                    }
                };

                checkThread.setPriority(Thread.MIN_PRIORITY);
                checkThread.start();
            }
        });
    }


    public static User[] getUsers() {
        ArrayList<User> users = new ArrayList<>();

        for (Data data : Data.get().values())
            if (data instanceof User)
                users.add((User) data);

        return users.toArray(new User[]{});
    }

    public static User findByUserId(String userId) {
        for (Data data : Data.get().values())
            if (data instanceof User)
                if (((User) data).getUserId().equals(userId))
                    return (User) data;

        return null;
    }


    public void resetRemoveTime() { this.removeTime = 0L; }
    public void setRemoveTime(int time) {
        if (getRemoveTime() == 0L)
            this.removeTime = System.currentTimeMillis() + time*1000L;
    }


    public String getUserId() { return userId; }
    public long getRemoveTime() { return removeTime; }
    public void setUserId(String userId) {
        if (userId != null)
            this.userId = userId;
    }
}
