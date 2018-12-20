package client;

import cryptography.HybridCryptography;
import cryptography.SecuredGCMUsage;
import listener_references.ClientCommand;
import listener_references.ClientConnection;
import listener_references.ClientJson;
import listener_references.ClientMessage;
import listeners.ClientCommandListener;
import listeners.ClientJsonListener;
import listeners.ClientMessageListener;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import packets.CommandPacket;
import packets.PacketType;

import javax.crypto.spec.GCMParameterSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cryptography.HybridCryptography.decrypt;
import static cryptography.HybridCryptography.encrypt;
import static packets.PacketType.TEXT;

public class TcpClient implements AutoCloseable, Runnable {

    private int port;
    private Socket socket;
    private String address;
    private boolean isOpen;
    private KeyPair clientKeys;
    private PrintWriter outgoing;
    private ClientConnection connection;
    private BufferedReader incoming;
    private PublicKey serverPublicKey;
    private ExecutorService executorService;
    private ClientListenerManager listenerManager;

    /**
     * Creates a new {@link TcpClient} bound to the specified host and port
     *
     * @param   host the host name, or {@code null} for the loopback address.
     * @param   port the port number.
     */
    public TcpClient(String host, int port) {
        socket = null;
        isOpen = false;
        incoming = null;
        outgoing = null;
        this.port = port;
        clientKeys = null;
        connection = null;
        this.address = host;
        serverPublicKey = null;
        executorService = null;
        listenerManager = new ClientListenerManager();
    }

    /**
     * Creates a new {@link TcpClient} bound to the specified host and port
     *
     * @param host               the address of the server
     * @param port               the port the client should try to connect to the server through
     * @param connectImmediately true if you want the client to automatically connect to the server
     * @throws ClientException   if something went wrong while the client tried connecting to the server
     */
    public TcpClient(String host, int port, boolean connectImmediately) throws ClientException {
        socket = null;
        isOpen = false;
        incoming = null;
        outgoing = null;
        this.port = port;
        clientKeys = null;
        connection = null;
        this.address = host;
        serverPublicKey = null;
        executorService = null;
        listenerManager = new ClientListenerManager();

        if (connectImmediately) connect().join();
    }

