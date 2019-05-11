package com.monsterend.handler;

import com.monsterend.util.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ReadHandler implements Handler<SelectionKey, IOException> {
    private final Map<SocketChannel, Queue<ByteBuffer>> pendingData;

    public ReadHandler(Map<SocketChannel, Queue<ByteBuffer>> pendingData) {
        this.pendingData = pendingData;
    }

    public void handle(SelectionKey key) throws IOException {
        var socketChannel = (SocketChannel) key.channel();
        var buffer = ByteBuffer.allocateDirect(80);
        var read = socketChannel.read(buffer);
        if (read == -1) {
            pendingData.remove(socketChannel);
            return;
        }
        if (read > 0) {
            Util.transmogrify(buffer);
            pendingData.get(socketChannel).add(buffer);
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }
}