package it.unibo.utils;

public class Logger {
    public static void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
