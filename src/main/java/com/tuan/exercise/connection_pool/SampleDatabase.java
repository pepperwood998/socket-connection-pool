package com.tuan.exercise.connection_pool;

import java.util.Hashtable;

public class SampleDatabase {

    private static Hashtable<String, String> db = new Hashtable<String, String>();

    static {
        db.put("zyot", "12345");
        db.put("zuratama", "1111");
        db.put("test", "123");
        db.put("eie", "1234");
    }

    public static boolean checkAuthentication(String name, String password) {
        if(db.containsKey(name)) {
            if(db.get(name).equals(password))
            return true;
        }   
        return false;
    }
}
