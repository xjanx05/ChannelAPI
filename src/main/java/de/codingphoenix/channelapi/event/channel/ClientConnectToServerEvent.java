package de.codingphoenix.channelapi.event.channel;

import de.codingphoenix.channelapi.event.Cancelable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.channels.SocketChannel;

@Getter
@Setter
@Accessors(fluent = true)
public class ClientConnectToServerEvent extends ChannelEvent implements Cancelable {

    public ClientConnectToServerEvent(SocketChannel socketChannel) {
        super(socketChannel);
    }


    private  boolean canceled;
    @Override
    public void canceled(boolean canceled) {
       this.canceled = canceled;
    }

    @Override
    public boolean canceled() {
        return canceled;
    }
}