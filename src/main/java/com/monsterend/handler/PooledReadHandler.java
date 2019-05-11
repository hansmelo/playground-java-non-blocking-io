
package com.monsterend.handler;

import com.monsterend.util.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class PooledReadHandler implements Handler<SelectionKey, IOException> {
    private final ExecutorService pool;
    private final Map<SocketChannel, Queue<ByteBuffer>> pendingData;
    private final Queue<Runnable> selectorActions;

    public PooledReadHandler(ExecutorService pool, Map<SocketChannel, Queue<ByteBuffer>> pendingData,
            Queue<Runnable> selectorActions) {
        this.pool = pool;
        this.pendingData = pendingData;
        this.selectorActions = selectorActions;
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
            pool.submit(() -> {
                Util.transmogrify(buffer);
                pendingData.get(socketChannel).add(buffer);
                selectorActions.add(() -> key.interestOps(SelectionKey.OP_WRITE));
                key.selector().wakeup();
            });
      
        }
    }
}