package packets;

import java.util.UUID;

/**
 * Generic datapacket to which all other packets should be children of
 */
public class DataPacket {

    private final long timestamp;
    private final String uuid;

    DataPacket() {
        uuid = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @param timestamp current time in milliseconds the packet was created
     */
    DataPacket(long timestamp) {
        uuid = UUID.randomUUID().toString();
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUuid() {
        return uuid;
    }
}
