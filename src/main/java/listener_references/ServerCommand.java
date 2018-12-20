package listener_references;

import packets.CommandPacket;

/**
 * The {@link ServerCommand} to be passed to a {@link listeners.ServerCommandListener}
 */
public class ServerCommand extends CommandImpl {

    private final ServerConnection connection;

    /**
     * Constructs a new {@link ServerCommand}
     * @param command    the command
     * @param arguments  the command arguments
     * @param connection the {@link ServerConnection} the command was received from
     */
    public ServerCommand(String command, String arguments, ServerConnection connection) {
        super(command, arguments);
        this.connection = connection;
    }

    /**
     * Constructs a new {@link ServerCommand} from an existing {@link CommandPacket}
     * @param packet     the {@link CommandPacket} to obtain the command and arguments from
     * @param connection the {@link ServerConnection} the command packet was received from
     */
    public ServerCommand(CommandPacket packet, ServerConnection connection) {
        super(packet);
        this.connection = connection;
    }

    /**
     * @return the {@link ServerConnection} the command was received from
     */
    @Override
    public ServerConnection getConnection() {
        return connection;
    }
}
