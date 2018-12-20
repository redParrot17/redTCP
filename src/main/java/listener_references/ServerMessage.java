package listener_references;

/**
 * The {@link ServerMessage} object to be passed to a {@link listeners.ServerMessageListener}
 */
public class ServerMessage extends MessageImpl {

    protected final ServerConnection connection;

    /**
     * Constructs a new {@link ServerMessage}
     * @param message    the message
     * @param connection the {@link ServerConnection} the message was received from
     */
    public ServerMessage(String message, ServerConnection connection) {
        super(message);
        this.connection = connection;
    }

    /**
     * @return the {@link ServerConnection} the message was received from
     */
    @Override
    public ServerConnection getConnection() {
        return connection;
    }
}
