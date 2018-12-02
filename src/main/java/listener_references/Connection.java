package listener_references;

import java.net.Socket;
import java.util.Optional;

/**
 * ServerConnection class that contains information about a socket connection to a client
 */
public class Connection {

    private transient final Socket socket;
    private final long connectionCreation;

    /**
     * @param socket the actual {@link Socket} that the client is connected to
     */
    public Connection(Socket socket) {
        connectionCreation = System.currentTimeMillis();
        this.socket = socket;
    }

    /**
     * @return the {@link Socket} associated with this connection
     */
    public Socket getSocket() {
        return socket;
    }

    public long getConnectionCreated() {
        return connectionCreation;
    }

    /**
     * Attempts to wrap the object as a {@code ServerConnection}
     * @return a {@link Optional<ServerConnection>} of this object wrapped as a {@code ServerConnection}
     */
    public Optional<ServerConnection> asServerConnection() {
        return this instanceof ServerConnection ? Optional.of((ServerConnection) this) : Optional.empty();
    }

    /**
     * Attempts to wrap the object as a {@code ClientConnection}
     * @return a {@link Optional<ClientConnection>} of this object wrapped as a {@code ClientConnection}
     */
    public Optional<ClientConnection> asClientConnection() {
        return this instanceof ClientConnection ? Optional.of((ClientConnection) this) : Optional.empty();
    }

    public enum Event {CONNECTED,REMOVED}

}
