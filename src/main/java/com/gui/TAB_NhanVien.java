package com.gui;

import javax.swing.*;
import java.awt.*;

public class TAB_NhanVien extends JPanel {

    public TAB_NhanVien() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("TAB HƯỚNG DẪN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(lblTitle, BorderLayout.CENTER);
    }
}
