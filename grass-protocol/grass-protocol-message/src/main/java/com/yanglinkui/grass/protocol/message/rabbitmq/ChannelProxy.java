package com.yanglinkui.grass.protocol.message.rabbitmq;

import com.rabbitmq.client.Channel;

public interface ChannelProxy extends Channel {

    public Channel getTargetChannel();

    public void active();
}
