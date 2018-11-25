package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cryptography.HybridCryptography;
import cryptography.SecuredGCMUsage;
import listener_references.ClientConnection;
import listener_references.Command;
import listener_references.Message;
import listeners.CommandListener;
import listeners.MessageListener;
import org.apache.commons.codec.binary.Base64;
import packets.CommandPacket;
import packets.EncryptionPacket;

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

public class TcpClient implements AutoCloseable, Runnable {

    private int port;
    private Socket socket;
    private String address;
    private boolean isOpen;
    private final Gson gson;
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
        gson = new GsonBuilder().setLenient().disableHtmlEscaping().serializeNulls().create();
    }

    /**
     * Creates a new {@link TcpClient} bound to the specified host and port
     *
     * @param host               the address of the server
     * @param port               the port the client should try to connect to the server through
     * @param connectImmediately true if you want the client to automatically connect to the server
     * @throws ClientException
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
        gson = new GsonBuilder().setLenient().disableHtmlEscaping().serializeNulls().create();

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
     * @throws  NoSuchAlgorithmException
     * @throws  InvalidKeySpecException
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
    public void addMessageListener(MessageListener listener) {
        listenerManager.addMessageListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeMessageListener(MessageListener listener) {
        listenerManager.removeMessageListener(listener);
    }
    @SuppressWarnings("unused")
    public void addCommandListener(CommandListener listener) {
        listenerManager.addCommandListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeCommandListener(CommandListener listener) {
        listenerManager.removeCommandListener(listener);
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
                String json = new String(Base64.decodeBase64(received));
                EncryptionPacket packet = gson.fromJson(json, EncryptionPacket.class);
                String message = decryptEncryptionPacket(packet, serverPublicKey, clientKeys.getPrivate());

                switch (packet.getPayloadType()) {
                    case TEXT:
                        listenerManager.raiseMessageEvent(new Message(connection, message));
                        break;
                    case COMMAND:
                        CommandPacket cPacket = gson.fromJson(message, CommandPacket.class);
                        listenerManager.raiseCommandEvent(new Command(connection, cPacket));
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
     * Attempts to encrypt the data and wrap it in an {@link EncryptionPacket}
     *
     * @param message    data to be encrypted
     * @param packetType what the encrypted data represents
     * @param publicKey  the {@link PublicKey} used for encryption
     * @param privateKey the {@link PrivateKey} used to sign the encryption
     * @return           the completed {@link EncryptionPacket}
     */
    private static EncryptionPacket generateEncryptionPacket(String message, EncryptionPacket.PacketType packetType, PublicKey publicKey, PrivateKey privateKey) {
        byte iv[] = new byte[SecuredGCMUsage.IV_SIZE];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv);
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(SecuredGCMUsage.TAG_BIT_LENGTH, iv);
        String[] encryptedText = encrypt(message, publicKey, privateKey, gcmParamSpec, "eco.echotrace.77".getBytes());
        if (encryptedText == null || encryptedText.length != 2) return null;
        return new EncryptionPacket(encryptedText[1], packetType, gcmParamSpec, encryptedText[0]);
    }

    /**
     * Attempts to decrypt the encryption packet back into the original content
     *
     * @param packet     the {@link EncryptionPacket} to be decrypted
     * @param publicKey  the {@link PublicKey} of the connected server
     * @param privateKey the {@link PrivateKey} used for validating the author
     * @return           the original decrypted data
     * @throws Exception
     */
    private static String decryptEncryptionPacket(EncryptionPacket packet, PublicKey publicKey, PrivateKey privateKey) throws Exception {
        return decrypt(packet, publicKey, privateKey, "eco.echotrace.77".getBytes());
    }

    /**
     * Attempts to encrypt and send the {@code data} to the server
     * that the client is currently connected to
     *
     * @param   data the {@code String} to be sent to the server
     * @throws  ClientException if something went wrong while trying
     *          to send the message
     * @throws  IllegalArgumentException if {@code data} is null
     */
    public void sendText(String data) throws ClientException {
        if (data == null) throw new IllegalArgumentException("Data cannot be null");
        if (serverPublicKey == null) throw new ClientException("Failed to encrypt data: server's public async encryption key does not exist");
        if (outgoing == null) throw new ClientException("Failed to send data: no secure connection to the server exists");
        EncryptionPacket packet = generateEncryptionPacket(data, EncryptionPacket.PacketType.TEXT, serverPublicKey, clientKeys.getPrivate());
        if (packet == null) throw new ClientException("Failed to encrypt data: could not generate encryption packet");
        outgoing.println(Base64.encodeBase64String(gson.toJson(packet).getBytes()));
    }

    /**
     * Attempts to format, encrypt, and send the {@code command} and
     * {@code arguments} to the server that the client is currently
     * connected to
     *
     * @param   command
     * @param   arguments
     * @throws  ClientException if something went wrong while trying
     *          to send the message
     */
    public void sendCommand(String command, String arguments) throws ClientException {
        //if (command == null) throw new IllegalArgumentException("client.listener_references.Command cannot be null");
        if (serverPublicKey == null) throw new ClientException("Failed to encrypt data: server's public async encryption key does not exist");
        if (outgoing == null) throw new ClientException("Failed to send data: no secure connection to the server exists");
        String data = gson.toJson(new CommandPacket(command, arguments));
        EncryptionPacket packet = generateEncryptionPacket(data, EncryptionPacket.PacketType.COMMAND, serverPublicKey, clientKeys.getPrivate());
        if (packet == null) throw new ClientException("Failed to encrypt data: could not generate encryption packet");
        outgoing.println(Base64.encodeBase64String(gson.toJson(packet).getBytes()));
    }

    private byte[] parseStrByteArray(String a) {
        if (a == null) return null;
        String[] parsed = a.replaceFirst("\\[", "").replaceFirst("]", "").trim().split(", ");
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
