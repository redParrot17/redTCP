package server;

import listener_references.*;
import listeners.ServerCommandListener;
import listeners.ServerConnectionListener;
import listeners.ServerJsonListener;
import listeners.ServerMessageListener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A listener manager to manage all the event listeners
 */
public class ServerListenerManager {

    private ExecutorService executor;
    private volatile List<ServerMessageListener> messageListeners;
    private volatile List<ServerConnectionListener> connectionListeners;
    private volatile List<ServerCommandListener> commandListeners;
    private volatile List<ServerJsonListener> jsonListeners;

    /**
     * Constructs a new {@link ServerListenerManager} with no listeners pre-registered
     */
    public ServerListenerManager() {
        executor = Executors.newCachedThreadPool();
        connectionListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
        commandListeners = new ArrayList<>();
        jsonListeners = new ArrayList<>();
    }

    /**
     * Adds the specified {@link ServerMessageListener} to the list
     * @param listener the listener to be added
     */
    public void addMessageListener(ServerMessageListener listener) {
        Objects.requireNonNull(listener);
        messageListeners.add(listener);
    }

    /**
     * Removes the specified {@link ServerMessageListener} from the list
     * @param listener the listener to be removed
     */
    public void removeMessageListener(ServerMessageListener listener) {
        Objects.requireNonNull(listener);
        messageListeners.remove(listener);
    }

    /**
     * Adds the specified {@link ServerCommandListener} to the list
     * @param listener the listener to be added
     */
    public void addCommandListener(ServerCommandListener listener) {
        Objects.requireNonNull(listener);
        commandListeners.add(listener);
    }

    /**
     * Removes the specified {@link ServerCommandListener} from the list
     * @param listener the listener to be removed
     */
    public void removeCommandListener(ServerCommandListener listener) {
        Objects.requireNonNull(listener);
        commandListeners.remove(listener);
    }

    /**
     * Adds the specified {@link ServerJsonListener} to the list
     * @param listener the listener to be added
     */
    void addJsonListener(ServerJsonListener listener) {
        Objects.requireNonNull(listener);
        jsonListeners.add(listener);
    }

    /**
     * Removes the specified {@link ServerJsonListener} from the list
     * @param listener the listener to be removed
     */
    void removeJsonListener(ServerJsonListener listener) {
        Objects.requireNonNull(listener);
        jsonListeners.remove(listener);
    }

    /**
     * Adds the specified {@link ServerConnectionListener} to the list
     * @param listener the listener to be added
     */
    void addConnectionListener(ServerConnectionListener listener) {
        Objects.requireNonNull(listener);
        connectionListeners.add(listener);
    }

    /**
     * Removes the specified {@link ServerConnectionListener} from the list
     * @param listener the listener to be removed
     */
    void removeConnectionListener(ServerConnectionListener listener) {
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
        jsonListeners.clear();
    }

    /**
     * Runs each of the {@link ServerMessageListener}s with the {@code message} as input
     * @param message the {@link MessageImpl} to pass to each of the listeners
     */
    synchronized void raiseMessageEvent(ServerMessage message) {
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
    synchronized void raiseConnectionEvent(ServerConnection connection, Connection.Event event) {
        connectionListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            if (event == Connection.Event.CONNECTED) listener.onConnectionCreated(connection);
            if (event == Connection.Event.REMOVED) listener.onConnectionRemoved(connection);
            return null;
        }));
    }

    /**
     * Runs each of the {@link ServerConnectionListener}s with the {@code command} as input
     * @param command the {@link CommandImpl} to pass to each of the listeners
     */
    synchronized void raiseCommandEvent(ServerCommand command) {
        commandListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onCommandReceived(command);
            return null;
        }));
    }

    /**
     * Runs each of the {@link ServerJsonListener}s with the {@code json} as input
     * @param json the {@link JSONObject} to pass to each of the listeners
     */
    synchronized void raiseJsonEvent(ServerJson json) {
        jsonListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onJsonReceived(json);
            return null;
        }));
    }

}
