package ch.softappeal.yass.transport.socket;

import ch.softappeal.yass.core.remote.session.Connection;
import ch.softappeal.yass.core.remote.session.Packet;
import ch.softappeal.yass.core.remote.session.SessionClient;
import ch.softappeal.yass.serialize.Reader;
import ch.softappeal.yass.serialize.Serializer;
import ch.softappeal.yass.serialize.Writer;
import ch.softappeal.yass.transport.TransportSetup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class SocketConnection implements Connection {

    public final Socket socket;
    private final Serializer packetSerializer;
    private volatile boolean closed = false;
    private final BlockingQueue<ByteArrayOutputStream> writerQueue = new LinkedBlockingQueue<>(); // unbounded queue
    private final Object writerQueueEmpty = new Object();

    private SocketConnection(final TransportSetup setup, final Socket socket) {
        this.socket = socket;
        packetSerializer = setup.packetSerializer;
    }

    static void create(
        final TransportSetup setup,
        final Socket socket,
        final Reader reader,
        final OutputStream outputStream,
        final Executor writerExecutor
    ) throws Exception {
        final SocketConnection connection = new SocketConnection(setup, socket);
        final SessionClient sessionClient = SessionClient.create(setup, connection);
        try {
            writerExecutor.execute(() -> {
                try {
                    connection.write(outputStream);
                } catch (final Exception e) {
                    sessionClient.close(e);
                }
            });
        } catch (final Exception e) {
            sessionClient.close(e);
            return;
        }
        connection.read(sessionClient, reader);
    }

    @Override public void write(final Packet packet) throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
        packetSerializer.write(packet, Writer.create(buffer));
        writerQueue.put(buffer);
    }

    private void read(final SessionClient sessionClient, final Reader reader) {
        while (true) {
            try {
                final Packet packet = (Packet)packetSerializer.read(reader);
                sessionClient.received(packet);
                if (packet.isEnd()) {
                    return;
                }
            } catch (final Exception e) {
                sessionClient.close(e);
                return;
            }
        }
    }

    private void notifyWriterQueueEmpty() {
        synchronized (writerQueueEmpty) {
            writerQueueEmpty.notifyAll();
        }
    }

    /**
     * Buffering of output is needed to prevent long delays due to Nagle's algorithm.
     */
    private static void flush(final ByteArrayOutputStream buffer, final OutputStream out) throws IOException {
        buffer.writeTo(out);
        out.flush();
    }

    private void write(final OutputStream out) throws Exception {
        while (true) {
            final ByteArrayOutputStream buffer = writerQueue.poll(200L, TimeUnit.MILLISECONDS);
            if (buffer == null) {
                if (closed) {
                    return;
                }
                continue;
            }
            while (true) { // drain queue -> batching of packets
                final ByteArrayOutputStream buffer2 = writerQueue.poll();
                if (buffer2 == null) {
                    notifyWriterQueueEmpty();
                    break;
                }
                buffer2.writeTo(buffer);
            }
            flush(buffer, out);
        }
    }

    private boolean writerQueueNotEmpty() {
        return !writerQueue.isEmpty();
    }

    public void awaitWriterQueueEmpty() {
        try {
            synchronized (writerQueueEmpty) {
                while (writerQueueNotEmpty()) {
                    writerQueueEmpty.wait();
                }
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Note: No more calls to {@link #write(Packet)} are accepted when this method is called due to implementation of {@link SessionClient}.
     */
    @Override public void closed() throws Exception {
        try {
            try {
                while (writerQueueNotEmpty()) {
                    TimeUnit.MILLISECONDS.sleep(200L);
                }
                TimeUnit.MILLISECONDS.sleep(200L); // give the socket a chance to write the end packet
            } finally {
                closed = true; // terminates writer thread
                socket.close();
            }
        } finally {
            notifyWriterQueueEmpty(); // guarantees that awaitWriterQueueEmpty never blocks again
        }
    }

}
