package main;

import data.ClypeData;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClypeClient {
  private static final Logger LOGGER = Logger.getGlobal();
  private final String username;
  private final String hostName;
  private final int port;
  private final ArrayDeque<ClypeData<?>> sendQueue = new ArrayDeque<>();
  private final ArrayDeque<ClypeData<?>> receiveQueue = new ArrayDeque<>();
  private final HashSet<String> activeUsers = new HashSet<>();
  private boolean closeConnection;
  private ObjectInputStream in;
  private ObjectOutputStream out;

  public ClypeClient(String username, String hostName, int port) {
    if (username == null || hostName == null || port < 1024) {
      throw new IllegalArgumentException("Username/Hostname is null or port number is below 1024");
    }
    this.username = username;
    this.hostName = hostName;
    this.port = port;
    this.closeConnection = false;
    this.in = null;
    this.out = null;
  }

  public ClypeClient(String username, String hostName) {
    this(username, hostName, 5000);
  }

  public ClypeClient(String username) {
    this(username, "localhost");
  }

  public static void main(String[] args) {
    ClypeClient client;
    try {
      String input = args[0];
      String[] userArgs = input.split("@");
      if (userArgs.length == 1) client = new ClypeClient(userArgs[0]);
      else {
        String[] hostArgs = userArgs[1].split(":");
        if (hostArgs.length == 1) {
          client = new ClypeClient(userArgs[0], hostArgs[0]);
        } else {
          client = new ClypeClient(userArgs[0], hostArgs[0], Integer.parseInt(hostArgs[1]));
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      String anonymousUsername = UUID.randomUUID().toString();
      LOGGER.info(
          String.format(
              "Defaulting to anonymous local session of username %s.", anonymousUsername));
      client = new ClypeClient(anonymousUsername);
    }
    client.start();
  }

  public void start() {
    try (Socket skt = new Socket(hostName, port)) {
      LOGGER.info(skt.toString());
      in = new ObjectInputStream(skt.getInputStream());
      out = new ObjectOutputStream(skt.getOutputStream());
      setUserNameOnStream(username);
      new Thread(ClientSideServerListener.of(this)).start();
      LOGGER.info(String.format("Client %s has successfully connected.", username));
      while (!closeConnection) {
        receiveData();
        while (!sendQueue.isEmpty()) {
          sendData(sendQueue.removeFirst());
        }
      }
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendData(ClypeData<?> data) {
    try {
      out.writeObject(data);
      out.flush();
    } catch (IOException ioe) {
      LOGGER.severe(ioe.getMessage());
    }
  }

  public void receiveData() {
    try {
      ClypeData<?> receivedData = (ClypeData<?>) this.in.readObject();
      if (receivedData.getType().equals(ClypeData.Type.LOG_OUT)) {
        closeConnection();
      } else if (receivedData.getType().equals(ClypeData.Type.LIST_USERS)) {
        activeUsers.clear();
        activeUsers.addAll(
            Arrays.stream(((String) receivedData.getData()).split(":"))
                .collect(Collectors.toSet()));
        LOGGER.info(String.format("Current users : %s", activeUsers.toString()));
      } else {
        receiveQueue.addLast(receivedData);
      }
    } catch (EOFException e) {
      closeConnection();
    } catch (IOException | ClassNotFoundException | GeneralSecurityException e) {
      LOGGER.severe(e.toString());
    }
  }

  public boolean getCloseConnection() {
    return this.closeConnection;
  }

  public String getUsername() {
    return username;
  }

  public ArrayList<ClypeData<?>> getReceivedMessages() {
    ArrayList<ClypeData<?>> received = new ArrayList<>(receiveQueue);
    receiveQueue.clear();
    return received;
  }

  public HashSet<String> getActiveUsers() {
    return activeUsers;
  }

  private void setUserNameOnStream(String userName) throws IOException {
    out.writeUTF(userName);
    out.flush();
  }

  private void closeConnection() {
    closeConnection = true;
  }
}
