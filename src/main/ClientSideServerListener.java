package main;

/**
 * @author Justin Mekenye Opini
 * @version 1.0
 * Client of package main
 */

public class ClientSideServerListener implements Runnable {
    private ClypeClient client;
    ClientSideServerListener( ClypeClient client){
        this.client = client;
    }
    @Override
    public void run(){
        while(!this.client.getCloseConnection()){
            this.client.receiveData();
            this.client.printData();
        }
    }
}
