package com.diyiliu.thread;

import javax.swing.*;

/**
 * Description: DrawThread
 * Author: DIYILIU
 * Update: 2017-08-22 15:38
 */
public class DrawThread  implements Runnable {

    private boolean flag = true;
    private int n = 0;

    private JPanel drawPanel;
    private JLabel lbMsg;

    public DrawThread(JPanel drawPanel, JLabel lbMsg) {
        this.drawPanel = drawPanel;
        this.lbMsg = lbMsg;
    }

    @Override
    public void run() {

        while (true) {
            lbMsg.setText(createMsg());
            drawPanel.repaint();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!flag) {
                break;
            }
        }
    }

    public String createMsg() {
        String content = "数据加载中";
        for (int i = 0; i < n % 5; i++) {
            content += "  ";
        }
        content += ".";
        n++;
        if (n == 10000) {
            n = 0;
        }

        return content;
    }

    public void stop(){
        flag = false;
    }
}
