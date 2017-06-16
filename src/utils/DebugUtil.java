package utils;

import java.io.PrintStream;

/**
 * Created by a on 2017-06-16.
 */
public class DebugUtil {

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

}
