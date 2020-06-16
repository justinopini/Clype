package main;

import data.ClypeData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.logging.Logger;

/** Caters to the information exchange between server and a single client of this instance. */
public class ServerSideClientIO implements Runnable {
  private static final Logger LOGGER = Logger.getGlobal();
  private final ClypeServer server;
  private final ObjectInputStream in;
  private final ObjectOutputStream out;
  private final ArrayDeque<ClypeData<?>> sendQueue = new ArrayDeque<>();
  private boolean closeConnection = false;

  public ServerSideClientIO(
      ClypeServer server, ObjectInputStream in, ObjectOutputStream out) {
    this.server = server;
    this.in = in;
    this.out = out;
  }

  @Override
  public void run() {
    while (!this.closeConnection) {
      receiveData();
      while (!sendQueue.isEmpty()) {
        ClypeData<?> data =  sendQueue.removeFirst();
        if (data.getRecipients().isEmpty()) {
          server.broadcast(data);
        } else {
          server.broadcast(data, new HashSet<>(data.getRecipients()));
        }
      }
    }
  }

  private void receiveData() {
    try {
      ClypeData<?> receivedData = (ClypeData<?>) in.readObject();
      if (receivedData.getType().equals(ClypeData.Type.LOG_OUT)) {
        closeConnection();
      }
      sendQueue.addLast(receivedData);
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.severe(e.toString());
    }
  }

  public void sendData(ClypeData<?> dataToSendToClient) {
    try {
      out.writeObject(dataToSendToClient);
    } catch (SocketException e) {
      closeConnection();
    } catch (IOException ioe) {
      LOGGER.severe(ioe.toString());
    }
  }

  public boolean isClosed() {
    return closeConnection;
  }

  private void closeConnection() {
    closeConnection = true;
  }
}
