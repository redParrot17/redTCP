package client;

/**
 * The class {@code client.ClientException} and its subclasses are a form of
 * {@code Exception} that indicates conditions that a reasonable
 * application might want to catch.
 *
 * <p>The class {@code client.ClientException} and any subclasses are <em>checked
 * exceptions</em>.  Checked exceptions need to be declared in a
 * method or constructor's {@code throws} clause if they can be thrown
 * by the execution of the method or constructor and propagate outside
 * the method or constructor boundary.
 *
 * @author  Frank Yellin
 * @author  Christian Burns
 * @since   1.0
 */
public class ClientException extends Exception {
    static final long serialVersionUID = -3855894576576512748L;

    public ClientException(String message, Exception e) {
        super(message.isEmpty() ? e.getMessage() : message + " | " + e.getMessage(), e.getCause(), e.getSuppressed().length != 0, e.getStackTrace() != null);
    }

    /**
     * Constructs a new client exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ClientException() {
        super();
    }

    /**
     * Constructs a new client exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public ClientException(String message) {
        super(message);
    }

}
