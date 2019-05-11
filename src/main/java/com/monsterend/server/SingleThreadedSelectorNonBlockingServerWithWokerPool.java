package com.monsterend.server;

import com.monsterend.handler.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class SingleThreadedSelectorNonBlockingServerWithWokerPool {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting the server");
        var serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        var selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        var pool = Executors.newWorkStealingPool();
        var selectorActions = new ConcurrentLinkedQueue<Runnable>();

        var peddingData = new ConcurrentHashMap<SocketChannel, Queue<ByteBuffer>>();
        var acceptHandler = new AcceptHandler(peddingData);
        var readHandler = new PooledReadHandler(pool, peddingData, selectorActions);
        var writeHandler = new WriteHandler(peddingData);

        while (true) {
            selector.select();
            processSelectorActions(selectorActions);
            var keys = selector.selectedKeys();
            for (var it = keys.iterator(); it.hasNext();) {
                execute(acceptHandler, readHandler, writeHandler, it);
            }
        }
    }

    private static void processSelectorActions(Queue<Runnable> selectorActions) {
        Runnable action;
        while ((action = selectorActions.poll()) != null) {
            action.run();
        }
    }

    private static void execute(AcceptHandler acceptHandler, PooledReadHandler readHandler, WriteHandler writeHandler,
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