package listener_references;

/**
 * The {@link ClientMessage} object to be passed to a {@link listeners.ClientMessageListener}
 */
public class ClientMessage extends MessageImpl {

    protected final ClientConnection connection;

    /**
     * Constructs a new {@link ClientMessage}
     * @param message    the message
     * @param connection the {@link ClientConnection} the message was received from
     */
    public ClientMessage(String message, ClientConnection connection) {
        super(message);
        this.connection = connection;
    }

    /**
     * @return the {@link ClientConnection} the message was received from
     */
    @Override
    public ClientConnection getConnection() {
        return connection;
    }
}
