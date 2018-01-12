package com.diyiliu.gui;

import com.diyiliu.gui.dl.WaitDialog;
import com.diyiliu.model.Host;
import com.diyiliu.model.MapperModel;
import com.diyiliu.model.Pair;
import com.diyiliu.util.TelnetUtil;
import com.diyiliu.util.UIHelper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.java.accessibility.util.GUIInitializedListener;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
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
    private JButton btReload;

    private MapperModel mapperModel;

    private TelnetUtil telnetUtil;
    private Properties properties;

    private WaitDialog waitDialog;

    public MainFrame() {
        this.setContentPane(plContainer);

        mapperModel = new MapperModel();
        tbList.setModel(mapperModel);

        btMapping.addActionListener(this);
        btMapping.setActionCommand("toMapping");

        btReload.addActionListener(this);
        btReload.setActionCommand("toReload");

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
                return;
            }

            String protocol = cbxProtocol.getSelectedItem().toString();

            Host inHost = new Host(inIp, inPort);
            Host outHost = new Host(outIp, outPort);

            Pair pair = new Pair();
            pair.setProtocol(protocol);
            pair.setInside(inHost);
            pair.setOutside(outHost);

            if (toDoMapping(pair, mapperModel.getPairs())) {
                doRunning("#", "int e0/0");
                doRunning("#",  pair.toString());
                List<String> list = telnetUtil.getResults();
                for (String rs : list) {
                    if (rs.contains("ERROR:")) {

                        JOptionPane.showMessageDialog(this, "映射失败:" + rs, "提示", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                doRunning("#", "wr");
                list = telnetUtil.getResults();
                for (String rs : list) {
                    if (rs.contains("OK")) {

                        JOptionPane.showMessageDialog(this, "映射成功!", "提示", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
        }

        if ("toReload".equals(e.getActionCommand())) {
            waitDialog = new WaitDialog(telnetUtil, mapperModel);
            new Thread(waitDialog).start();
            waitDialog.setVisible(true);
        }
    }

    @Override
    public void run() {
        telnetUtil = new TelnetUtil((String) properties.get("host"), Integer.parseInt((String) properties.get("port")));

        doRunning(">", (String) properties.get("pw"));
        doRunning("Password", "en");
        doRunning("#", (String) properties.get("pw.en"));
        doRunning("#", "conf t");

        waitDialog = new WaitDialog(telnetUtil, mapperModel);
        new Thread(waitDialog).start();
        waitDialog.setVisible(true);
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

    public void doRunning(String endFlag, String input){
        telnetUtil.run(endFlag, input);
        while (telnetUtil.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        UIHelper.beautify();
        MainFrame mainFrame = new MainFrame();
        new Thread(mainFrame).start();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
