package listeners;

import listener_references.ServerMessage;

/**
 * MessageListener to be fired whenever a ServerMessage is received
 */
public interface ServerMessageListener extends Listener {
    void onMessageReceived(ServerMessage message);
}
