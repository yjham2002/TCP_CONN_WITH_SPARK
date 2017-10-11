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
        Log.i(array1.length + " :: " + array2.length);

        int len = array1.length > array2.length ? array2.length : array1.length;
        int count = 0;
        for(int i = 0; i < len; i++){
            if(array1[i] != array2[i]) {
                count++;
                Log.i("array1["+i+"] = " + array1[i] + " : array["+i+"] = " + array2[i]);
            }
        }

        if(count == 0) Log.i("[DebugUtil] Both arrays are same");
        else Log.i("[DebugUtil] Both arrays are not same");
    }

}
