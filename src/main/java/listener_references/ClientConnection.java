package listener_references;

import client.ClientException;
import client.TcpClient;

import java.net.Socket;

/**
 * ServerConnection class that contains information about a socket connection to a client
 */
public class ClientConnection extends Connection {

    private transient final TcpClient client;

    /**
     *
     * @param server    the {@link TcpClient} currently accepting client connections
     * @param socket    the actual {@link Socket} that the client is connected to
     */
    public ClientConnection(TcpClient server, Socket socket) {
        super(socket);
        this.client = server;
    }

    /**
     * Sends a simple message to the client connected through this connection
     * @param data text to be sent
     */
    public void replyText(String data) throws ClientException {
        client.sendText(data);
    }

    /**
     * Sends a command to the client connected through this connection
     * @param command   command to be sent
     * @param arguments the command arguments
     */
    public void replyCommand(String command, String arguments) throws ClientException {
        client.sendCommand(command, arguments);
    }

}
