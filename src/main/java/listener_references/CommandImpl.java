package listener_references;

import packets.CommandPacket;

import java.sql.Timestamp;

/**
 * The command object to be passed to a command listener
 */
public abstract class CommandImpl {

    protected final String command;
    protected final String arguments;
    protected final Timestamp commandReceived;

    /**
     * @param command   command
     * @param arguments command arguments
     */
    public CommandImpl(String command, String arguments) {
        commandReceived = new Timestamp(System.currentTimeMillis());
        this.arguments = arguments;
        this.command = command;
    }

    /**
     * @param commandPacket {@link CommandPacket
     */
    public CommandImpl(CommandPacket commandPacket) {
        this.command = commandPacket.getCommand();
        this.arguments = commandPacket.getArguments();
        this.commandReceived = new Timestamp(System.currentTimeMillis());
    }

    /**
     * @return the {@link Timestamp} of when the command was received
     */
    public Timestamp getCommandReceived() {
        return commandReceived;
    }

    public abstract Connection getConnection();

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return the command arguments
     */
    public String getArguments() {
        return arguments;
    }
}
