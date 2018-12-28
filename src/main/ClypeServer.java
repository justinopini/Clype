package main;
import data.ClypeData;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 1.0
 * Server class of main package
 */

public class ClypeServer {
    private int port;
    private boolean closeConnection;
    private Map<String,ServerSideClientIO> serverSideClientIOList;

    /**
     * @param port integer	representing	port	number	on	server	connected	to
     */
    private ClypeServer(int port){
        if ( port < 1024)
            throw new IllegalArgumentException("port number is below 1024");
        this.port = port;
        this.closeConnection = false;
        this.serverSideClientIOList = new HashMap<String, ServerSideClientIO>();
    }

    public ClypeServer(){
        this(7000);
    }
    public Map<String,ServerSideClientIO> getServerSideClientIOList(){return serverSideClientIOList;}
    public void start() throws IOException {
        System.out.println("Server started");
        ServerSocket skt = new ServerSocket(this.port);
        while (!this.closeConnection) {
                Socket clientSocket = skt.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                String username_ =  in.readUTF();
                ServerSideClientIO serverSideClientIOElement = new ServerSideClientIO(this, clientSocket, in,out);
                serverSideClientIOList.put(username_,serverSideClientIOElement);
                new Thread(serverSideClientIOElement).start();
        }
        skt.close();
    }
    @Override
    public int hashCode(){
        int result = 17;
        result = 37*result + port;
        int temp = (this.closeConnection) ? 1:0;
        result = 37*result + temp;
        for (ServerSideClientIO aServerSideClientIOList : serverSideClientIOList.values()) {
            result += aServerSideClientIOList.hashCode();
        }
        return result;
    }

    /**
     * @param other Object to compare with
     * @return true or false if equal or nor
     * Overrides super class
     */
    @Override
    public boolean equals(Object other){
        if (!(other instanceof ClypeServer)){
            return false;
        }
        ClypeServer otherServer = (ClypeServer) other;
        return this.port == otherServer.port && this.closeConnection == otherServer.closeConnection && this.serverSideClientIOList.equals(((ClypeServer) other).serverSideClientIOList);
    }

    /**
     * @return formartted description of class using its variables
     * Overrides super class
     */
    @Override
    public String toString(){
        try {
            return "Port: " + this.port + "\ncOonnection: " + this.closeConnection + "\n" + this.serverSideClientIOList.toString();
        }catch (NullPointerException e){
            return "Port: " + this.port + "\ncOonnection: " + this.closeConnection + "\nnull dataToReceiveFromClient or null dataToSendFromClient";
        }
    }
    synchronized void  broadcast(ClypeData dataToBroadcastToClients ){
        for (ServerSideClientIO aServerSideClientIOList : this.serverSideClientIOList.values()){
            aServerSideClientIOList.setDataToSendToClient(dataToBroadcastToClients);
            aServerSideClientIOList.sendData();
        }
    }
    synchronized void remove(ServerSideClientIO serverSideClientToRemove){
        this.serverSideClientIOList.values().remove(serverSideClientToRemove);
    }
    public static void main(String[] args) throws IOException {
        ClypeServer testServer = null;
        try{
            testServer = new ClypeServer(Integer.parseInt(args[0]));
        }catch (ArrayIndexOutOfBoundsException aioobe){
            testServer = new ClypeServer();
        }
        testServer.start();
    }
}
