package main;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

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
    ClientGUI gui = new ClientGUI();
    gui.initialize(client);
    LOGGER.info("Successfully started GUI instance. ");
    boolean continueRender = true;
    while (!client.getCloseConnection() && continueRender) {
      try {
        continueRender = gui.render(client.getReceivedMessages());
      } catch (GeneralSecurityException | IOException e) {
        LOGGER.severe(e.toString());
      }
    }
  }
}
