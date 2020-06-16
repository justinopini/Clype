package main;

import data.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Pair;
import main.SchedulerUtils.TimerService;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class ClientGUI extends Application {
  // Conversions of supported emojis to it equivalent unicode form of the same index.
  private static final String[] TEXT_EMOJIS =
      new String[] {
        ":)", ";)", ":(", "B)", ":D", "D:", ":d", ";p", ":p", ":o", ":s", ":x", ":|", ":/", ":[",
        ":>", ":@", ":*", ":!", "o:)", ">:-o", ">:-)", ":3", "(y)", "(n)"
      };
  private static final String[] UNICODE_EMOJIS =
      new String[] {
        "\uD83D\uDE0A",
        "\uD83D\uDE09",
        "\uD83D\uDE1F",
        "\uD83D\uDE0E",
        "\uD83D\uDE03",
        "\uD83D\uDE29",
        "\uD83D\uDE0B",
        "\uD83D\uDE1C",
        "\uD83D\uDE1B",
        "\uD83D\uDE2E",
        "\uD83D\uDE16",
        "\uD83D\uDE36",
        "\uD83D\uDE10",
        "\uD83D\uDE15",
        "\uD83D\uDE33",
        "\uD83D\uDE0F",
        "\uD83D\uDE37",
        "\uD83D\uDE18",
        "\uD83D\uDE2C",
        "\uD83D\uDE07",
        "\uD83D\uDE20",
        "\uD83D\uDE08",
        "\uD83D\uDE3A",
        "\uD83D\uDC4D",
        "\uD83D\uDC4E"
      };
  private static final Logger LOGGER = Logger.getGlobal();
  private static final ArrayDeque<Pair<String, RenderedData>> rawMessages = new ArrayDeque<>();
  private static final ArrayList<String> authorizedRecipients = new ArrayList<>();
  private static final GridPane root = new GridPane();
  private static ClypeClient client;
  private static HashSet<String> usersList = new HashSet<>();

  private static String attachMedia(){
    String filePath = "";
    FileDialog fd = new FileDialog(new JFrame());
    fd.setVisible(true);
    File[] f = fd.getFiles();
    if (f.length > 0) {
      filePath += fd.getFiles()[0].getAbsolutePath();
      filePath = filePath.replace("\\", "/");
    }
    return filePath;
  }

  public static void main(String[] args) {
    new Thread(() -> launch(args)).start();
  }

  static void initialize(ClypeClient client) {
    ClientGUI.client = client;
    main(null);
  }

  /** Renders the received messages from other users. */
  static boolean render(List<ClypeData<?>> messages) throws GeneralSecurityException, IOException {
    for (ClypeData<?> data : messages){
        if (!data.getSender().equals(client.getUsername())){
            try{
                RenderedData renderedData = renderData(data);
                rawMessages.addLast(new Pair<>(data.getSender(), renderedData));
            } catch (LogoutException e){
                return false;
            }

        }
    }
    return true;
  }

  /** PReturns a {@link RenderedData} provided the given {@link ClypeData}.*/
    private static RenderedData renderData(ClypeData<?> data) throws GeneralSecurityException, IOException, LogoutException {
        switch (data.getType()){
            case MESSAGE -> {
                Label messageReceived = new Label();
                String receivedData = (String) data.getData();
                for (int emojiIndex = 0;emojiIndex < TEXT_EMOJIS.length;emojiIndex++) {
                    receivedData = receivedData.replace(TEXT_EMOJIS[emojiIndex], UNICODE_EMOJIS[emojiIndex]);
                }
                messageReceived.setText(receivedData);
                messageReceived.setWrapText(true);
                messageReceived.setTextAlignment(TextAlignment.JUSTIFY);
                return new RenderedData(messageReceived, messageReceived.getHeight() + 10);
            }
            case IMAGE ->  {
                ImageView im = new ImageView();
                Image image = new Image(new ByteArrayInputStream(((PictureCypeData.ClypeImage)data.getData()).image));
                double scaling = image.getHeight() / 250;
                im.setFitHeight(image.getHeight() / scaling);
                im.setFitWidth(image.getWidth() / scaling);
                im.setImage(image);
                return new RenderedData(im, im.getFitHeight() + 10);
            }
            case VIDEO ->  {
                File nf = new File("video_temp_location_for_memory.mp4");
                FileOutputStream fw = new FileOutputStream(nf);
                fw.write(((VideoClypeData.ClypeVideo)data.getData()).video);
                fw.flush();
                fw.close();
                MediaPlayer player = new MediaPlayer(
                        new Media(
                                "file:/"
                                        + Paths.get(".").toAbsolutePath().normalize().toString().replace("\\", "/")
                                        + "/video_temp_location_for_memory.mp4"));
                MediaControl mediaControl = new MediaControl(player);
                mediaControl.mediaView.setFitHeight(250);
                mediaControl.mediaView.setFitHeight(250);
                mediaControl.setMaxHeight(250);
                mediaControl.setMaxWidth(250);
                return new RenderedData(mediaControl,mediaControl.getMaxHeight() + 10);
            }
            case FILE -> {
                FileClypeData.ClypeFile receivedData = (FileClypeData.ClypeFile) data.getData();
                receivedData.writeFileContents();
                Label messageReceived = new Label();
                messageReceived.setText(String.format("File received and written to %s", receivedData.getFileName()));
                messageReceived.setWrapText(true);
                messageReceived.setTextAlignment(TextAlignment.JUSTIFY);
                return new RenderedData(messageReceived, messageReceived.getHeight() + 10);
            }
            case LIST_USERS -> { usersList = client.getActiveUsers(); }
            case LOG_OUT -> {throw new LogoutException();}
            default -> throw new IllegalStateException("Unexpected value: " + data.getType());
        }
        // Exhaustive switch.
        throw new LogoutException();
    }

  @Override
  public void start(Stage primaryStage) {
      primaryStage.setTitle("Multi-session user selection");
      VBox rootVBox = new VBox();
      ListView<String> userView = new ListView<>();
      ObservableList<String> observableUsersList = FXCollections.observableList(List.copyOf(usersList));
      userView.setItems(observableUsersList);
      Label info = new Label("Users list updates every 10 seconds");
      TimerService service = TimerService.getNewTimeService(10);
      service.setOnSucceeded(
          t -> {
              if (observableUsersList.size() > 0){
                observableUsersList.clear();
              }
              observableUsersList.addAll(usersList);
              userView.setItems(observableUsersList);
          });
      service.start();
      userView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      userView
          .getSelectionModel()
          .getSelectedItems()
          .addListener((ListChangeListener<String>) c -> {});

      Button launchGUI = new Button("Launch Session");
      launchGUI.setOnAction(
          ae -> {
            authorizedRecipients.clear();
            authorizedRecipients.addAll(userView.getSelectionModel().getSelectedItems());
            authorizedRecipients.add(client.getUsername());
            PrimaryStage stage = new PrimaryStage();
            stage.sizeToScene();
            stage.show();
          });
      rootVBox.getChildren().add(info);
      rootVBox.getChildren().add(userView);
      rootVBox.getChildren().add(launchGUI);
      Scene scene = new Scene(rootVBox, 400, 400);
      primaryStage.setScene(scene);
      primaryStage.show();
  }

  /** A piece of {@link ClypeData} in JavaFX {@link Node} format and its sizing properties. */
  private static class RenderedData{
      private final Node node;
      private final double height;

      private RenderedData(Node node, double height) {
          this.node = node;
          this.height = height;
      }
  }

  /** Exception when the user has terminated the connection. */
  private static class LogoutException extends Exception{}

  private class PrimaryStage extends Stage {
    PrimaryStage() {
      super();
        HBox messageString = new HBox();
        VBox messagesSent = new VBox();
        VBox messagesReceived = new VBox();
        VBox online = new VBox();
        Label whoIsOnline = new Label("Friends online:");
        messagesReceived.getStyleClass().add("seperate");
        messagesSent.getStyleClass().add("seperate");
        messageString.getChildren().add(messagesReceived);
        messageString.getChildren().add(messagesSent);
        VBox buttons = new VBox();
        ListView<String> users = new ListView<>();
        ObservableList<String> list = FXCollections.observableArrayList();
        users.setItems(list);
        ObservableList<String> observableUsersList = FXCollections.observableList(List.copyOf(usersList));
        users.setItems(observableUsersList);

        // Lambda variables should be effectively final hence one element array.
        final int[] height = {0};
        TimerService service = TimerService.getNewTimeService(5);
        service.setOnSucceeded(
                t -> {
                    while (!rawMessages.isEmpty()) {
                        Pair<String, RenderedData> pair  = rawMessages.removeFirst();
                        LOGGER.info(String.format("Message from %s received", pair.getKey()));
                        // Clear up some space if we need it.
                        if (height[0] > 500) {
                            messagesSent.getChildren().remove(0);
                            messagesReceived.getChildren().remove(0);
                        }
                        Node node = pair.getValue().node;
                        if (pair.getKey().equals(client.getUsername())) {
                            node.getStyleClass().add("messageBubbleSent");
                            messagesSent.getChildren().add(node);
                            Label holder = new Label();
                            holder.getStyleClass().add("holder");
                            messagesReceived.getChildren().add(holder);
                        } else {
                            node.getStyleClass().add("messageBubbleReceived");
                            messagesReceived.getChildren().add(node);
                            Label holder = new Label();
                            holder.getStyleClass().add("holder");
                            messagesSent.getChildren().add(holder);
                        }
                        height[0] += pair.getValue().height + 120;
                    }
                    if (observableUsersList.size() > 0){
                        observableUsersList.clear();
                    }
                    observableUsersList.addAll(usersList);
                    users.setItems(observableUsersList);
                });
        service.start();

        HBox userInterface = new HBox();
        HBox displayedInfo = new HBox();
        TextField message = new TextField();
        message.setPromptText("Enter message here");
        Button sendBtn = new Button("Send");
        sendBtn.setOnAction(
                ae -> {
                  try {
                      MessageClypeData data = new MessageClypeData(client.getUsername(), authorizedRecipients, message.getText());
                      client.sendData(data);
                      rawMessages.addLast(new Pair<>(client.getUsername(), renderData(data)));
                  } catch (GeneralSecurityException | IOException | LogoutException e) {
                    LOGGER.severe(e.toString());
                  }
                  message.clear();
                });

        Button attachPicture = new Button("Attach Picture");
        Button attachVideo = new Button("Attach Video");
        Button attachFile = new Button("Attach file");
        attachPicture.setOnAction(
                ae -> {
                  String fieldPath = attachMedia();
                  if (fieldPath.length() > 1){
                    try {
                        PictureCypeData data = new PictureCypeData(client.getUsername(), authorizedRecipients, fieldPath);
                        client.sendData(data);
                        rawMessages.addLast(new Pair<>(client.getUsername(), renderData(data)));
                    } catch (GeneralSecurityException | IOException | LogoutException e) {
                        LOGGER.severe(e.toString());
                    }
                  }
                });
        attachVideo.setOnAction(
                ae -> {
                  String fieldPath = attachMedia();
                  if (fieldPath.length() > 1){
                    try {
                        VideoClypeData data = new VideoClypeData(client.getUsername(), authorizedRecipients, fieldPath);
                        client.sendData(data);
                        rawMessages.addLast(new Pair<>(client.getUsername(), renderData(data)));
                    } catch (GeneralSecurityException | IOException | LogoutException e) {
                        LOGGER.severe(e.toString());
                    }
                  }
                });
        attachFile.setOnAction(
                ae -> {
                  String fieldPath = attachMedia();
                  if (fieldPath.length() > 1){
                    try {
                        FileClypeData data = new FileClypeData(client.getUsername(), authorizedRecipients, fieldPath);
                        client.sendData(data);
                        rawMessages.addLast(new Pair<>(client.getUsername(), renderData(data)));
                    } catch (GeneralSecurityException | IOException | LogoutException e) {
                        LOGGER.severe(e.toString());
                    }
                  }
                });

        //==============================================================================================================
        online.getChildren().add(whoIsOnline);
        online.getChildren().add(users);
        root.getStyleClass().add("maintheme");
        root.getStyleClass().add("text");
        message.getStyleClass().add("messabeBox");
        sendBtn.getStyleClass().add("button");
        attachPicture.getStyleClass().add("button");
        attachVideo.getStyleClass().add("button");
        attachFile.getStyleClass().add("button");
        buttons.getChildren().add(sendBtn);
        buttons.getChildren().add(attachPicture);
        buttons.getChildren().add(attachVideo);
        buttons.getChildren().add(attachFile);
        buttons.getStyleClass().add("buttons");
        userInterface.getChildren().add(message);
        userInterface.getChildren().add(buttons);
        userInterface.getStyleClass().add("userInterface");
        messageString.getStyleClass().add("messageString");
        displayedInfo.getChildren().add(messageString);
        displayedInfo.getChildren().add(online);
        displayedInfo.getStyleClass().add("box");
        root.add(displayedInfo, 0, 0);
        root.add(userInterface, 0, 1);
        //==============================================================================================================

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
        super.setTitle("Clype : Clarkson Skype");
        super.setScene(scene);
        super.show();
    }
  }
}
