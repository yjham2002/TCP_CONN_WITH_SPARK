package server;

import constants.ConstProtocol;
import models.ByteSerial;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * @author 함의진
 * 스레드 클래스를 상속하여 클라이언트에 따라 인스턴스를 가변적으로 증가시켜 각각에 대한 응답 기능을
 * 위임받는 클래스로, ServiceProvider에 대해 Aggregation 관계를 가진다.
 * ServiceProvider는 해시맵으로 운영되며, 클라이언트마다 유니크한 값을 해싱하여 이를 키로 사용한다.
 * 또한, 이는 싱크로나이즈되어 운영되기에 이에 대해 레이스 컨디션이 발생하지 않도록 설계한다.
 */
public class ProtocolResponder extends Thread{

    private Socket socket; // ServiceProvider로부터 accept된 단위 소켓
    private DataInputStream in; // 입력 스트림
    private DataOutputStream out; // 출력 스트림
    private HashMap clients; // ServiceProvider의 클라이언트 집합의 레퍼런스 포인터

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
            byte[] buffer = new byte[ByteSerial.POOL_SIZE];

            while ((in.read(buffer, 0, buffer.length)) != -1) {
                in.read(buffer);
                new ByteSerial(buffer);
            }

//            name = in.readByte();
//            clients.put(name, out);

            System.out.println("Responder :: [" + name + "] Totally " + clients.size() + " connection established");

            while(in != null){
                String msg = in.readUTF();
                String res = "";


                //sendToSpecificOne("Specific Response for [" + msg + "] => " + res, name);
                sendToAll(new ByteSerial(new byte[]{83, 84, 48, 48, 55, 56, 0, 0, 0, 0, 1, 0, 0, 13, 10}, ByteSerial.TYPE_SET));
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

    private void sendToAll(ByteSerial msg){
        System.out.println("Sending :: " + msg);

        Iterator it = clients.keySet().iterator();
        while(it.hasNext()){
            try{
                DataOutputStream out = (DataOutputStream)clients.get(it.next());
                out.write(msg.getProcessed());
            }catch(IOException e){

            }
        }
    }

    private void sendToAll(byte[] msg){
        System.out.println("Sending :: " + msg);

        Iterator it = clients.keySet().iterator();
        while(it.hasNext()){
            try{
                DataOutputStream out = (DataOutputStream)clients.get(it.next());
                out.write(msg);
            }catch(IOException e){

            }
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
