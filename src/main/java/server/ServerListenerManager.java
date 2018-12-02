package server;

import listener_references.Command;
import listener_references.Connection;
import listener_references.Message;
import listener_references.ServerConnection;
import listeners.CommandListener;
import listeners.MessageListener;
import listeners.ServerConnectionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerListenerManager {

    private ExecutorService executor;
    private volatile List<MessageListener> messageListeners;
    private volatile List<ServerConnectionListener> connectionListeners;
    private volatile List<CommandListener> commandListeners;

    /**
     * Constructs a new {@link ServerListenerManager} with no listeners pre-registered
     */
    public ServerListenerManager() {
        executor = Executors.newCachedThreadPool();
        connectionListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
        commandListeners = new ArrayList<>();
    }

    /**
     * Adds the specified {@link MessageListener} to the list
     * @param listener the listener to be added
     */
    public void addMessageListener(MessageListener listener) {
        Objects.requireNonNull(listener);
        messageListeners.add(listener);
    }

    /**
     * Removes the specified {@link MessageListener} from the list
     * @param listener the listener to be removed
     */
    public void removeMessageListener(MessageListener listener) {
        Objects.requireNonNull(listener);
        messageListeners.remove(listener);
    }

    /**
     * Adds the specified {@link CommandListener} to the list
     * @param listener the listener to be added
     */
    public void addCommandListener(CommandListener listener) {
        Objects.requireNonNull(listener);
        commandListeners.add(listener);
    }

    /**
     * Removes the specified {@link CommandListener} from the list
     * @param listener the listener to be removed
     */
    public void removeCommandListener(CommandListener listener) {
        Objects.requireNonNull(listener);
        commandListeners.remove(listener);
    }

    /**
     * Adds the specified {@link ServerConnectionListener} to the list
     * @param listener the listener to be added
     */
    public void addConnectionListener(ServerConnectionListener listener) {
        Objects.requireNonNull(listener);
        connectionListeners.add(listener);
    }

    /**
     * Removes the specified {@link ServerConnectionListener} from the list
     * @param listener the listener to be removed
     */
    public void removeConnectionListener(ServerConnectionListener listener) {
        Objects.requireNonNull(listener);
        connectionListeners.remove(listener);
    }

    /**
     * Removes every single listener registered to the server
     */
    public void removeAllListeners() {
        connectionListeners.clear();
        commandListeners.clear();
        messageListeners.clear();
    }

    /**
     * Runs each of the {@link MessageListener}s with the {@code message} as input
     * @param message the {@link Message} to pass to each of the listeners
     */
    public synchronized void raiseMessageEvent(Message message) {
        messageListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onMessageReceived(message);
            return null;
        }));
    }

    /**
     * Runs each of the {@link ServerConnectionListener}s with the {@code connection} and {@code Event} as input
     * @param connection the {@link ServerConnection} to pass to each of the listeners
     * @param event      the {@link Connection.Event} associated with the connection
     */
    public synchronized void raiseConnectionEvent(ServerConnection connection, Connection.Event event) {
        connectionListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            if (event == Connection.Event.CONNECTED) listener.onConnectionCreated(connection);
            if (event == Connection.Event.REMOVED) listener.onConnectionRemoved(connection);
            return null;
        }));
    }

    /**
     * Runs each of the {@link CommandListener}s with the {@code command} as input
     * @param command the {@link Command} to pass to each of the listeners
     */
    public synchronized void raiseCommandEvent(Command command) {
        commandListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onCommandReceived(command);
            return null;
        }));
    }

}
