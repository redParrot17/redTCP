package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cryptography.HybridCryptography;
import cryptography.SecuredGCMUsage;
import listener_references.Command;
import listener_references.Message;
import listener_references.ServerConnection;
import listeners.CommandListener;
import listeners.MessageListener;
import listeners.ServerConnectionListener;
import org.apache.commons.codec.binary.Base64;
import packets.CommandPacket;
import packets.EncryptionPacket;

import javax.crypto.spec.GCMParameterSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpServer implements AutoCloseable, Runnable {

    private static final Gson GSON = new GsonBuilder().setLenient().disableHtmlEscaping().serializeNulls().create();
    private ServerListenerManager listenerManager;
    private ExecutorService executorService;
    private ExecutorService threadPool;
    private ServerSocket serverSocket;
    private InetAddress inetAddress;
    private KeyPair serverKeys;
    private boolean alive;
    private int backlog;
    private int timeout;
    private int port;

    /**
     * @param port port number that the server is to connect to
     */
    public TcpServer(int port) {
        listenerManager = new ServerListenerManager();
        executorService = null;
        serverSocket = null;
        inetAddress = null;
        threadPool = null;
        serverKeys = null;
        this.port = port;
        this.timeout = 0;
        this.backlog = 0;
        alive = false;
    }

    /**
     * @param port      port number that the server is to connect to
     * @param timeout   how many milliseconds of zero activity until a client is automatically disconnected
     */
    public TcpServer(int port, int timeout) {
        listenerManager = new ServerListenerManager();
        this.timeout = timeout > 0 ? timeout : 0;
        executorService = null;
        serverSocket = null;
        inetAddress = null;
        threadPool = null;
        serverKeys = null;
        this.port = port;
        this.backlog = 0;
        alive = false;
    }

    /**
     * @param port      port number that the server is to connect to
     * @param timeout   how many milliseconds of zero activity until a client is automatically disconnected
     * @param backLog   how many connections are allowed
     */
    public TcpServer(int port, int timeout, int backLog) {
        listenerManager = new ServerListenerManager();
        this.timeout = timeout > 0 ? timeout : 0;
        executorService = null;
        this.backlog = backLog;
        serverSocket = null;
        inetAddress = null;
        threadPool = null;
        serverKeys = null;
        this.port = port;
        alive = false;
    }

    /**
     * @param port      port number that the server is to connect to
     * @param timeout   how many milliseconds of zero activity until a client is automatically disconnected
     * @param backLog   how many connections are allowed
     * @param bindAddr  the local InetAddress the server will bind to. Leave null if you want to use "localhost"
     */
    public TcpServer(int port, int timeout, int backLog, InetAddress bindAddr) {
        listenerManager = new ServerListenerManager();
        this.timeout = timeout > 0 ? timeout : 0;
        this.inetAddress = bindAddr;
        executorService = null;
        this.backlog = backLog;
        serverSocket = null;
        threadPool = null;
        serverKeys = null;
        this.port = port;
        alive = false;
    }

    /**
     * @param port              port number that the server is to connect to
     * @param timeout           how many milliseconds of zero activity until a client is automatically disconnected
     * @param backLog           how many connections are allowed
     * @param startImmediately  should the server immediately connect and start
     * @throws ServerException  if something went wrong during the startup process
     */
    public TcpServer(int port, int timeout, int backLog, boolean startImmediately) throws ServerException {
        listenerManager = new ServerListenerManager();
        this.timeout = timeout > 0 ? timeout : 0;
        executorService = null;
        this.backlog = backLog;
        serverSocket = null;
        inetAddress = null;
        threadPool = null;
        serverKeys = null;
        this.port = port;
        alive = false;

        if (startImmediately) start().join();
    }

    /**
     * @param port              port number that the server is to connect to
     * @param timeout           how many milliseconds of zero activity until a client is automatically disconnected
     * @param backLog           how many connections are allowed
     * @param startImmediately  should the server immediately connect and start
     * @param bindAddr          the local InetAddress the server will bind to. Leave null if you want to use "localhost"
     * @throws ServerException  if something went wrong during the startup process
     */
    public TcpServer(int port, int timeout, int backLog, InetAddress bindAddr, boolean startImmediately) throws ServerException {
        listenerManager = new ServerListenerManager();
        this.timeout = timeout > 0 ? timeout : 0;
        this.inetAddress = bindAddr;
        executorService = null;
        this.backlog = backLog;
        serverSocket = null;
        threadPool = null;
        serverKeys = null;
        this.port = port;
        alive = false;

        if (startImmediately) start().join();
    }

    /**
     * @return the {@link InetAddress} of the server
     */
    public InetAddress getSocketAddress() {
        return serverSocket.getInetAddress();
    }

    /**
     * Starts up the server if it is not already running
     *
     * @return an empty {@link CompletableFuture<Void>} when the server has finished starting up
     * @throws ServerException  if something goes wrong during startup
     */
    public CompletableFuture<Void> start() throws ServerException {
        if (alive) throw new ServerException("Server is already running");
        try { serverKeys = HybridCryptography.generateKeys();
        } catch (NoSuchAlgorithmException e) {
            throw new ServerException("Unable to generate async encryption keys: " + e.getMessage());
        }
        if (serverKeys == null) throw new ServerException("Failed to generate async encryption keys");
        try {
            serverSocket = new ServerSocket(port, backlog, inetAddress);
        } catch (IOException ioe) {
            throw new ServerException("Failed to create server: " + ioe.getMessage());
        }
        alive = true;
        if (backlog > 0) threadPool = Executors.newFixedThreadPool(backlog);
        else threadPool = Executors.newCachedThreadPool();
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this);
        return CompletableFuture.completedFuture(null);
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
    public void addConnectionListener(ServerConnectionListener listener) {
        listenerManager.addConnectionListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeConnectionListener(ServerConnectionListener listener) {
        listenerManager.removeConnectionListener(listener);
    }
    @SuppressWarnings("unused")
    public void removeAllListeners() {
        listenerManager.removeAllListeners();
    }

    @Override
    public void run() {
        try {
            while (alive) {
                ClientConnection connection = new ClientConnection(this, serverSocket.accept(), timeout);
                try { threadPool.execute(connection);
                } catch (Exception e) {
                    connection.close();
                    if (!e.getMessage().equals("Socket is closed"))
                        e.printStackTrace();
                }
            }
        } catch (IOException e) {
            if (!e.getMessage().equals("socket closed"))
                e.printStackTrace();
        }
        close();
    }

    /**
     * Attempts to encrypt the data and wrap it in an {@link EncryptionPacket}
     *
     * @param message    data to be encrypted
     * @param packetType what the encrypted data represents
     * @param publicKey  the {@link PublicKey} used for encryption
     * @return           the completed {@link EncryptionPacket}
     */
    private EncryptionPacket generateEncryptionPacket(String message, EncryptionPacket.PacketType packetType, PublicKey publicKey) {
        byte iv[] = new byte[SecuredGCMUsage.IV_SIZE];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv);
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(SecuredGCMUsage.TAG_BIT_LENGTH, iv);
        String[] encryptedText = HybridCryptography.encrypt(message, publicKey, serverKeys.getPrivate(), gcmParamSpec, "eco.echotrace.77".getBytes());
        if (encryptedText == null || encryptedText.length != 2) return null;
        return new EncryptionPacket(encryptedText[1], packetType, gcmParamSpec, encryptedText[0]);
    }

    /**
     * Attempts to decrypt the encryption packet back into the original content
     *
     * @param packet     the {@link EncryptionPacket} to be decrypted
     * @param publicKey  the {@link PublicKey} of the client that sent the packet
     * @return           the original decrypted data
     * @throws Exception if something went wrong internally with the decryption process
     */
    private String decryptEncryptionPacket(EncryptionPacket packet, PublicKey publicKey) throws Exception {
        return HybridCryptography.decrypt(packet, publicKey, serverKeys.getPrivate(), "eco.echotrace.77".getBytes());
    }

    /**
     * Attempts to decrypt the encryption packet back into the original content
     *
     * @param json       json data to be decrypted
     * @param publicKey  the {@link PublicKey} of the client that sent the packet
     * @throws Exception if something went wrong internally with the encryption process
     */
    private String decryptEncryptionPacket(String json, PublicKey publicKey) throws Exception {
        EncryptionPacket packet = GSON.fromJson(json, EncryptionPacket.class);
        return HybridCryptography.decrypt(packet, publicKey, serverKeys.getPrivate(), "eco.echotrace.77".getBytes());
    }

    /**
     * Sends a simple message to the client containing {@code data}
     *
     * @param outgoing  {@link PrintWriter} used for sending messages to the client
     * @param key       {@link PublicKey} of the client used to encrypt the data
     * @param data      {@link String} data to be sent to the client
     */
    public void sendText(PrintWriter outgoing, PublicKey key, String data) {
        if (key == null || data == null || outgoing == null || !alive) return;
        EncryptionPacket packet = generateEncryptionPacket(data, EncryptionPacket.PacketType.TEXT, key);
        String json = GSON.toJson(packet);
        if (packet != null) outgoing.println(Base64.encodeBase64String(json.getBytes()));
    }

    /**
     * Sends a command to the client
     *
     * @param outgoing  {@link PrintWriter} used to communicate to the client
     * @param key       {@link PublicKey} of the client used to encrypt the data
     * @param command   the command to be sent
     * @param arguments the command arguments
     */
    public void sendCommand(PrintWriter outgoing, PublicKey key, String command, String arguments) {
        Objects.requireNonNull(outgoing);
        Objects.requireNonNull(key);
        Objects.requireNonNull(command);
        Objects.requireNonNull(arguments);
        String data = GSON.toJson(new CommandPacket(command, arguments));
        EncryptionPacket packet = generateEncryptionPacket(data, EncryptionPacket.PacketType.COMMAND, key);
        String json = GSON.toJson(packet);
        if (packet != null) outgoing.println(Base64.encodeBase64String(json.getBytes()));
    }

    /**
     * Performs a handshake with the client to swap asymmetric public keys and
     * ensure the keys were received.
     *
     * @param   incoming    the BufferedReader representing the input stream of
     *                      the socket the client is connected through.
     * @param   outgoing    The PrintWriter representing the output stream of
     *                      the socket the client is connected through.
     * @return  The PublicKey of the client if the full handshake was
     *          successful, or null if the handshake was unsuccessful.
     */
    private CompletableFuture<PublicKey> exchangePublicKeys(BufferedReader incoming, PrintWriter outgoing) throws ServerException {
        Objects.requireNonNull(incoming,"\"incoming\" cannot be null" );
        Objects.requireNonNull(outgoing, "\"outgoing\" cannot be null");
        String confirmation;
        byte[] keyBytes;
        String message;
        PublicKey publicKey;

        try { // receive client public key
            String firstMessage = incoming.readLine();
            if (firstMessage == null) return null;
            keyBytes = parseStrByteArray(firstMessage);
        } catch (IOException ioe) {
            throw new ServerException("Client connection failure while exchanging public async keys: " + ioe.getMessage());
        }
        // verify that the key's bytes exist before attempting to process them
        if (keyBytes == null || keyBytes.length <= 0)
            throw new ServerException("Unable to retrieve client's public async encryption key");
        try { // construct the public key from the received message
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ServerException("Failed to construct client's public async encryption key: " + e.getMessage());
        }
        if (publicKey == null) // make sure that the key actually exists
            throw new ServerException("Failed to construct client's public async encryption key");
        // give the client the server public key
        outgoing.println(Arrays.toString(serverKeys.getPublic().getEncoded()));
        try { // await confirmation that the client received the server's public encryption key
            confirmation = incoming.readLine();
            if (confirmation == null) throw new ServerException("Failed to retrieve confirmation from the client after sending the server's public key");
        } catch (IOException e) {
            throw new ServerException("Communication failure while obtaining confirmation from the client that the server's public key was received: " + e.getMessage());
        }
        try { message = decryptEncryptionPacket(new String(Base64.decodeBase64(confirmation)), publicKey);
        } catch (Exception e) {
            throw new ServerException("Failed to decrypt confirmation message from the client: " + e.getMessage());
        }
        if (message.equals("handshake")) return CompletableFuture.completedFuture(publicKey);
        else throw new ServerException("Received invalid confirmation that the client received the server's public key");
    }

    private byte[] parseStrByteArray(String a) {
        if (a == null) return null;
        String[] parsed = a.replaceFirst("\\[", "").replaceFirst("]", "").trim().split(", ");
        byte[] keyBytes = new byte[parsed.length];
        for (int b=0; b<parsed.length; b++) keyBytes[b] = Byte.valueOf(parsed[b]);
        return keyBytes;
    }

    /**
     * Attempts to gracefully-ish shutdown the server and disconnect all existing client connections
     */
    @Override
    public void close() {
        if (!alive) return; alive = false;
        listenerManager.removeAllListeners();
        executorService.shutdownNow();
        threadPool.shutdownNow();
        try { serverSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public class ClientConnection implements Runnable, AutoCloseable {

        private PublicKey clientPublicKey;
        private BufferedReader incoming;
        private ServerConnection connection;
        private PrintWriter outgoing;
        private TcpServer server;
        private Socket socket;
        private int timeout;

        ClientConnection(TcpServer server, Socket socket, int timeout) throws IOException {
            this.incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outgoing = new PrintWriter(socket.getOutputStream(), true);
            this.timeout = timeout < 0 ? 0 : timeout;
            this.clientPublicKey = null;
            this.server = server;
            this.socket = socket;
            connection = null;
        }

        @Override
        public void run() {
            try {
                //Logger.log("client", socket.getInetAddress().getHostAddress(), "handshake", "client requesting handshake");
                socket.setSoTimeout(5000);
                clientPublicKey = Objects.requireNonNull(exchangePublicKeys(incoming, outgoing), "client public key was null").get(2, TimeUnit.MINUTES);
                if (clientPublicKey == null) {
                    //Logger.log("client", socket.getInetAddress().getHostAddress(), "handshake", "client failed to complete handshake");
                    close();
                    return;
                }
                socket.setSoTimeout(timeout);
                socket.setKeepAlive(true);
                //Logger.log("client", socket.getInetAddress().getHostAddress(), "handshake", "client completed handshake");
                connection = new ServerConnection(server, socket, clientPublicKey, outgoing);
                listenerManager.raiseConnectionEvent(connection, ServerConnection.Event.CONNECTED);
                while (!socket.isClosed()) {
                    String received = incoming.readLine();
                    if (received == null) return;

                    String json = new String(Base64.decodeBase64(received));
                    EncryptionPacket packet = GSON.fromJson(json, EncryptionPacket.class);
                    String message = decryptEncryptionPacket(packet, clientPublicKey);

                    switch (packet.getPayloadType()) {
                        case TEXT:
                            listenerManager.raiseMessageEvent(new Message(connection, message));
                            break;
                        case COMMAND:
                            CommandPacket cPacket = GSON.fromJson(message, CommandPacket.class);
                            if (cPacket.getCommand().equals("sudo")) {
                                if (cPacket.getArguments().equals("disconnect")) {
                                    socket.close();
                                    break;
                                }
                            } else {
                                listenerManager.raiseCommandEvent(new Command(connection, cPacket));
                            }
                            break;
                    }

                }
            } catch (SocketTimeoutException e) {
                //System.out.println("[SOCKET][" + socket.getInetAddress().getHostAddress() + "](DISCONNECTED) socket connection timed out");
            } catch (SocketException e) {
                if (!(e.getMessage().equals("ServerConnection reset")) && !(e.getMessage().equals("Socket closed")))
                    e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try { close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            listenerManager.raiseConnectionEvent(connection, ServerConnection.Event.REMOVED);
        }

        @Override
        public void close() {
            try { socket.close();
            } catch (IOException ignore) { }
            //Logger.log("socket", socket.getRemoteSocketAddress().toString(), "disconnected", "socket connection closed");
        }
    }

}
