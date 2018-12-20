package listeners;

import listener_references.ServerJson;

/**
 * ServerJsonListener to be fired whenever a ServerJson object is received
 */
public interface ServerJsonListener extends Listener {
    void onJsonReceived(ServerJson json);
}
