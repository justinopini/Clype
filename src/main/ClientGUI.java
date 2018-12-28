
package main;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.*;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 2.0
 */

public class ClientGUI extends Application{


    private static ClypeClient testClient = null;
    private GridPane root = new GridPane();
    private static Map<String,Node> rawMessages= new HashMap<String, Node>();
    private static int UID;
    private static final int[] tempHeight = {0};
    private static LinkedList<String> usersList = new LinkedList<String>();
    private static LinkedList<String> authorizedSenders = new LinkedList<>();

    private static void updateRecieved() throws IOException {
        while (!testClient.getCloseConnection()){
            if (testClient.getUserMessage() != null){
                Label messageRecieved = new Label();
                messageRecieved.setText(testClient.getUserMessage());
                messageRecieved.setWrapText(true);
                messageRecieved.setTextAlignment(TextAlignment.JUSTIFY);
                tempHeight[0] += messageRecieved.getHeight() + 10;
                rawMessages.put(testClient.getSenderUserName()+":"+UID,messageRecieved);
                testClient.clearMessage();
                UID+=1;
            }
            if (testClient.getPicute() != null){
                ImageView im =  new ImageView();
                Image image = new Image(new ByteArrayInputStream(testClient.getPicute()));
                double scaling = image.getHeight()/250;
                im.setFitHeight(image.getHeight()/scaling);
                im.setFitWidth(image.getWidth()/scaling);
                im.setImage(image);
                tempHeight[0] += im.getFitHeight() + 10 ;
                rawMessages.put(testClient.getSenderUserName()+":"+UID,im);
                testClient.clearPicture();
                UID+=1;

            }
            if (testClient.getVideo() != null){
                File nf = new File("video_temp_location_for_memory.mp4");
                FileOutputStream fw = new FileOutputStream(nf);
                fw.write(testClient.getVideo());
                fw.flush();
                fw.close();
                //mediacontrol got from cs242 moodle files
                MediaPlayer player = new MediaPlayer(new Media("file:/"+Paths.get(".").toAbsolutePath().normalize().toString().replace("\\","/")+"/video_temp_location_for_memory.mp4"));
                MediaControl mediaControl = new MediaControl(player);
                mediaControl.mediaView.setFitHeight(250);
                mediaControl.mediaView.setFitHeight(250);
                mediaControl.setMaxHeight(250);
                mediaControl.setMaxWidth(250);
                tempHeight[0] += mediaControl.getMaxHeight() + 10 ;
                rawMessages.put(testClient.getSenderUserName()+":"+UID,mediaControl);
                testClient.clearVideo();
                UID+=1;
            }
        }
    }

    //Scheduling tasks every second to check recieved messages got from https://stackoverflow.com/questions/27853735/simple-example-for-scheduledservice-in-javafx
    private static class TimerService extends ScheduledService<Integer> {

        private IntegerProperty count = new SimpleIntegerProperty();

        public final void setCount(Integer value) {
            count.set(value);
        }

        public final Integer getCount() {
            return count.get();
        }

        protected Task<Integer> createTask() {
            return new Task<Integer>() {
                protected Integer call() {
                    //Adds 1 to the count
                    count.set(getCount() + 1);
                    return getCount();
                }
            };
        }
    }

    class graphicalUserInterface extends Stage {

        public graphicalUserInterface() {
            super();
            try {
                final int[] currentUID = {0};
                HBox messageString = new HBox();
                VBox messagesSent = new VBox();
                VBox messagesRecieved = new VBox();

                VBox online = new VBox();
                Label whoIsOnline = new Label("Friends online:");
                messagesRecieved.getStyleClass().add("seperate");
                messagesSent.getStyleClass().add("seperate");
                messageString.getChildren().add(messagesRecieved);
                messageString.getChildren().add(messagesSent);
                VBox buttons = new VBox();

                ListView<String> users = new ListView<String>();
                ObservableList<String> list = FXCollections.observableArrayList();
                users.setItems(list);


                ObservableList<String> observableUsersList =
                        FXCollections.observableList(usersList);

                users.setItems(observableUsersList);

                TimerService service = new TimerService();
                AtomicInteger count = new AtomicInteger(0);
                service.setCount(count.get());
                service.setPeriod(Duration.seconds(1));
                service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        for (String user : rawMessages.keySet()) {
                            if (Integer.parseInt(user.split(":")[1]) > currentUID[0]) {
                                if (tempHeight[0] > 500) {
                                    messagesSent.getChildren().remove(0);
                                    messagesRecieved.getChildren().remove(0);

                                }
                                Node todisplay = rawMessages.get(user);
                                if (user.split(":")[0].equals(testClient.getUserName())) {
                                    todisplay.getStyleClass().add("messageBubbleSent");
                                    messagesSent.getChildren().add(todisplay);
                                    Label holder = new Label();
                                    holder.getStyleClass().add("holder");
                                    messagesRecieved.getChildren().add(holder);
                                } else {
                                    if(authorizedSenders.contains(user.split(":")[0])) {
                                        todisplay.getStyleClass().add("messageBubbleRecieved");
                                        messagesRecieved.getChildren().add(todisplay);
                                        Label holder = new Label();
                                        holder.getStyleClass().add("holder");
                                        messagesSent.getChildren().add(holder);
                                    }
                                }
                                currentUID[0] += 1;
                                tempHeight[0] += 120;
                            }
                        }
                        observableUsersList.setAll(usersList);
                        users.setItems(observableUsersList);
                    }
                });
                service.start();

