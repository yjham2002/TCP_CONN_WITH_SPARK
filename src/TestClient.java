import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by a on 2017-05-25.
 */
public class TestClient {
    public static void main(String... args){
        Socket socket;

        try {

            socket = new Socket("192.168.0.38",8000);

            new ClientSender(socket,"Client-" + new Random().nextInt()).start();

            new ClientReceiver(socket).start();

        } catch (UnknownHostException e){e.printStackTrace();

        } catch (IOException e){e.printStackTrace();

        }

    }
}
class ClientSender extends Thread {

    String name;
    DataOutputStream out;
    public ClientSender(Socket socket,String name) {
        this.name = name;
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e){e.printStackTrace();
        }
    }//생성자
    @Override
    public void run() {
        Scanner scan = new Scanner(System.in); //console창 입력
        try {
            if(out!=null){ //초기 접속 이후 실행되지 않는다.
                out.writeUTF(name);
                System.out.println("Connected");
            }
            while(out != null){
                out.writeUTF(scan.nextLine());
            }//server로 문자를 보내는 메소드
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}//ClientSender
class ClientReceiver extends Thread {
    DataInputStream in;
    public ClientReceiver(Socket socket) {
        try{
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e){e.printStackTrace();
        }
    }//생성자
    @Override
    public void run() {
        while(in!=null){
            try {
                System.out.println(in.readUTF());
            } catch (IOException e){e.printStackTrace();
            }
        }
    }
}//ClientReceiver

