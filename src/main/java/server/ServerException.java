package server;

/**
 * An Exception to be thrown by the {@link TcpServer} class
 */
public class ServerException extends Exception {
    static final long serialVersionUID = 2315235713347645137L;

    public ServerException(String message) {
        super(message);
    }

}
