package server;

import constants.ConstProtocol;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by a on 2017-05-25.
 */
public class ProtocolResponder extends Thread{

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private HashMap clients;

    public ProtocolResponder(Socket socket, HashMap clients){
        this.socket = socket;
        this.clients = clients;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        boolean started = false;

        String name = null;
        try{
            name = in.readUTF();
            clients.put(name, out);

            System.out.println("Responder :: [" + name + "] Totally " + clients.size() + " connection established");

            while(in != null){
                String msg = in.readUTF();
                String res = "";

                if(Arrays.equals(msg.substring(0, 2).getBytes(), ConstProtocol.STX.getBytes())) res = "STX";

                sendToSpecificOne("Specific Response for [" + msg + "] => " + res, name);
                sendToAll("Message sent from [" + name + "] :: " + msg);
            }

            System.out.println("Conn");

        }catch(IOException e){
            // Ignore
        }finally {
            System.out.println("Connection Finished");
            clients.remove(name);
        }
    }

    private void sendToSpecificOne(String msg, String key){
        System.out.println("Sending :: " + msg);
        try {
            DataOutputStream out = (DataOutputStream) clients.get(key);
            out.writeUTF(msg);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendToAll(String msg){
        System.out.println("Sending :: " + msg);

        Iterator it = clients.keySet().iterator();
        while(it.hasNext()){
            try{
                DataOutputStream out = (DataOutputStream)clients.get(it.next());
                out.writeUTF(msg);
            }catch(IOException e){

            }
        }
    }

}
