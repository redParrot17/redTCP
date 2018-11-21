package listeners;

import listener_references.Message;

/**
 * MessageListener to be fired whenever a simple message is received
 */
public interface MessageListener extends Listener {
    void onMessageReceived(Message message);
}
