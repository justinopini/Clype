package main;

import data.ClypeData;
import data.MessageClypeData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 1.0
 * Client of package main
 */

public class ServerSideClientIO implements Runnable {
    private boolean closeConnection;
    private ClypeData dataToReceiveFromClient;
    private String users; // to store the users
    private ClypeData dataToSendToClient;
    private ObjectInputStream inFromClient;
    private ObjectOutputStream outToClient;
    private ClypeServer	server;
    //private Socket clientSocket;
    public ServerSideClientIO(ClypeServer server,Socket clientSocket, ObjectInputStream  in,ObjectOutputStream out ){
        this.dataToReceiveFromClient = null;
        this.dataToSendToClient = null;
        this.inFromClient = in;
        this.outToClient = out;
        this.closeConnection = false;
        this.server = server;
        //this.clientSocket = clientSocket;
        this.users = "\n";
    }
    @Override
    public void run(){
        //try {
        //    this.outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
            //this.inFromClient  = new ObjectInputStream(clientSocket.getInputStream());
       // } catch (IOException e) {
        //    e.printStackTrace();
        //}
        while(!this.closeConnection){
            this.receiveData();
            if (dataToReceiveFromClient != null)
                this.server.broadcast(this.dataToReceiveFromClient);
            else {
                try {
                    this.outToClient.writeObject(new MessageClypeData("",this.users,-1)); //set to -1 to filter out the users
                } catch (IOException e) {
                    this.closeConnection = true;
                }
            }
        }

    }
    public void receiveData(){
        try {
            Object BUFFER = this.inFromClient.readObject();
            try {
                this.dataToReceiveFromClient = (ClypeData) BUFFER;

            }catch (Exception e){
                if(BUFFER.toString().equals("DONE")) {
                    this.server.remove(this);
                    this.closeConnection = true;
                }
                else if(BUFFER.toString().equals("LISTUSERS")) {
                    this.users = "";
                    boolean first = true;
                    for (String aServerSideClientIOUsername : this.server.getServerSideClientIOList().keySet()){
                            if (first) {
                                this.users += aServerSideClientIOUsername;
                                first = false;
                            } else {
                                this.users += ":" ;
                                this.users += aServerSideClientIOUsername;
                            }
                    }
                    this.dataToReceiveFromClient = null;
                }
            }
        }catch (IOException ioe){
            System.out.println(ioe.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void sendData(){
        try {
            this.outToClient.writeObject(this.dataToSendToClient);
        }catch (IOException ioe){
            System.err.println(ioe.getMessage());
        }
    }
    public void setDataToSendToClient(ClypeData dataToSendToClient){
        this.dataToSendToClient = dataToSendToClient;
    }
}
