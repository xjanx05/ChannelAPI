package de.codingphoenix.channelapi.providers.event.channel;

import de.codingphoenix.channelapi.providers.handler.SocketClientHandler;
import lombok.Getter;
import lombok.experimental.Accessors;


@Getter
@Accessors(fluent = true)
public class ChannelReceiveMessageEvent extends ServerChannelHandleEvent {

    public ChannelReceiveMessageEvent(SocketClientHandler socketClientHandler, String message) {
        super(socketClientHandler);
        this.message = message;
    }

    private final String message;
}

