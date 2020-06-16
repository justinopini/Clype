package main;

import data.ClypeData;
import data.MessageClypeData;
import data.PictureCypeData;
import data.VideoClypeData;
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
import main.SchedulerUtils.TimerService;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
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
  private static ClypeClient client;
  private final Map<String, Node> rawMessages = new HashMap<>();
  private final ArrayList<String> authorizedSenders = new ArrayList<>();
  private final int[] height = {0};
  private final GridPane root = new GridPane();
  private ArrayList<String> usersList = new ArrayList<>();
  private int messageIndex;

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

  void initialize(ClypeClient client) {
    main(null);
    ClientGUI.client = client;
  }

  /** Renders the received messages from other users. */
  void render(List<ClypeData<?>> messages) throws Exception {
    boolean guiCloseConnection = false;
    while (!client.getCloseConnection() && ! guiCloseConnection) {
      for (ClypeData<?> data : messages){
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
            height[0] += messageReceived.getHeight() + 10;
            rawMessages.put(data.getSender() + ":" + messageIndex, messageReceived);
            messageIndex++;
          }
          case IMAGE ->  {
            ImageView im = new ImageView();
            Image image = new Image(new ByteArrayInputStream(((PictureCypeData.ClypeImage)data.getData()).image));
            double scaling = image.getHeight() / 250;
            im.setFitHeight(image.getHeight() / scaling);
            im.setFitWidth(image.getWidth() / scaling);
            im.setImage(image);
            height[0] += im.getFitHeight() + 10;
            rawMessages.put(data.getSender() + ":" + messageIndex, im);
            messageIndex++;
          }
          case VIDEO ->  {
            File nf = new File("video_temp_location_for_memory.mp4");
            FileOutputStream fw = new FileOutputStream(nf);
            fw.write(((VideoClypeData.ClypeVideo)data.getData()).video);
            fw.flush();
            fw.close();
            MediaPlayer player =
            new MediaPlayer(
                new Media(
                    "file:/"
                        + Paths.get(".").toAbsolutePath().normalize().toString().replace("\\", "/")
                        + "/video_temp_location_for_memory.mp4"));
            MediaControl mediaControl = new MediaControl(player);
            mediaControl.mediaView.setFitHeight(250);
            mediaControl.mediaView.setFitHeight(250);
            mediaControl.setMaxHeight(250);
            mediaControl.setMaxWidth(250);
            height[0] += mediaControl.getMaxHeight() + 10;
            rawMessages.put(data.getSender() + ":" + messageIndex, mediaControl);
            messageIndex += 1;
          }
          case FILE -> {}
          case LIST_USERS -> { usersList = client.getActiveUsers(); }
          case LOG_OUT -> {guiCloseConnection = true;}
          default -> throw new IllegalStateException("Unexpected value: " + data.getType());
        }
      }
    }
  }

  @Override
  public void start(Stage primaryStage) {
    try {
      primaryStage.setTitle("Multi-session user selection");
      VBox rootVBox = new VBox();
      ListView<String> userView = new ListView<>();
      ObservableList<String> observableUsersList = FXCollections.observableList(usersList);
      userView.setItems(observableUsersList);

      TimerService service = TimerService.getNewTimeService(30);
      service.setOnSucceeded(
          t -> {
            try {
              observableUsersList.setAll(usersList);
              userView.setItems(observableUsersList);
            } catch (Exception e) {
              LOGGER.severe(e.getMessage());
            }
          });
      service.start();

      Label info = new Label("Users list updates every 10 seconds");
      Button launchGUI = new Button("Launch Session");

      userView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      userView
          .getSelectionModel()
          .getSelectedItems()
          .addListener((ListChangeListener<String>) c -> {});

      launchGUI.setOnAction(
          ae -> {
            authorizedSenders.clear();
            authorizedSenders.addAll(userView.getSelectionModel().getSelectedItems());
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
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
    }
  }

  private class PrimaryStage extends Stage {
    PrimaryStage() {
      super();
      try {
        final int[] messageIndexes = {0};
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
        ObservableList<String> observableUsersList = FXCollections.observableList(usersList);
        users.setItems(observableUsersList);

        TimerService service = TimerService.getNewTimeService(2);
        service.setOnSucceeded(
                t -> {
                  for (String user : rawMessages.keySet()) {
                    if (Integer.parseInt(user.split(":")[1]) > messageIndexes[0]) {
                      if (height[0] > 500) {
                        messagesSent.getChildren().remove(0);
                        messagesReceived.getChildren().remove(0);
                      }
                      Node todisplay = rawMessages.get(user);
                      if (user.split(":")[0].equals("")) {
                        todisplay.getStyleClass().add("messageBubbleSent");
                        messagesSent.getChildren().add(todisplay);
                        Label holder = new Label();
                        holder.getStyleClass().add("holder");
                        messagesReceived.getChildren().add(holder);
                      } else {
                        if (authorizedSenders.contains(user.split(":")[0])) {
                          todisplay.getStyleClass().add("messageBubbleRecieved");
                          messagesReceived.getChildren().add(todisplay);
                          Label holder = new Label();
                          holder.getStyleClass().add("holder");
                          messagesSent.getChildren().add(holder);
                        }
                      }
                      messageIndexes[0] += 1;
                      height[0] += 120;
                    }
                  }
                  observableUsersList.setAll(usersList);
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
                    client.sendData(new MessageClypeData(client.getUsername(), Collections.EMPTY_LIST, message.getText()));
                  } catch (Exception e) {
                    LOGGER.severe(e.getMessage());
                  }
                  message.clear();
                });

        Button attachPicture = new Button("Attach Picture");
        Button attachVideo = new Button("Attach Video");
        attachPicture.setOnAction(
                ae -> {
                  String fieldPath = attachMedia();
                  if (fieldPath.length() > 1){
                    try {
                      client.sendData(new PictureCypeData(client.getUsername(), Collections.EMPTY_LIST, fieldPath));
                    } catch (Exception e) {
                      LOGGER.severe(e.getMessage());
                    }
                  }
                });
        attachVideo.setOnAction(
                ae -> {
                  String fieldPath = attachMedia();
                  if (fieldPath.length() > 1){
                    try {
                      client.sendData(new VideoClypeData(client.getUsername(), Collections.EMPTY_LIST, fieldPath));
                    } catch (Exception e) {
                      LOGGER.severe(e.getMessage());
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
        buttons.getChildren().add(sendBtn);
        buttons.getChildren().add(attachPicture);
        buttons.getChildren().add(attachVideo);
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
      } catch (Exception e) {
        LOGGER.severe(e.getMessage());
      }
    }
  }
}
