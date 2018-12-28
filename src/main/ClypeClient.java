package main;
import data.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 2.0
 * Client of package main
 */

public class ClypeClient {
    private String userName;
    private String hostName;
    private int port;
    private boolean closeConnection;
    private ClypeData dataToSendToServer;
    private ClypeData dataToReceiveFromServer;
    //private Scanner inFromStd;
    private ObjectInputStream inFromServer;
    private ObjectOutputStream outToServer;
    private String userInput;
    private Boolean recievedInput;
    private String userMessage;
    private Boolean isRecieved;
    private byte[] pictueRecieved;
    private byte[] videoRecieved;
    private String currentSneder;
    private String[] keyboardEmojis = new String[]{":)",";)",":(","B)",":D","D:",":d",";p",":p",":o",":s",":x",":|",":/",":[",":>",":@",":*",":!","o:)",">:-o",">:-)",":3","(y)","(n)"};
    private String[] unicodeEmojis = new String[]{"\uD83D\uDE0A","\uD83D\uDE09","\uD83D\uDE1F","\uD83D\uDE0E","\uD83D\uDE03","\uD83D\uDE29","\uD83D\uDE0B","\uD83D\uDE1C","\uD83D\uDE1B","\uD83D\uDE2E","\uD83D\uDE16","\uD83D\uDE36","\uD83D\uDE10","\uD83D\uDE15","\uD83D\uDE33","\uD83D\uDE0F","\uD83D\uDE37","\uD83D\uDE18","\uD83D\uDE2C","\uD83D\uDE07","\uD83D\uDE20","\uD83D\uDE08","\uD83D\uDE3A","\uD83D\uDC4D","\uD83D\uDC4E"};
    public String users;
    /**
     * @param userName String	representing	name	of	the	client
     * @param hostName String	representing	name	of	the	computer	representing	the	server
     * @param port Integer	representing	port	number	on	server	connected	to
     */
    public ClypeClient(String userName,String hostName,int port) throws IllegalArgumentException{
        if (userName == null || hostName == null || port < 1024)
            throw new IllegalArgumentException("Username/Hostname is null or port number is below 1024");
        this.userName = userName;
        this.hostName = hostName;
        this.port = port;
        this.closeConnection = false;
        this.dataToSendToServer = null;
        this.dataToReceiveFromServer = null;
        this.inFromServer = null;
        this.outToServer = null;
        this.userInput = "";
        this.recievedInput = false;
        this.isRecieved = false;
        this.pictueRecieved = new byte[8192];
        this.videoRecieved = null;
        users = this.userName;
    }

    /**
     * @param userName String	representing	name	of	the	client
     * @param hostName String	representing	name	of	the	computer	representing	the	server
     */
    public ClypeClient(String userName,String hostName)
    {
        this(userName, hostName, 7000);
    }

    /**
     * @param userName String	representing	name	of	the	client
     */

    public ClypeClient(String userName){
        this(userName,"localhost");
    }
    public ClypeClient(){
        this("Anon");
    }

