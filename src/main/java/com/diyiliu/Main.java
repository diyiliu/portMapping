package com.diyiliu;

import com.diyiliu.gui.MainFrame;
import com.diyiliu.util.UIHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Description: Main
 * Author: DIYILIU
 * Update: 2017-08-16 14:10
 */
public class Main {

    public static void main(String[] args) {
        Properties properties = null;
        InputStream in = null;
        try {
            in = ClassLoader.getSystemResourceAsStream("config.properties");
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        UIHelper.beautify();
        MainFrame mainFrame = new MainFrame();
        mainFrame.setProperties(properties);
        new Thread(mainFrame).start();
    }
}
