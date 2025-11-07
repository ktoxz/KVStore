package com.gui;

import javax.swing.*;
import java.awt.*;

/*
    * @author Thanh Khôi
 */

public class TAB_BanHang extends JPanel {

    public TAB_BanHang() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("TAB BÁN HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(lblTitle, BorderLayout.CENTER);
    }
}
