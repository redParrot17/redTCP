package listener_references;

import org.json.JSONObject;

/**
 * The {@link ClientJson} object to be passed to a {@link listeners.ClientJsonListener}
 */
public class ClientJson extends JsonImpl {

    protected final ClientConnection connection;

    /**
     * Constructs a new {@link ClientJson}
     * @param json       the {@link JSONObject}
     * @param connection the {@link ClientConnection} the json was received from
     */
    public ClientJson(JSONObject json, ClientConnection connection) {
        super(json);
        this.connection = connection;
    }

    /**
     * @return the {@link ClientConnection} the json was received from
     */
    @Override
    public ClientConnection getConnection() {
        return connection;
    }
}
