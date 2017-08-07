package utils;

import constants.ConstProtocol;
import models.ByteSerial;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author 함의진
 * @version 1.0.0
 * 디버깅의 위해 사용할 수 있는 정적 메소드 클래스
 */
public class DebugUtil {

    /**
     * 배열을 출력하기 위한 클래스로 특정 인덱스에 표시하여 출력이 가능하다
     * @param array
     * @param index
     * @param stream
     */
    public static void printArray(byte[] array, int index, PrintStream stream){
        stream.print("[");
        for(int i = 0; i < array.length; i++){
            if(i == index) stream.print(" { ");
            stream.print(array[i]);
            if(i == index) stream.print(" } ");
            if(i + 1 < array.length) stream.print(", ");
        }
        stream.println("]");
    }

    public static void printArray(byte[] array, int index){
        printArray(array, index, System.out);
    }

    public static void printArray(byte[] array){
        printArray(array, -1);
    }

    public static void printArray(int[] array, int index, PrintStream stream){
        stream.print("[");
        for(int i = 0; i < array.length; i++){
            if(i == index) stream.print(" { ");
            stream.print(array[i]);
            if(i == index) stream.print(" } ");
            if(i + 1 < array.length) stream.print(", ");
        }
        stream.println("]");
    }

    public static void printArray(int[] array, int index){
        printArray(array, index, System.out);
    }

    public static void printArray(int[] array){
        printArray(array, -1);
    }

    public static void printArray(double[] array, int index, PrintStream stream){
        stream.print("[");
        for(int i = 0; i < array.length; i++){
            if(i == index) stream.print(" { ");
            stream.print(array[i]);
            if(i == index) stream.print(" } ");
            if(i + 1 < array.length) stream.print(", ");
        }
        stream.println("]");
    }

    public static void printArray(double[] array, int index){
        printArray(array, index, System.out);
    }

    public static void printArray(double[] array){
        printArray(array, -1);
    }

    public static void printAndCompare(byte[] array1, byte[] array2){
        System.out.println(array1.length + " :: " + array2.length);

        int len = array1.length > array2.length ? array2.length : array1.length;
        int count = 0;
        for(int i = 0; i < len; i++){
            if(array1[i] != array2[i]) {
                count++;
                System.out.println("array1["+i+"] = " + array1[i] + " : array["+i+"] = " + array2[i]);
            }
        }

        if(count == 0) System.out.println("[DebugUtil] Both arrays are same");
        else System.out.println("[DebugUtil] Both arrays are not same");
    }

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 8000);

            // 입력 스트림
            // 서버에서 보낸 데이터를 받음
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));



            // 출력 스트림
            // 서버에 데이터를 송신
            OutputStream out = socket.getOutputStream();
            InputStream inp = socket.getInputStream();

            // 서버에 데이터 송신
            Thread a = new Thread(){
              @Override
                public void run(){
                  try {
                      while (true) {

                          for(int i = 0; i < 2000; i++) {
                              final byte[] arr = new byte[]{83, 84, 48, 51, 49, 57, 49, 51, 53, 55, 48, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 34, 7, 31, 0, 51, 3, 34, 7, 122, 25, -63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 74, 2, -37, 3, -70, 39, 50, 0, 0, 0, 0, 11, 24, 3, 39, 2, -117, 0, -121, 3, 39, 0, 112, 1, 83, 3, 39, 0, -50, 1, 26, 3, 39, 2, -120, 0, -118, 2, -37, 3, -70, 39, 50, 0, 0, 0, 0, 11, 24, 3, 37, 0, -13, 12, -65, 3, 37, 0, -43, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 27, 1, 21, 1, 109, 0, 9, 2, 108, 0, 0, 0, 8, 4, 3, 3, 39, 0, 32, 2, -41, 3, 39, 3, -121, 0, 0, 3, 39, 3, -120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 37, 0, -13, 21, 47, 3, 39, 2, -47, 0, -111, 3, 39, 0, -20, 1, -101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -64, 3, 27, 1, 21, 1, 109, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, -80, 0, -6, 1, -62, 0, 9, -52, -52, 0, 0, 0, 0, 3, -14, 3, -14, 3, -14, 3, -14, 0, -56, 0, 3, 0, 0, 0, 0, 39, -93, 0, 0, 0, 55, 3, -118, 3, 39, 0, 0, 7, -31, 2, -37, 3, -70, -1, -113, 0, 0, 0, 0, 7, 13, 10};
//                arr = new byte[]{83, 84, 48, 51, 48, 57, 49, 51, 53, 51, 48, 57, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, -69, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -99, 0, -41, 1, -8, 0, 0, 2, 0, 0, 0, 0, 1, -123, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -99, 0, -41, 1, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -52, -52, 0, 0, 0, 0, 3, -14, 3, -14, 3, -14, 3, -14, 0, -56, -128, 0, 0, 0, 0, 0, 1, -8, 0, 0, 0, 16, 5, -24, 3, 33, 0, 0, 7, -31, 2, -44, 6, 78, 0, 64, 0, 0, 0, 0, 81, 13, 10, 13, 10};

                              out.write(arr);
                              out.flush();
                              System.out.println("데이터를 송신 하였습니다." + arr.length);
                          }
                          try{
                              Thread.sleep(10000);
                          }catch (InterruptedException e){
                              e.printStackTrace();
                          }


//                try{
//                    Thread.sleep(100);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
                      }
                  }catch (IOException e){
                      e.printStackTrace();
                  }
              }
            };




            Thread b = new Thread(){
                @Override
                public void run(){
                    try {
                        byte[] buffer = new byte[512];
                        ByteSerial byteSerial = new ByteSerial(buffer);

                        while ((inp.read(buffer)) != -1) {
                            System.out.println(Arrays.toString(buffer));
                            byte[] arr = SohaProtocolUtil.concat(ConstProtocol.STX, new byte[]{48, 50, 50, 52, 49, 51, 53, 55, 48, 54}, Arrays.copyOfRange(buffer, 8, 18));
                            byte[] check = SohaProtocolUtil.concat(new byte[]{HexUtil.checkSum(arr)}, ConstProtocol.ETX);
                            out.write(SohaProtocolUtil.concat(arr, check));
//                            out.flush();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

            };
            a.start();
            b.start();
//            while ((inp.read(buffer)) != -1)
//            {
//                System.out.println(in.readLine());
//                out.write(0);
//            }

            // 서버 접속 끊기
//            in.close();
//            out.close();
//            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
