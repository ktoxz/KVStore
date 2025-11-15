package com;

import com.connectDB.ConnectDB;
import com.dao.DAO_CT_KhuyenMai;
import com.dao.DAO_SanPham;
import com.entity.CT_KhuyenMai;
import com.entity.SanPham;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.gui.GUI_Login;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Failed to connect to database: " + ex.getMessage(),
                    "DB Connection Error",
                    JOptionPane.ERROR_MESSAGE));
            ex.printStackTrace();
            
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ConnectDB.getInstance().disconnect();
            } catch (Exception ignored) {}
        }));

        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception ignored) {}



        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KVStore - Đăng nhập");
            GUI_Login loginPanel = new GUI_Login();
            loginPanel.setParentFrame(frame);
            frame.setContentPane(loginPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1080, 600);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
