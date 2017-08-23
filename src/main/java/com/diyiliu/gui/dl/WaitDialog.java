package com.diyiliu.gui.dl;

import com.diyiliu.model.Host;
import com.diyiliu.model.MapperModel;
import com.diyiliu.model.Pair;
import com.diyiliu.thread.DrawThread;
import com.diyiliu.util.TelnetUtil;
import com.diyiliu.util.UIHelper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class WaitDialog extends JDialog implements Runnable {
    private JPanel contentPane;
    private JPanel drawPanel;
    private JLabel lbWait;

    private DrawThread drawThread;

    private MapperModel mapperModel;

    private TelnetUtil telnetUtil;

    public WaitDialog(TelnetUtil telnetUtil, MapperModel mapperModel) {
        setContentPane(contentPane);
        setModal(true);
        setUndecorated(true);
        pack();
        UIHelper.setCenter(this);

        this.telnetUtil = telnetUtil;
        this.mapperModel = mapperModel;
    }

    @Override
    public void run() {
        drawThread = new DrawThread(drawPanel, lbWait);
        new Thread(drawThread).start();

        telnetUtil.run(": end", new String[]{"sh ru"});

        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!telnetUtil.isRunning()) {
                drawThread.stop();
                dispose();
                break;
            }
        }

        List list = telnetUtil.getResults();
        List<Pair> pairList = toListPair(list);
        Collections.sort(pairList);

        mapperModel.refresh(pairList);
    }

    public boolean isMatch(String content) {
        String regex = "^static \\(inside,outside\\) [tcp|udp][\\s\\S]*?";

        return Pattern.matches(regex, content);
    }

    public Pair dataFormat(String content) {
        String[] array = content.split(" ");
        if (array.length < 7) {
            return null;
        }

        String protocol = array[2];
        Host inside = new Host(array[5], array[6]);
        Host outside = new Host(array[3], array[4]);

        Pair p = new Pair();
        p.setProtocol(protocol);
        p.setInside(inside);
        p.setOutside(outside);

        return p;
    }

    /**
     * String集合转Pair集合
     *
     * @param l
     * @return
     */
    public List<Pair> toListPair(List<String> l) {
        List list = new ArrayList();
        for (int i = 0; i < l.size(); i++) {
            String content = l.get(i);
            if (isMatch(content)) {
                Pair p = dataFormat(content);
                list.add(p);
            }
        }

        return list;
    }
}
