package com.gui;

import com.service.TabStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TAB_HuongDan extends JPanel {

    public TAB_HuongDan() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(TabStyler.createHeader("HƯỚNG DẪN SỬ DỤNG"), BorderLayout.NORTH);

        JTextArea txtGuide = new JTextArea();
        txtGuide.setEditable(false);
        txtGuide.setLineWrap(true);
        txtGuide.setWrapStyleWord(true);
        txtGuide.setOpaque(false);
        txtGuide.setText("1. Đăng nhập với tài khoản được cấp.\n" +
                "2. Sử dụng các tab bên trái để truy cập chức năng tương ứng.\n" +
                "3. Các nút Lưu/Thêm/Sửa luôn nằm ở cuối biểu mẫu.\n" +
                "4. Với biểu đồ hoặc bảng, hãy sử dụng bộ lọc ngày và nút Tải/Xuất để lấy dữ liệu mong muốn.\n" +
                "5. Nếu cần trợ giúp thêm, liên hệ quản trị viên hệ thống.");

        JScrollPane scrollPane = new JScrollPane(txtGuide);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        TabStyler.applyContentFont(this);
    }
}
