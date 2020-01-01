package com.ccbobe.core;


import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author ccbobe
 */
@Service
public class ClientStore {

    private ConcurrentHashMap<String, Channel> store = new ConcurrentHashMap<>(8);

    public Channel add(String host,Channel channel){
        store.put(host,channel);
        return channel;
    }

    public Channel removed(String host){
        Channel channel = store.remove(host);
        return channel;
    }

    public Channel getChannel(String host){
        Channel channel = store.get(host);
        return channel;
    }

}
