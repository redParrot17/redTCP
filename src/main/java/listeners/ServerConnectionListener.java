package listeners;

import listener_references.ServerConnection;

/**
 * ServerConnectionListener to be fired whenever a connection is made or destroyed
 */
public interface ServerConnectionListener extends Listener {
    void onConnectionCreated(ServerConnection connection);
    void onConnectionRemoved(ServerConnection connection);
}
