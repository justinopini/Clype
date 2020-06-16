package main;

import data.ClypeData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClypeClient {
  private static final Logger LOGGER = Logger.getGlobal();
  private final String username;
  private final String hostName;
  private final int port;
  private final ArrayList<ClypeData<?>> sendQueue = new ArrayList<>();
  private final ArrayList<ClypeData<?>> receiveQueue = new ArrayList<>();
  private final ArrayList<String> activeUsers = new ArrayList<>();
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
    } catch (Exception e) {
      String anonymousUsername = UUID.randomUUID().toString();
      LOGGER.info(String.format("Defaulting to anonymous local session of username %s.", anonymousUsername));
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
        for (ClypeData<?> data : sendQueue) {
          sendData(data);
        }
        sendQueue.clear();
        receiveData().ifPresent(receiveQueue::add);
      }
      in.close();
      out.close();
    } catch (Exception e) {
      LOGGER.severe(e.toString());
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

  public Optional<ClypeData<?>> receiveData() {
    try {
      ClypeData<?> receivedData = (ClypeData<?>) this.in.readObject();
      if (receivedData.getType().equals(ClypeData.Type.LOG_OUT)) {
        closeConnection = true;
      } else if (receivedData.getType().equals(ClypeData.Type.LIST_USERS)) {
        activeUsers.clear();
        activeUsers.addAll(
            Arrays.stream(((String) receivedData.getData()).split(":"))
                .collect(Collectors.toList()));
      }
      return Optional.of(receivedData);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public boolean getCloseConnection() {
    return this.closeConnection;
  }

  public String getUsername() {
    return username;
  }

  public ArrayList<ClypeData<?>> getReceivedMessages() {
    return new ArrayList<>(receiveQueue);
  }

  public ArrayList<String> getActiveUsers() {
    return new ArrayList<>(activeUsers);
  }

  public void clearReceivedMessagesQueue() {
    receiveQueue.clear();
  }

  private void setUserNameOnStream(String userName) throws IOException {
    out.writeUTF(userName);
    out.flush();
  }
}
