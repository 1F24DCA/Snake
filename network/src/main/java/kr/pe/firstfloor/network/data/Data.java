
package kr.pe.firstfloor.network.data;

import java.util.*;

import kr.pe.firstfloor.network.*;
import kr.pe.firstfloor.annotation.ExtendableSingletonClass;

@ExtendableSingletonClass
public class Data {
    protected Data() {}


    private static Endpoint endpoint = null;
    private static final HashMap<Integer, Data> data = new HashMap<>();
    private static final ArrayList<EndpointRegisterListener> endpointRegisterListeners = new ArrayList<>();


    public static void register(final Endpoint endpoint) {
        if (Data.endpoint == null) {
            Data.endpoint = endpoint;

            if (endpoint instanceof Server) {
                ((Server) endpoint).addConnectionListener(new Server.ConnectionListener() {
                    @Override
                    public void onConnected(User user, boolean isReconnected) {
                        if (isReconnected) {
                            user.resetRemoveTime();

                            // TODO: rollback last used activity for disconnected users.
                        }
                    }

                    @Override
                    public void onDisconnected(User user, String reason) {
                        user.setRemoveTime(90);
                    }
                });
            } else if (endpoint instanceof Client) {
                ((Client) endpoint).addConnectionListener(new Client.ConnectionListener() {
                    @Override
                    public void onConnected(User user) {}

                    @Override
                    public void onDisconnected(String reason) {
                        data.clear();

                        Data.endpoint = null;
                    }
                });

                ((Client) endpoint).addCommandListener(new Client.CommandListener() {
                    @Override
                    public void onCommandReceived(Command command) {
                        if (command.is(Server.COMMAND_DATA))
                            command.setObject(data, 1);
                    }
                });
            }
        }

        for (EndpointRegisterListener listener : endpointRegisterListeners)
            listener.onEndpointRegistered(endpoint);
    }


    public static Data newInstance(Class<? extends Data> toInstantiation) throws EndpointNotFoundException, UnsupportedInClientException, IllegalAccessException, InstantiationException {
        if (endpoint == null) {
            throw new EndpointNotFoundException();
        } else if (toInstantiation.equals(Data.class)) {
            throw new IllegalAccessException("Can't create 'Data.class' itself. you can only create extended classes of Data class.");
        } else if (endpoint instanceof Client) {
            throw new UnsupportedInClientException();
        } else if (endpoint instanceof Server) {
            int id = 0;

            while (true)
                if (data.keySet().contains(id))
                    id++;
                else
                    break;

            Data instance = toInstantiation.newInstance();
            data.put(id, instance);

            return instance;
        }

        return null;
    }

    public static Data newInstance(Class<? extends Data> toInstantiation, int id) throws EndpointNotFoundException, UnsupportedInServerException, IllegalAccessException, InstantiationException {
        if (endpoint == null) {
            throw new EndpointNotFoundException();
        } else if (toInstantiation.equals(Data.class)) {
            throw new IllegalAccessException("Can't create 'Data.class' itself. you can only create extended classes of Data class.");
        } else if (endpoint instanceof Server) {
            throw new UnsupportedInServerException();
        } else if (endpoint instanceof Client) {
            Data instance = toInstantiation.newInstance();
            data.put(id, instance);

            return instance;
        }

        return null;
    }


    public static int getId(Data dataToGet) {
        for (int id : data.keySet())
            if (data.get(id).equals(dataToGet))
                return id;

        return -1;
    }

    public static Data findById(int id) {
        return data.get(id);
    }

    public static Data removeById(int id) {
        return data.remove(id);
    }

    public static boolean remove(Data dataToRemove) {
        return data.remove(getId(dataToRemove)) != null;
    }

    protected static HashMap<Integer, Data> get() { return data; }


    protected static void addEndpointRegisterListener(EndpointRegisterListener listener) {
        endpointRegisterListeners.add(listener);
    }


    @Override
    public int hashCode() { return Integer.valueOf(getId(this)).hashCode(); }

    @Override
    public boolean equals(Object another) {
        if (another instanceof Data)
            if (getId(this) == getId((Data) another))
                return true;

        return false;
    }
}
//