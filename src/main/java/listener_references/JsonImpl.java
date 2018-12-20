package listener_references;

import org.json.JSONObject;

import java.sql.Timestamp;

public abstract class JsonImpl {

    protected final JSONObject data;
    protected final Timestamp messageReceived;

    /**
     * Constructs a new {@link JsonImpl} class
     * @param data the {@link JSONObject} to be contained
     */
    public JsonImpl(JSONObject data) {
        this.data = data;
        this.messageReceived = new Timestamp(System.currentTimeMillis());
    }

    /**
     * @return the contained {@link JSONObject}
     */
    public JSONObject getJsonObject() {
        return data;
    }

    /**
     * @return the {@link Connection} associated with this class
     */
    public abstract Connection getConnection();

    /**
     * @return the {@link Timestamp} of when the json was received
     */
    public Timestamp getJsonReceived() {
        return messageReceived;
    }

}
