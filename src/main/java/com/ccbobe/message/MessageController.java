package com.ccbobe.message;

import com.ccbobe.core.ClientStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ccbobe
 */
@RestController
public class MessageController {
    @Autowired
    private ClientStore clientStore;

    @RequestMapping("ackClient")
    public String ackClient(String host,String msg){
        clientStore.getChannel(host).writeAndFlush(msg+"\r\n");
        return msg;
    }


    @RequestMapping("ackInteger")
    public Integer ackInteger(String host,Integer msg){
        clientStore.getChannel(host).writeAndFlush(msg);
        return msg;
    }


    @RequestMapping("ssl")
    public String ssl(String msg){
        return msg;
    }

}
