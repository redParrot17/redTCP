package listener_references;

import java.sql.Timestamp;

/**
 * The message object to be passed to any MessageListener
 */
public abstract class MessageImpl {

    protected final String message;
    protected final Timestamp messageReceived;

    /**
     * @param message the message that was received
     */
    public MessageImpl(String message) {
        this.messageReceived = new Timestamp(System.currentTimeMillis());
        this.message = message;
    }

    /**
     * @return the contained message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the {@link Connection} the message was received from
     */
    public abstract Connection getConnection();

    /**
     * @return the {@link Timestamp} the message was received
     */
    public Timestamp getMessageReceived() {
        return messageReceived;
    }
}
