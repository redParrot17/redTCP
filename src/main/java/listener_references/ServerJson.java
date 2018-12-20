package listener_references;

import org.json.JSONObject;

/**
 * The {@link ServerJson} object to be passed to a {@link listeners.ServerJsonListener}
 */
public class ServerJson extends JsonImpl {

    protected final ServerConnection connection;

    /**
     * Constructs a new {@link ServerJson}
     * @param json       the {@link JSONObject}
     * @param connection the {@link ServerConnection} the json was received from
     */
    public ServerJson(JSONObject json, ServerConnection connection) {
        super(json);
        this.connection = connection;
    }

    /**
     * @return the {@link ServerConnection} the json was received from
     */
    @Override
    public ServerConnection getConnection() {
        return connection;
    }
}
