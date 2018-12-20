package listeners;

import listener_references.ClientJson;

/**
 * JsonListener to be fired whenever a ClientJson object is received
 */
public interface ClientJsonListener extends Listener {
    void onJsonReceived(ClientJson json);
}
