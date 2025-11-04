package com;

import com.connectDB.ConnectDB;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.gui.GUI_Login;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Connect to the database first
        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException ex) {
            // Show a dialog and print stack trace; continue to show GUI even if DB fails
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Failed to connect to database: " + ex.getMessage(),
                    "DB Connection Error",
                    JOptionPane.ERROR_MESSAGE));
            ex.printStackTrace();
        }

        // Ensure DB disconnects when the app exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ConnectDB.getInstance().disconnect();
            } catch (Exception ignored) {
            }
        }));

        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception ignored) {
        }

        // Launch GUI on Event Dispatch Thread
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