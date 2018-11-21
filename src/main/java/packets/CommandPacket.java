package packets;

public class CommandPacket extends DataPacket {

    private final String command;
    private final String arguments;

    /**
     * @param command   the action to be taken
     * @param arguments the data corresponding to the action
     */
    public CommandPacket(String command, String arguments) {
        super();
        this.command = command;
        this.arguments = arguments;
    }

    public String getCommand() {
        return command;
    }

    public String getArguments() {
        return arguments;
    }
}
