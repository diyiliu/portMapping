package com.diyiliu.gui;

import com.diyiliu.model.Host;
import com.diyiliu.model.MapperModel;
import com.diyiliu.model.Pair;
import com.diyiliu.util.TelnetUtil;
import com.diyiliu.util.UIHelper;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Description: MainFrame
 * Author: DIYILIU
 * Update: 2017-08-16 14:14
 */
public class MainFrame extends JFrame implements ActionListener, Runnable {
    private JTable tbList;
    private JTextField tfInAddress;
    private JTextField tfInPort;
    private JTextField tfOutAddress;
    private JTextField tfOutPort;
    private JButton btMapping;
    private JComboBox cbxProtocol;
    private JPanel plContainer;

    private MapperModel mapperModel;
    private List<Pair> pairList;

    private TelnetUtil telnetUtil;

    private Properties properties;

    public MainFrame() {
        this.setContentPane(plContainer);

        mapperModel = new MapperModel();
        tbList.setModel(mapperModel);

        btMapping.addActionListener(this);
        btMapping.setActionCommand("toMapping");

        this.setSize(680, 450);
        // 设置窗口居中
        UIHelper.setCenter(this);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if ("toMapping".equals(e.getActionCommand())) {

            String inIp = tfInAddress.getText().trim();
            String inPort = tfInPort.getText().trim();
            String outIp = tfOutAddress.getText().trim();
            String outPort = tfOutPort.getText().trim();

            if (StringUtils.isBlank(inIp) || StringUtils.isBlank(inPort) ||
                    StringUtils.isBlank(outIp) || StringUtils.isBlank(outPort)) {

                JOptionPane.showMessageDialog(this, "IP或端口不能为空!", "提示", JOptionPane.WARNING_MESSAGE);
                reload();
                return;
            }


            String protocol = cbxProtocol.getSelectedItem().toString();

            Host inHost = new Host(inIp, inPort);
            Host outHost = new Host(outIp, outPort);

            Pair pair = new Pair();
            pair.setProtocol(protocol);
            pair.setInside(inHost);
            pair.setOutside(outHost);

            if (toDoMapping(pair, pairList)) {
                telnetUtil.run("(config)#", new String[]{"int e0/0", pair.toString()});
                while (telnetUtil.isRunning()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
                List<String> list = telnetUtil.getResults();
                for (String rs : list) {
                    if (rs.contains("ERROR:")) {

                        JOptionPane.showMessageDialog(this, "映射失败:" + rs, "提示", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                telnetUtil.run("[OK]", new String[]{"wr", pair.toString()});
                while (telnetUtil.isRunning()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
                list = telnetUtil.getResults();
                for (String rs : list) {
                    if (rs.contains("OK")) {

                        JOptionPane.showMessageDialog(this, "映射成功!", "提示", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
            reload();
        }
    }

    @Override
    public void run() {
        telnetUtil = new TelnetUtil((String) properties.get("host"), Integer.parseInt((String) properties.get("port")));
        telnetUtil.run("#", new String[]{(String) properties.get("pw"), "en", (String) properties.get("pw.en"), "conf t"});

        while (telnetUtil.isRunning()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        reload();
    }


    public static void main(String[] args) {
        UIHelper.beautify();
        MainFrame mainFrame = new MainFrame();
        new Thread(mainFrame).start();
    }


    public void reload() {
        telnetUtil.run(": end", new String[]{"sh ru"});

        while (telnetUtil.isRunning()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        List list = telnetUtil.getResults();

        pairList = toListPair(list);
        Collections.sort(pairList);
        mapperModel.refresh(toListArray(pairList));
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

    /**
     * 把Pair对象数组，转为Array对象数组
     *
     * @param l
     * @return
     */
    public List toListArray(List<Pair> l) {

        List list = new ArrayList();
        for (int i = 0; i < l.size(); i++) {
            Pair p = l.get(i);
            list.add(p.toArray(i + 1));
        }

        return list;
    }

    public boolean toDoMapping(Pair p, List<Pair> list) {

        for (Pair pair : list) {

            if (p.getInside().equals(pair.getInside())) {

                JOptionPane.showMessageDialog(this, "内网地址已存在映射!", "提示", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if (p.getOutside().equals(pair.getOutside())) {
                JOptionPane.showMessageDialog(this, "外网地址已存在映射!", "提示", JOptionPane.WARNING_MESSAGE);
                return false;
            }

        }

        return true;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
