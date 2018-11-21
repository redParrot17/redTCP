package listeners;

import listener_references.Command;

/**
 * CommandListener to be fired whenever a command is received
 */
public interface CommandListener extends Listener {
    void onCommandReceived(Command command);
}
