package listeners;

import listener_references.ServerCommand;

/**
 * CommandListener to be fired whenever a ServerCommand is received
 */
public interface ServerCommandListener extends Listener {
    void onCommandReceived(ServerCommand command);
}
