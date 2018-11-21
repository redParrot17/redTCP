package listener_references;

import listeners.CommandListener;
import packets.CommandPacket;

/**
 * The command object to be passed to any {@link CommandListener}
 */
public class Command {

    private final String command;
    private final String arguments;
    private final long commandReceived;
    private final Connection connection;

    /**
     * @param connection the current connection the client currently has
     * @param command    command
     * @param arguments  command arguments
     */
    public Command(Connection connection, String command, String arguments) {
        commandReceived = System.currentTimeMillis();
        this.connection = connection;
        this.arguments = arguments;
        this.command = command;
    }

    /**
     * @param connection    the current connection the client currently has
     * @param commandPacket {@link CommandPacket
     */
    public Command(Connection connection, CommandPacket commandPacket) {
        this.connection = connection;
        this.command = commandPacket.getCommand();
        this.arguments = commandPacket.getArguments();
        this.commandReceived = System.currentTimeMillis();
    }

    public long getCommandReceived() {
        return commandReceived;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getCommand() {
        return command;
    }

    public String getArguments() {
        return arguments;
    }
}
