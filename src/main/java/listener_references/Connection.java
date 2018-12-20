package listener_references;

import java.net.Socket;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Connection class that contains information about a socket connection
 */
public class Connection {

    protected transient final Socket socket;
    protected final Timestamp connectionCreation;

    /**
     * @param socket the {@link Socket} associated with the connection
     */
    public Connection(Socket socket) {
        connectionCreation = new Timestamp(System.currentTimeMillis());
        this.socket = socket;
    }

    /**
     * @return the {@link Socket} associated with this connection
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @return the {@link Timestamp} of when the connection was established
     */
    public Timestamp getConnectionCreated() {
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
