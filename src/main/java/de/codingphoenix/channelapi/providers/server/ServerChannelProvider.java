package de.codingphoenix.channelapi.providers.server;

import de.codingphoenix.channelapi.event.EventHandler;
import de.codingphoenix.channelapi.event.channel.ClientConnectToServerEvent;
import de.codingphoenix.channelapi.event.channel.ClientDisconnectServerConnectionEvent;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Accessors(fluent = true)
public class ServerChannelProvider {
    private final ServerSocketChannel serverSocketChannel;
    private final EventHandler eventHandler;
    private boolean connected = false;

    private final HashMap<UUID, ServerSocketClientHandler> providers = new HashMap<>();

    public ServerChannelProvider() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        eventHandler = new EventHandler();

        Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));

        eventHandler.registerEventListener(ClientDisconnectServerConnectionEvent.class, event -> providers.remove(event.serverSocketClientHandler().channelIdentifier()));
    }

    public void connect(int port) throws IOException {
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        executor = Executors.newCachedThreadPool();

        connected = true;
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("Client connected: " + socketChannel.getRemoteAddress());

            UUID channelIdentifier = UUID.randomUUID();

            ClientConnectToServerEvent event = new ClientConnectToServerEvent(socketChannel);
            eventHandler.triggerEvent(event);

            if (event.canceled()) {
                socketChannel.close();
                continue;
            }

            ServerSocketClientHandler clientHandler = new ServerSocketClientHandler(channelIdentifier, eventHandler, ServerSocketClientHandler.SocketType.SERVER, socketChannel);
            providers.put(channelIdentifier, clientHandler);
            executor.submit(clientHandler);
        }
    }

    public void disconnect() {
        try {
            connected = false;
            for (ServerSocketClientHandler provider : providers.values()) {
                provider.socketChannel().close();
            }

            serverSocketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private ExecutorService executor;
}