package kr.pe.firstfloor.util;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Check {
    public interface ExternalNetworkTestListener { void onTested(boolean isAvailable); }
    public static void testExternalNetwork(final String address, final int port, final ExternalNetworkTestListener listener) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean isAvailable = false;
                try {
                    Socket socket = new Socket(address, port);

                    if (socket.isConnected())
                        isAvailable = true;

                    socket.close();
                } catch (Exception exception) {}

                listener.onTested(isAvailable);
            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    // TODO: better way to search? check https://prolite.tistory.com/717
    public interface InternalNetworkTestListener { void onTested(String[] addresses); }
    public static void testInternalNetwork(final int port, final InternalNetworkTestListener listener) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                final List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());
                final List<String> addresses = Collections.synchronizedList(new ArrayList<String>());

                try {
                    String local = InetAddress.getLocalHost().getHostAddress();

                    for (int index = 1; index < 255; index++) {
                        final String address =
                                local.replaceAll("(\\d+\\.\\d+\\.\\d+)\\.\\d+", "$1."+index);

                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Socket socket = new Socket(address, port);

                                    if (socket.isConnected())
                                        addresses.add(address);

                                    socket.close();
                                } catch (Exception exception) {}
                            }
                        };

                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.start();

                        threads.add(thread);
                    }

                    while (threads.size() > 0) {
                        Iterator<Thread> iterator = threads.iterator();

                        while (iterator.hasNext())
                            if (!iterator.next().isAlive())
                                iterator.remove();
                    }
                } catch (Exception exception) {
                    StackTrace.print(this, exception);
                }

                listener.onTested(addresses.toArray(new String[]{}));
            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
