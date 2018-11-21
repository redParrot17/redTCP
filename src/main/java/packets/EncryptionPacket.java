package packets;

import javax.crypto.spec.GCMParameterSpec;

/**
 * What is to be primarily sent through the network to the client
 */
public class EncryptionPacket {

    private String payload;
    private PacketType payloadType;
    private GCMParameterSpec gcmParamSpec;
    private String key;

    /**
     *
     * @param payload       encrypted data String
     * @param payloadType   {@link PacketType} indicating what the {@code payload} contains
     * @param gcmParamSpec  the GCM parameter specifications used during encryption
     * @param key           the asymmetrically encrypted symmetric encryption key for the {@code payload}
     */
    public EncryptionPacket(String payload, PacketType payloadType, GCMParameterSpec gcmParamSpec, String key) {
        this.payload = payload;
        this.payloadType = payloadType;
        this.gcmParamSpec = gcmParamSpec;
        this.key = key;
    }

    public String getPayload() {
        return payload;
    }

    public PacketType getPayloadType() { return payloadType; }

    public GCMParameterSpec getGcmParamSpec() {
        return gcmParamSpec;
    }

    public String getKey() {
        return key;
    }

    /**
     * Enum for the different types of data to be contained within the EncryptionPacket's payload
     */
    public enum PacketType {
        TEXT, COMMAND
    }
}
