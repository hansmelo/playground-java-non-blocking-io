package com.monsterend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class AppTest {
    public static void main(String... args) throws InterruptedException {
        var sockets = new Socket[3000];
        for (var i = 0; i < sockets.length; i++) {
            try {
                sockets[i] = new Socket("localhost", 8080);
                System.out.println(i);
            } catch (IOException e) {
                System.err.println("Error connecting " + e);
            }
        }
        TimeUnit.DAYS.sleep(1);
    }
}
