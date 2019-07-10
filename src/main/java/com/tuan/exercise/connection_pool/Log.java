package com.tuan.exercise.connection_pool;

public class Log {
    public static void log(String msg) {
        System.out.print(msg);
    }

    public static void line(String msg) {
        System.out.println(msg);
    }
    
    public static void error(String err, Exception e, boolean printTrace) {
        System.out.println("Error: " + err);
        if (printTrace && e != null) {
            e.printStackTrace();
        }
    }
}
