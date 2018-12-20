package listeners;

import listener_references.ClientCommand;

/**
 * CommandListener to be fired whenever a ClientCommand is received
 */
public interface ClientCommandListener extends Listener {
    void onCommandReceived(ClientCommand command);
}
