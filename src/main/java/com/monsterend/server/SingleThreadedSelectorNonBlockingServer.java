package com.monsterend.server;

import com.monsterend.handler.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class SingleThreadedSelectorNonBlockingServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting the server");
        var serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        var selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        var pendingData = new HashMap<SocketChannel, Queue<ByteBuffer>>();
        var acceptHandler = new AcceptHandler(pendingData);
        var readHandler = new ReadHandler(pendingData);
        var writeHandler = new WriteHandler(pendingData);

        while (true) {
            selector.select();
            var keys = selector.selectedKeys();
            for (var it = keys.iterator(); it.hasNext();) {
                execute(acceptHandler, readHandler, writeHandler, it);
            }
        }
    }

    private static void execute(AcceptHandler acceptHandler, ReadHandler readHandler, WriteHandler writeHandler,
            Iterator<SelectionKey> it) throws IOException {
        var key = it.next();
        it.remove();
        if (key.isValid()) {
            if (key.isAcceptable()) {
                acceptHandler.handle(key);
            } else if (key.isReadable()) {
                readHandler.handle(key);
            } else if (key.isWritable()) {
                writeHandler.handle(key);
            }
        }
    }
}