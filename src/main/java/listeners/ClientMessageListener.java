package listeners;

import listener_references.ClientMessage;

/**
 * MessageListener to be fired whenever a ClientMessage is received
 */
public interface ClientMessageListener extends Listener {
    void onMessageReceived(ClientMessage message);
}
