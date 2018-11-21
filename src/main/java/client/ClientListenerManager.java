package client;

import listener_references.Command;
import listener_references.Message;
import listeners.CommandListener;
import listeners.MessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientListenerManager {

    private ExecutorService executor;
    private volatile List<MessageListener> messageListeners;
    private volatile List<CommandListener> commandListeners;

    /**
     * Constructs a new {@link ClientListenerManager} with no listeners pre-registered
     */
    ClientListenerManager() {
        executor = Executors.newCachedThreadPool();
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
     * Removes every single listener registered to the server
     */
    public void removeAllListeners() {
        messageListeners.clear();
        commandListeners.clear();
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
