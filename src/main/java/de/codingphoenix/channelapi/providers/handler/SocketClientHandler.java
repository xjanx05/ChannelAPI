package de.codingphoenix.channelapi.providers.handler;

import de.codingphoenix.channelapi.providers.event.EventHandler;
import de.codingphoenix.channelapi.providers.event.channel.ChannelReceiveMessageEvent;
import de.codingphoenix.channelapi.providers.event.channel.ClientDisconnectServerConnectionEvent;
import de.codingphoenix.channelapi.providers.event.channel.ServerDisconnectClientConnectionEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
public class SocketClientHandler implements Runnable {
    private ByteBuffer buffer;

    private final UUID channelIdentifier;
    private final SocketChannel socketChannel;
    private final EventHandler eventHandler;
    private boolean running;


    public SocketClientHandler(UUID channelIdentifier, EventHandler eventHandler, SocketChannel socketChannel) {
        this.channelIdentifier = channelIdentifier;
        this.eventHandler = eventHandler;
        this.socketChannel = socketChannel;
        buffer = ByteBuffer.allocate(1024);
    }

    @Override
    public void run() {
        running = true;
        try {
            while (running) {
                buffer.clear();
                int bytesRead = 0;
                try {
                    bytesRead = socketChannel.read(buffer);
                } catch (IOException e) {
                    eventHandler.triggerEvent(new ClientDisconnectServerConnectionEvent(socketChannel, false));
                    socketChannel.close();
                    break;
                }
                if (bytesRead == -1) {
                    eventHandler.triggerEvent(new ClientDisconnectServerConnectionEvent(socketChannel, true));
                    socketChannel.close();
                    break;
                }

                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String messageReceived = new String(bytes).trim();
                eventHandler.triggerEvent(new ChannelReceiveMessageEvent(this, messageReceived));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(JSONObject jsonObject) throws IOException {
        write(ByteBuffer.wrap(jsonObject.toString().getBytes()));
    }

    public void write(String msg) throws IOException {
        write(ByteBuffer.wrap(msg.getBytes()));
    }

    public void write(ByteBuffer msg) throws IOException {
        socketChannel.write(msg);
    }
}
