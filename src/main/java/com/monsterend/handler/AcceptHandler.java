package com.monsterend.handler;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class AcceptHandler implements Handler<SelectionKey, IOException> {
    private final Map<SocketChannel, Queue<ByteBuffer>> peddingData;

    public AcceptHandler(Map<SocketChannel, Queue<ByteBuffer>> peddingData) {
        this.peddingData = peddingData;
    }

    public void handle(SelectionKey key) throws IOException {
        var serverSocketChannel = (ServerSocketChannel) key.channel();
        var socketChannel = serverSocketChannel.accept();
        System.out.println("Someonoe connected: " + socketChannel);
        socketChannel.configureBlocking(false);
        peddingData.put(socketChannel, new ConcurrentLinkedQueue<ByteBuffer>());

        socketChannel.register(key.selector(), SelectionKey.OP_READ);
    }
}