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

    /**
     * @return the payload of the packet
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @return the {@link PacketType} defining what the payload is
     */
    public PacketType getPayloadType() { return payloadType; }

    /**
     * @return the {@link GCMParameterSpec} of the encryption data
     */
    public GCMParameterSpec getGcmParamSpec() {
        return gcmParamSpec;
    }

    /**
     * @return the encrypted symmetric key
     */
    public String getKey() {
        return key;
    }

}
