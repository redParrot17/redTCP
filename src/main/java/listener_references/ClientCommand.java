package listener_references;

import packets.CommandPacket;

/**
 * The command object to be passed to a command listener
 */
public class ClientCommand extends CommandImpl {

    private final ClientConnection connection;

    /**
     * Constructs a new {@link ClientCommand}
     * @param command    the command
     * @param arguments  the command arguments
     * @param connection the {@link ClientConnection} the command was received from
     */
    public ClientCommand(String command, String arguments, ClientConnection connection) {
        super(command, arguments);
        this.connection = connection;
    }

    /**
     * Constructs a new {@link ClientCommand} from the specified {@link CommandPacket}
     * @param packet     the {@link CommandPacket} to fetch the command from
     * @param connection the {@link ClientConnection} the packet was received from
     */
    public ClientCommand(CommandPacket packet, ClientConnection connection) {
        super(packet);
        this.connection = connection;
    }

    /**
     * @return the {@link ClientConnection} the command was received from
     */
    @Override
    public ClientConnection getConnection() {
        return connection;
    }
}
