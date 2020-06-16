package main;

import data.ClypeData;
import data.MessageClypeData;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Server that manages connections between Clype clients. */
public class ClypeServer {
  private static final int CONNECTION_WAIT = 10000;
  private static final Logger LOGGER = Logger.getGlobal();
  private final int port;
  // A map of users to their clients.
  private final Map<String, ServerSideClientIO> serverSideClientIOList = new HashMap<>();
  private boolean closeConnection = false;

  private ClypeServer(int port) {
    if (port < 1024) throw new IllegalArgumentException("port number is below 1024");
    this.port = port;
  }

  public ClypeServer() {
    this(5000);
  }

  public static void main(String[] args) throws IOException {
    ClypeServer server;
    try {
      server = new ClypeServer(Integer.parseInt(args[0]));
    } catch (ArrayIndexOutOfBoundsException e) {
      server = new ClypeServer();
    }
    server.start();
  }

  public void start() throws IOException {
    try (ServerSocket skt = new ServerSocket(port)) {
      LOGGER.info(skt.toString());
      LOGGER.info("Server has started successfully.");
      skt.setSoTimeout(CONNECTION_WAIT);
      while (!closeConnection) {
        try {
          updateCurrentUsers();
          Socket clientSocket = skt.accept();
          // Because of the order these are initialized in the client, out has to be before in.
          ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
          ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
          String username = in.readUTF();
          ServerSideClientIO serverSideClientIOElement =
                  new ServerSideClientIO(this, username, in, out);
          serverSideClientIOList.put(username, serverSideClientIOElement);
          new Thread(serverSideClientIOElement).start();
          LOGGER.info(String.format("%s has successfully connected to the server.", username));
        }
        catch (SocketTimeoutException ignored) {}
        catch (Exception e){
          LOGGER.severe(e.toString());
        }
      }
    }
  }

  /** Broadcast a piece of data to all users. */
  synchronized void broadcast(ClypeData<?> dataToBroadcastToClients) {
    broadcast(dataToBroadcastToClients, serverSideClientIOList.keySet());
  }

  /** Broadcast a piece of data to all the appropriate users specified. */
  synchronized void broadcast(ClypeData<?> dataToBroadcastToClients, Set<String> users) {
    for (ServerSideClientIO serverSideClient :
        serverSideClientIOList.entrySet().stream()
            .filter(e -> users.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList())) {
      serverSideClient.sendData(dataToBroadcastToClients);
    }
  }

  /** Closes the connection to a client that is currently being served. */
  synchronized void remove(String userName) {
    this.serverSideClientIOList.remove(userName);
    // Shut down the server if there are no more clients to serve.
    if (serverSideClientIOList.isEmpty()) {
      this.closeConnection = true;
    }
  }

  private void updateCurrentUsers()
          throws NoSuchPaddingException, ShortBufferException, NoSuchAlgorithmException,
          IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    String currentUsers = String.join(":", serverSideClientIOList.keySet());
    if (!serverSideClientIOList.isEmpty()) {
      broadcast(new MessageClypeData("anonymous", Collections.EMPTY_LIST, currentUsers));
    }
    LOGGER.info(
        String.format("There are %d active users currently. ", serverSideClientIOList.size()));
  }
}
