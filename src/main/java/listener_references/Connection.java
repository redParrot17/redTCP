package listener_references;

import java.net.Socket;

/**
 * ServerConnection class that contains information about a socket connection to a client
 */
public class Connection {

    private transient final Socket socket;
    private final long connectionCreation;

    /**
     * @param socket    the actual {@link Socket} that the client is connected to
     */
    public Connection(Socket socket) {
        connectionCreation = System.currentTimeMillis();
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public long getConnectionCreated() {
        return connectionCreation;
    }

    public enum event {CONNECTED,REMOVED}

}
