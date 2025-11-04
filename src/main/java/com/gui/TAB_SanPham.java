package com.gui;

import javax.swing.*;
import java.awt.*;

public class TAB_SanPham extends JPanel {

    public TAB_SanPham() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("TAB SẢN PHẨM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(lblTitle, BorderLayout.CENTER);
    }
}
