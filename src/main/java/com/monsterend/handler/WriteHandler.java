package com.monsterend.handler;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class WriteHandler implements Handler<SelectionKey, IOException> {
    private final Map<SocketChannel, Queue<ByteBuffer>> pendingData;

    public WriteHandler(Map<SocketChannel, Queue<ByteBuffer>> pendingData) {
        this.pendingData = pendingData;
    }

    public void handle(SelectionKey key) throws IOException {
        var socketChannel = (SocketChannel) key.channel();
        var queue = pendingData.get(socketChannel);
        while (!queue.isEmpty()) {
            var buffer = queue.peek();
            var written = socketChannel.write(buffer);
            if (written == -1) {
                socketChannel.close();
                pendingData.remove(socketChannel);
                return;
            }
            if (buffer.hasRemaining()) {
                return;
            } else {
                queue.remove();
            }
        }
        key.interestOps(SelectionKey.OP_READ);
    }
}