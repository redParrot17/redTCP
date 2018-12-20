package client;

import listener_references.*;
import listeners.ClientCommandListener;
import listeners.ClientJsonListener;
import listeners.ClientMessageListener;
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
public class ClientListenerManager {

    private ExecutorService executor;
    private volatile List<ClientMessageListener> messageListeners;
    private volatile List<ClientCommandListener> commandListeners;
    private volatile List<ClientJsonListener> jsonListeners;

    /**
     * Constructs a new {@link ClientListenerManager} with no listeners pre-registered
     */
    ClientListenerManager() {
        executor = Executors.newCachedThreadPool();
        messageListeners = new ArrayList<>();
        commandListeners = new ArrayList<>();
        jsonListeners = new ArrayList<>();
    }

    /**
     * Adds the specified {@link ClientMessageListener} to the list
     * @param listener the listener to be added
     */
    public void addMessageListener(ClientMessageListener listener) {
        Objects.requireNonNull(listener);
        messageListeners.add(listener);
    }

    /**
     * Removes the specified {@link ClientMessageListener} from the list
     * @param listener the listener to be removed
     */
    public void removeMessageListener(ClientMessageListener listener) {
        Objects.requireNonNull(listener);
        messageListeners.remove(listener);
    }

    /**
     * Adds the specified {@link ClientCommandListener} to the list
     * @param listener the listener to be added
     */
    public void addCommandListener(ClientCommandListener listener) {
        Objects.requireNonNull(listener);
        commandListeners.add(listener);
    }

    /**
     * Removes the specified {@link ClientCommandListener} from the list
     * @param listener the listener to be removed
     */
    public void removeCommandListener(ClientCommandListener listener) {
        Objects.requireNonNull(listener);
        commandListeners.remove(listener);
    }

    /**
     * Adds the specified {@link ClientJsonListener} to the list
     * @param listener the listener to be added
     */
    void addJsonListener(ClientJsonListener listener) {
        Objects.requireNonNull(listener);
        jsonListeners.add(listener);
    }

    /**
     * Removes the specified {@link ClientJsonListener} from the list
     * @param listener the listener to be removed
     */
    void removeJsonListener(ClientJsonListener listener) {
        Objects.requireNonNull(listener);
        jsonListeners.remove(listener);
    }

    /**
     * Removes every single listener registered to the server
     */
    public void removeAllListeners() {
        messageListeners.clear();
        commandListeners.clear();
        jsonListeners.clear();
    }

    /**
     * Runs each of the {@link ClientMessageListener}s with the {@code message} as input
     * @param message the {@link MessageImpl} to pass to each of the listeners
     */
    synchronized void raiseMessageEvent(ClientMessage message) {
        messageListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onMessageReceived(message);
            return null;
        }));
    }

    /**
     * Runs each of the {@link ClientCommandListener}s with the {@code command} as input
     * @param command the {@link CommandImpl} to pass to each of the listeners
     */
    synchronized void raiseCommandEvent(ClientCommand command) {
        commandListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onCommandReceived(command);
            return null;
        }));
    }

    /**
     * Runs each of the {@link ClientJsonListener}s with the {@code command} as input
     * @param json the {@link JSONObject} to pass to each of the listeners
     */
    synchronized void raiseJsonEvent(ClientJson json) {
        jsonListeners.forEach(listener -> executor.submit((Callable<Void>) () -> {
            listener.onJsonReceived(json);
            return null;
        }));
    }

}
