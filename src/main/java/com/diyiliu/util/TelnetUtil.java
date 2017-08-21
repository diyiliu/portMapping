package com.diyiliu.util;

import com.diyiliu.thread.BackMsgThread;
import com.diyiliu.thread.ExchangeThread;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Description: TelnetUtil
 * Author: DIYILIU
 * Update: 2017-08-17 09:49
 */
public class TelnetUtil {

    private String host;
    private int port;

    private BackMsgThread backMsgThread;
    private ExchangeThread exchangeThread;

    public TelnetUtil(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    public void init(){
        try {
            TelnetClient client = new TelnetClient();
            client.connect("192.168.1.1", 23);
            InputStream in = client.getInputStream();
            OutputStream os = client.getOutputStream();

            backMsgThread = new BackMsgThread(in);
            new Thread(backMsgThread).start();

            exchangeThread = new ExchangeThread(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(String endFlag, String... values){
        exchangeThread.setQueue(backMsgThread.getBackMsg());
        exchangeThread.setEndFlag(endFlag);
        exchangeThread.setInputValues(values);

        new Thread(exchangeThread).start();
        exchangeThread.setLive(true);
    }

    public List<String> getResults(){

        return exchangeThread.getResults();
    }


    public boolean isRunning(){

        if (exchangeThread != null){

            return exchangeThread.isLive();
        }

        return false;
    }
}
