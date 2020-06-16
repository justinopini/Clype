package main;

import data.ClypeData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

/** Caters to the information exchange between server and a single client of this instance. */
public class ServerSideClientIO implements Runnable {
  private static final Logger LOGGER = Logger.getGlobal();
  private final ClypeServer server;
  private final String usersName;
  private final ObjectInputStream in;
  private final ObjectOutputStream out;
  private final ArrayList<ClypeData<?>> sendQueue = new ArrayList<>();
  private boolean closeConnection = false;

  public ServerSideClientIO(
      ClypeServer server, String usersName, ObjectInputStream in, ObjectOutputStream out) {
    this.server = server;
    this.usersName = usersName;
    this.in = in;
    this.out = out;
  }

  @Override
  public void run() {
    while (!this.closeConnection) {
      receiveData().ifPresent(sendQueue::add);
      for (ClypeData<?> data : sendQueue) {
        if (data.getRecipients().isEmpty()) {
          server.broadcast(data);
        }else {
          server.broadcast(data, new HashSet<>(data.getRecipients()));
        }
      }
      sendQueue.clear();
    }
  }

  public Optional<ClypeData<?>> receiveData() {
    try {
      ClypeData<?> receivedData = (ClypeData<?>) in.readObject();
      if (receivedData.getType().equals(ClypeData.Type.LOG_OUT)) {
        server.remove(usersName);
        closeConnection = true;
      }
      return Optional.of(receivedData);
    } catch (IOException | ClassNotFoundException e) {
      return Optional.empty();
    }
  }

  public void sendData(ClypeData<?> dataToSendToClient) {
    try {
      out.writeObject(dataToSendToClient);
    } catch (IOException ioe) {
      LOGGER.severe(ioe.getMessage());
    }
  }
}
