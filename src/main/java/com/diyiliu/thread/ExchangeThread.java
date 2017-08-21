package com.diyiliu.thread;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Description: ExchangeThread
 * Author: DIYILIU
 * Update: 2017-08-17 09:32
 */
public class ExchangeThread implements Runnable {

    private OutputStream os;
    private String[] inputValues;
    private Queue queue;
    private String endFlag;

    public ExchangeThread(OutputStream os) {
        this.os = os;
    }

    public ExchangeThread(OutputStream os, String[] inputValues, Queue queue, String endFlag) {
        this.os = os;
        this.inputValues = inputValues;
        this.queue = queue;
        this.endFlag = endFlag;
    }

    private List<String> results = new ArrayList<>();

    private boolean live = false;

    @Override
    public void run() {
        results.clear();
        live = true;

        LinkedList<String> inputList = new LinkedList();
        inputList.addAll(Arrays.asList(inputValues));

        while (true) {
            String content = "";
            if (!queue.isEmpty()) {
                content = (String) queue.poll();
                results.add(content);

                //System.out.println(content);
            } else {
                write(" ", os);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!inputList.isEmpty()){
                    write(inputList.poll(), os);
                }
            }

            if (inputList.isEmpty() && content.contains(endFlag)) {
                System.out.println("输入完成!");
                live = false;
                break;
            }
        }
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public void setInputValues(String[] inputValues) {
        this.inputValues = inputValues;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public void setEndFlag(String endFlag) {
        this.endFlag = endFlag;
    }

    /**
     * 写入命令方法
     *
     * @param cmd
     * @param os
     */
    public void write(String cmd, OutputStream os) {
        try {
            cmd = cmd + "\n";
            os.write(cmd.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLive() {
        return live;
    }

    public List<String> getResults() {
        return results;
    }
}
