package main;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Logger;

import static javafx.application.Application.launch;

/** Client side listener for Clype. */
public class ClientSideServerListener implements Runnable {
  private static final Logger LOGGER = Logger.getGlobal();
  private final ClypeClient client;

  private ClientSideServerListener(ClypeClient client) {
    this.client = client;
  }

  /** Creates a {@link ClientSideServerListener} instance of the provided client. */
  public static ClientSideServerListener of(ClypeClient client) {
    return new ClientSideServerListener(client);
  }

  @Override
  public void run() {
    try {
      ClientGUI GUI = new ClientGUI();
      GUI.initialize(client);
      LOGGER.info("Successfully started GUI instance. ");
      while (!client.getCloseConnection()) {
        GUI.render(client.getReceivedMessages());
        client.clearReceivedMessagesQueue();
      }
    } catch (Exception e) {
      LOGGER.severe(e.toString());
    }
  }
}
