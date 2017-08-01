package utils;

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
            byte[] arr =null;
            for(int i = 0; i < 10; i++) {

                arr = new byte[]{83, 84, 48, 51, 48, 57, 48, 48, 55, 56, 48, 49, 2, -43, 8, 69, 0, 33, 2, -43, 8, 68, 0, 2, 2, -43, 8, 69, 0, 2, 2, -43, 8, 71, 0, 31, 2, -46, 6, -85, 0, 2, 2, -46, 6, -83, 17, -5, 2, -43, 8, 95, 0, 7, 2, -43, 8, 94, 0, 1, 2, -46, 6, -86, 0, 3, 2, -46, 6, -83, 17, -5, 2, -43, 8, 69, 0, 33, 2, -43, 8, 68, 0, 2, 2, -43, 8, 101, 0, 1, 2, -43, 8, 99, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, -26, 1, 18, 1, 125, 0, 9, 2, 105, 0, 0, 0, 1, 6, 11, 2, -43, 8, 101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 5, -26, 1, 18, 1, 125, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -24, 0, -4, 3, 82, 0, 9, 7, -14, 0, 0, 0, 0, 4, -80, 0, -4, 3, 82, 2, 95, 0, -56, 0, 3, 0, 0, 0, 0, 0, -110, 0, 0, 0, 20, 8, 102, 2, -43, 0, 0, 7, -31, 2, -43, 7, -125, -1, -113, 0, 0, 0, 0, 83, 13, 10};
//                arr = new byte[]{83, 84, 48, 51, 48, 57, 49, 51, 53, 51, 48, 57, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, -69, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 2, -44, 6, 88, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -99, 0, -41, 1, -8, 0, 0, 2, 0, 0, 0, 0, 1, -123, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -99, 0, -41, 1, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -52, -52, 0, 0, 0, 0, 3, -14, 3, -14, 3, -14, 3, -14, 0, -56, -128, 0, 0, 0, 0, 0, 1, -8, 0, 0, 0, 16, 5, -24, 3, 33, 0, 0, 7, -31, 2, -44, 6, 78, 0, 64, 0, 0, 0, 0, 81, 13, 10, 13, 10};

                out.write(arr);
                out.flush();

                System.out.println("데이터를 송신 하였습니다." + arr.length);
//                try{
//                    Thread.sleep(15000);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
            }

            byte[] buffer = new byte[512];

            while ((inp.read(buffer)) != -1)
            {
                out.write(arr);
            }

            // 서버 접속 끊기
            in.close();
            out.close();
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