                HBox userInterface = new HBox();

                TextField message = new TextField();
                message.setPromptText("Enter message here");


                HBox displayedInfo = new HBox();

                Button sendBtn = new Button("Send");
                sendBtn.setOnAction(new EventHandler<ActionEvent>() {
                    //send message and recieve message
                    @Override
                    public void handle(ActionEvent ae) {
                        testClient.setUserInput(message.getText());
                        message.clear();
                    }
                });

                Button attachPicture = new Button("Attach Picture");
                Button attachVideo = new Button("Attach Video");


                attachPicture.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        String Path = "";
                        FileDialog fd = new FileDialog(new JFrame());
                        fd.setVisible(true);
                        File[] f = fd.getFiles();
                        if (f.length > 0) {
                            Path += fd.getFiles()[0].getAbsolutePath();
                            Path = Path.replace("\\", "/");
                        }
                        if (Path.length() > 1)
                            testClient.setUserInput("PICTURE " + Path);

                    }
                });
                attachVideo.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        String Path = "";
                        FileDialog fd = new FileDialog(new JFrame());
                        fd.setVisible(true);
                        File[] f = fd.getFiles();
                        if (f.length > 0) {
                            Path += fd.getFiles()[0].getAbsolutePath();
                            Path = Path.replace("\\", "/");
                        }
                        if (Path.length() > 1)
                            testClient.setUserInput("VIDEO " + Path);
                    }
                });
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

                Scene scene = new Scene(root, 1200, 800);
                scene.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
                super.setTitle("Clype 2.0");
                super.setScene(scene);
                super.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void start(Stage primaryStage) {
            try {
                primaryStage.setTitle("Multi-session user selection");
                VBox root = new VBox();
                ListView<String> userView = new ListView<String>();
                ObservableList<String> observableUsersList = FXCollections.observableList(usersList);
                userView.setItems( observableUsersList );

                TimerService service = new TimerService();
                AtomicInteger count = new AtomicInteger(0);
                service.setCount(count.get());
                service.setPeriod(Duration.seconds(10));
                service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                            testClient.setUserInput("LISTUSERS");
                            usersList = new LinkedList<String>(Arrays.asList(testClient.users.split(":")));
                            usersList.remove(testClient.getUserName());
                            observableUsersList.setAll(usersList);
                            userView.setItems(observableUsersList);
                    }
                });
                service.start();

                Label info = new Label("Users list updates every 10 seconds");

                Button launchGUI = new Button("Launch Session");


                userView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
                userView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
                    @Override
                    public void onChanged ( ListChangeListener.Change<? extends String> c) {
                    }
                });

                launchGUI.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        authorizedSenders = new LinkedList<String>();
                        authorizedSenders.addAll(userView.getSelectionModel().getSelectedItems());
                        graphicalUserInterface agui = new graphicalUserInterface();
                        agui.sizeToScene();
                        agui.show();
                    }
                }
                );
                root.getChildren().add(info );
                root.getChildren().add( userView );
                root.getChildren().add( launchGUI );
                Scene scene = new Scene(root,400,400);
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch(Exception e) {
                e.printStackTrace();
            }
    }

    public static void main(String[] args) {
        String input;
        try{
            input = args[0];
            String[] userArgs = input.split("@");
            if (userArgs.length == 1)
                testClient = new ClypeClient(userArgs[0]);
            else {
                String[] hostArgs = userArgs[1].split(":");
                if (hostArgs.length == 1)
                    testClient = new ClypeClient(userArgs[0],hostArgs[0]);
                else
                    testClient = new ClypeClient(userArgs[0],hostArgs[0],Integer.parseInt(hostArgs[1]));
            }
        }catch (ArrayIndexOutOfBoundsException aioobe){
            testClient = new ClypeClient("Justin");
        }

        new Thread(() -> {testClient.start(); }).start();
        new Thread(() -> {launch(args); }).start();
        new Thread(() -> {
            try {
                updateRecieved();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