    /**
     * Attempts to connect the client to the server specified when
     * creating the client object
     *
     * @return  an empty completed future when the client is fully
     *          connected to the server
     * @throws  ClientException if the client failed to create a
     *          secure connection with the server
     */
    public CompletableFuture<Void> connect() throws ClientException {
        if (isOpen) throw new ClientException("Client is already connected to the server");
        try { clientKeys = HybridCryptography.generateKeys();
        } catch (NoSuchAlgorithmException e) {
            throw new ClientException("Unable to generate async encryption keys: " + e.getMessage());
        }
        if (clientKeys == null) throw new ClientException("Failed to generate async encryption keys");
        try {
            socket = new Socket(address, port);
            socket.setKeepAlive(true);
            incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outgoing = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ioe) {
            throw new ClientException("Failed to connect to server: " + ioe.getMessage() + " | Is the server actually running?");
        }
        try { exchangeKeys();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ClientException("Unable to process server's public async encryption key: " + e.getMessage());
        } catch (IOException e) {
            throw new ClientException("Communication failure when exchanging public async keys with the server: " + e.getMessage());
        }
        isOpen = true;
        connection = new ClientConnection(this, socket);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Attempts to exchange public async encryption keys with the
     * connected server
     *
     * @throws  IOException if something went wrong with the server's
     *          incoming or outgoing streams
     * @throws  NoSuchAlgorithmException if the encryption algorithm didn't exist
     * @throws  InvalidKeySpecException if the key specifications were invalid
     * @throws  ClientException if the client couldn't send a confirmation
     *          message to the server after completing the handshake
     */
    private void exchangeKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ClientException {
        outgoing.println(Arrays.toString(clientKeys.getPublic().getEncoded()));
        String firstMessage = incoming.readLine();
        if (firstMessage == null) throw new IOException();
        byte[] keyBytes = parseStrByteArray(firstMessage);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        serverPublicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        try { sendText("handshake");
        } catch (ClientException e) {
            throw new ClientException("Failed to send handshake confirmation message: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void addMessageListener(ClientMessageListener listener) {
        listenerManager.addMessageListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeMessageListener(ClientMessageListener listener) {
        listenerManager.removeMessageListener(listener);
    }
    @SuppressWarnings("unused")
    public void addCommandListener(ClientCommandListener listener) {
        listenerManager.addCommandListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeCommandListener(ClientCommandListener listener) {
        listenerManager.removeCommandListener(listener);
    }
    @SuppressWarnings("unused")
    public void addJsonListener(ClientJsonListener listener) {
        listenerManager.addJsonListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeJsonListener(ClientJsonListener listener) {
        listenerManager.removeJsonListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeAllListeners() {
        listenerManager.removeAllListeners();
    }
    public Optional<ClientConnection> getConnection() {
        return Optional.of(connection);
    }

    @Override
    public void run() {
        try {
            while (isOpen) {

                String received = incoming.readLine();
                if (received == null) return;
                String raw = new String(Base64.decodeBase64(received));
                JSONObject packet = new JSONObject(raw);
                JSONObject data = decryptEncryptionPacket(packet, serverPublicKey, clientKeys.getPrivate());

                switch (packet.getEnum(PacketType.class, "type")) {
                    case TEXT:
                        String text = data.getString("text");
                        listenerManager.raiseMessageEvent(new ClientMessage(text, connection));
                        break;
                    case COMMAND:
                        String command = data.getString("command");
                        String arguments = data.getString("arguments");
                        CommandPacket cPacket = new CommandPacket(command, arguments);
                        listenerManager.raiseCommandEvent(new ClientCommand(cPacket, connection));
                        break;
                    case JSON:
                        listenerManager.raiseJsonEvent(new ClientJson(data, connection));
                        break;
                }
            }
        } catch (SocketException se) {
            if (!se.getMessage().equals("Socket closed"))
                se.printStackTrace();
        } catch (Exception e) {
            if (e.getMessage().equals("client.listener_references.ServerConnection reset")) {
                System.out.println("Server terminated connection");
                close();
            } else { e.printStackTrace(); }
        }
        close();
    }

    /**
     * Attempts to encrypt the data and wrap it in an {@link JSONObject}
     *
     * @param json       data to be encrypted
     * @param publicKey  the {@link PublicKey} used for encryption
     * @param privateKey the {@link PrivateKey} used to sign the encryption
     * @return           the completed {@link JSONObject}
     */
    private static JSONObject generateEncryptionPacket(JSONObject json, PublicKey publicKey, PrivateKey privateKey) {
        byte[] iv = new byte[SecuredGCMUsage.IV_SIZE];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv);
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(SecuredGCMUsage.TAG_BIT_LENGTH, iv);
        JSONObject encryptedJson = encrypt(json, publicKey, privateKey, gcmParamSpec, "eco.echotrace.77".getBytes());
        if (encryptedJson == null) return null;
        return encryptedJson;
    }

    /**
     * Attempts to decrypt the encryption packet back into the original content
     *
     * @param packet     the {@link JSONObject} to be decrypted
     * @param publicKey  the {@link PublicKey} of the connected server
     * @param privateKey the {@link PrivateKey} used for validating the author
     * @return           the original decrypted data
     * @throws Exception if something went wrong internally during the encryption process
     */
    private static JSONObject decryptEncryptionPacket(JSONObject packet, PublicKey publicKey, PrivateKey privateKey) throws Exception {
        return decrypt(packet, publicKey, privateKey, "eco.echotrace.77".getBytes());
    }

    /**
     * Attempts to encrypt and send the {@code text} to the server
     * that the client is currently connected to
     *
     * @param   text the {@code String} to be sent to the server
     * @throws  ClientException if something went wrong while trying
     *          to send the message
     * @throws  IllegalArgumentException if {@code text} is null
     */
    public void sendText(String text) throws ClientException {
        send(new JSONObject().put("text", text), TEXT);
    }

    /**
     * Attempts to format, encrypt, and send the {@code command} and
     * {@code arguments} to the server that the client is currently
     * connected to
     *
     * @param   command   the command to be sent to the server
     * @param   arguments the command args associated with the command
     * @throws  ClientException if something went wrong while trying
     *          to send the message
     */
    public void sendCommand(String command, String arguments) throws ClientException {
        send(new JSONObject().put("command", command).put("arguments", arguments), PacketType.COMMAND);
    }

    public void sendJSON(JSONObject json) throws ClientException {
        send(json, PacketType.JSON);
    }

    private void send(JSONObject json, PacketType type) throws ClientException {
        if (json == null || json.isEmpty()) throw new IllegalArgumentException("Cannot send empty data");
        if (serverPublicKey == null) throw new ClientException("Failed to encrypt data: server's public async encryption key does not exist");
        if (outgoing == null) throw new ClientException("Failed to send data: no secure connection to the server exists");
        JSONObject packet = generateEncryptionPacket(json, serverPublicKey, clientKeys.getPrivate());
        if (packet == null || packet.isEmpty()) throw new ClientException("Failed to encrypt data: could not generate encryption packet");
        packet.put("type", type);
        outgoing.println(Base64.encodeBase64String(packet.toString().getBytes()));
    }

    private byte[] parseStrByteArray(String a) {
        if (a == null) return null;
        String[] parsed = a.replaceAll("\\[", "").replaceAll("]", "")
                .replaceAll(" ", "").split(",");
        byte[] keyBytes = new byte[parsed.length];
        for (int b=0; b<parsed.length; b++) keyBytes[b] = Byte.valueOf(parsed[b]);
        return keyBytes;
    }

    /**
     * Attempts to gracefully-ish disconnect from the server that the client is connected to
     */
    @Override
    public void close() {
        try { sendCommand("sudo", "disconnect");
        } catch (ClientException ignore) { }
        if (!isOpen) return; isOpen = false;
        executorService.shutdownNow();
        connection = null;
        try { socket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