    /**
     * Runnable multithreading
     */
    public void start(){
        Socket skt = null;
        try {
            //this.inFromStd = new Scanner(System.in);
            skt = new Socket(this.hostName, this.port);

            //Establish name with server
            //Socket usernameSocket = new Socket(this.hostName,8000);
            //ObjectOutputStream usernameSender = new ObjectOutputStream(usernameSocket.getOutputStream());
            //usernameSender.writeUTF(this.userName);
            //usernameSender.flush();
            //usernameSender.reset();
            //usernameSender.close();
            //usernameSocket.close();


            this.inFromServer  = new ObjectInputStream(skt.getInputStream());
            this.outToServer = new ObjectOutputStream(skt.getOutputStream());
            System.out.println("Connected");

            this.outToServer.writeUTF(this.userName);
            this.outToServer.flush();
            this.outToServer.reset();

            ClientSideServerListener clientSideServerListener = new ClientSideServerListener(this);
            Thread clientSideServerThread =  new Thread(clientSideServerListener);
            clientSideServerThread.start();

            while (!this.closeConnection) {
                while (!recievedInput)
                    continue;
                readClientData();
                sendData();
                if (this.dataToSendToServer == null)
                    this.closeConnection = true; //break loop
            }
        } catch(UnknownHostException uhe)
        {
            System.err.println(uhe.getMessage());
        }
        catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        try
        {
            this.dataToSendToServer = null;
            sendData(); //close server
            this.inFromServer.close();
            this.outToServer.close();
            skt.close();
        }
        catch(IOException | NullPointerException ioe)
        {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     *
     * @throws IOException
     */
    public void readClientData() throws IOException {
        if (this.recievedInput) {
            this.isRecieved = false;
            String BUFFER = this.userInput;
            if (BUFFER.equals("DONE")) {
                this.closeConnection = true;
                alert("DONE");
            } else if (BUFFER.split(" ")[0].equals("SENDFILE")) {
                FileClypeData temp = new FileClypeData(this.userName, BUFFER.split(" ")[1], 2);
                temp.readFileContents();
                if (temp.getData() != null)
                    this.dataToSendToServer = temp;
            } else if (BUFFER.split(" ")[0].equals("PICTURE")){
                PictureCypeData temp = new PictureCypeData(BUFFER.split(" ")[1],this.userName,4);
                if (temp.getData() != null)
                    this.dataToSendToServer = temp;
            } else if (BUFFER.split(" ")[0].equals("VIDEO")){
                VideoClypeData temp = new VideoClypeData(BUFFER.split(" ")[1],this.userName,5);
                if (temp.getData() != null)
                    this.dataToSendToServer = temp;
            }else if (BUFFER.equals("LISTUSERS")) {
                this.dataToSendToServer = new MessageClypeData("", "", -1);
                alert("LISTUSERS");
            } else
                this.dataToSendToServer = new MessageClypeData(this.userName, BUFFER, 1);
            this.recievedInput = false;
        }
    }
    public void sendData() {
        try {
            this.outToServer.writeObject(this.dataToSendToServer);
            this.outToServer.flush();
        }catch (IOException ioe){
            System.err.println(ioe.getMessage());
        }
    }

    /**
     *
     * @param command to send done and listusers commands
     */
    public void alert(String command) {
        try {
            this.outToServer.writeObject(command);
        }catch (IOException ioe){
            System.err.println(ioe.getMessage());
        }
    }
    public void receiveData(){
        try {
            this.dataToReceiveFromServer = (ClypeData) this.inFromServer.readObject();
        }catch (IOException ioe){
            System.out.println(ioe.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void printData(){
        try {
            switch (this.dataToReceiveFromServer.getType()) {
                case (-1): {
                    if(((String) this.dataToReceiveFromServer.getData()).length() >= 1)
                        this.users = (String) this.dataToReceiveFromServer.getData();
                    break;
                }
                case (1): {
                    String temp = (String) this.dataToReceiveFromServer.getData();
                    for (int emojiIndex = 0;emojiIndex < keyboardEmojis.length;emojiIndex++) {
                        temp = temp.replace(keyboardEmojis[emojiIndex], unicodeEmojis[emojiIndex]);
                    }
                    this.userMessage = temp;
                    this.currentSneder = dataToReceiveFromServer.getUserName();
                    this.dataToReceiveFromServer = null;
                    break;
                }
                case (4): {
                    this.pictueRecieved = (byte[]) this.dataToReceiveFromServer.getData();
                    this.currentSneder = dataToReceiveFromServer.getUserName();
                    this.dataToReceiveFromServer = null;
                    break;
                }case (5): {
                    this.videoRecieved= (byte[]) this.dataToReceiveFromServer.getData();
                    this.currentSneder = dataToReceiveFromServer.getUserName();
                    this.dataToReceiveFromServer = null;
                    break;
                }
            }
            isRecieved =true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setUserInput(String userInput){this.userInput = userInput;this.recievedInput=true;}
    public Boolean getIsRecieved(){return this.isRecieved;}

    public String getUserMessage() { return this.userMessage; }
    public byte[] getPicute() { return this.pictueRecieved; }
    public byte[] getVideo() { return this.videoRecieved; }

    public void clearMessage(){this.userMessage = null;}
    public void clearPicture(){this.pictueRecieved = null;}
    public void clearVideo(){this.videoRecieved = null;}
    /**
     * @return userName
     */
    public String getUserName(){
        return this.userName;
    }
    public String getSenderUserName(){
        return this.currentSneder;
    }

    /**
     * @return hostName
     */
    public String getHostName(){
        return this.hostName;
    }

    /**
     * @return port
     */
    public int getPort(){
        return this.port;
    }

    /**
     * @return unique hashcode value
     * Overrides super class
     */
    public boolean getCloseConnection() {return this.closeConnection;}
    @Override
    public int hashCode(){
        int result = 17;
        result = 37*result + port;
        int temp = (this.closeConnection) ? 1:0;
        result = 37*result + temp;
        result = 37*result + this.userName.hashCode();
        result = 37*result + this.hostName.hashCode();
        if (this.dataToSendToServer != null)
            result = 37*result + this.dataToSendToServer.hashCode();
        if (this.dataToReceiveFromServer != null)
            result = 37*result + this.dataToReceiveFromServer.hashCode();
        return result;
    }

    /**
     * @param other Object to compare with
     * @return true or false if equal or nor
     * Overrides super class
     */
    @Override
    public boolean equals(Object other){
        if (!(other instanceof ClypeClient)){
            return false;
        }
        ClypeClient otherClient = (ClypeClient) other;
        return this.userName.equals(otherClient.userName) && this.hostName.equals(otherClient.hostName) && this.port == otherClient.port && this.closeConnection == otherClient.closeConnection && this.dataToSendToServer == otherClient.dataToSendToServer && this.dataToReceiveFromServer == otherClient.dataToReceiveFromServer;
    }

    /**
     * @return formartted description of class using its variables
     * Overrides super class
     */
    @Override
    public String toString(){
        try{
        return "Username: " + userName +"\nHostname: " + hostName +"\nPort: " + port + "\nConnection: " + this.closeConnection + "\n" + this.dataToSendToServer.toString() + "\n" + this.dataToReceiveFromServer.toString();
        }catch (NullPointerException e){
            return "Username: " + userName +"\nHostname: " + hostName +"\nPort: " + port + "\nConnection: " + this.closeConnection + "\nnull dataToReceiveFromClient or null dataToSendFromClient";
        }
        }
}
