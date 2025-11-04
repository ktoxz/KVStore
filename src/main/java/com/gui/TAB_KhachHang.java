package com.gui;

import java.awt.*;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

public class TAB_KhachHang extends JPanel {

    private JTextField txtMaKH, txtTenKH, txtSDT, txtDiemTichLuy, txtTim;
    private JRadioButton radNam, radNu;
    private JButton btnThem, btnSua, btnXoa, btnTim;
    private JTable tableNV;
    private DefaultTableModel modelNV;

    public TAB_KhachHang() {
        setLayout(new BorderLayout());

        // ===== Center Panel =====
        JPanel pCenter = new JPanel(new BorderLayout());

        // ===== LEFT PANEL (Form nhập) =====
        JPanel pLeft = new JPanel();
        Font font = new Font("Arial", Font.BOLD, 18);

        TitledBorder border_left = BorderFactory.createTitledBorder("Thông tin khách hàng");
        border_left.setTitleColor(Color.BLUE);
        border_left.setTitleFont(font);
        pLeft.setBorder(border_left);

        Box box = Box.createVerticalBox();

        int labelWidth = 120;
        Dimension sizeTxt = new Dimension(180, 25);
        Dimension sizeBtn = new Dimension(130, 30);

        // Mã KH
        Box b1 = Box.createHorizontalBox();
        JLabel lblMa = new JLabel("Mã khách hàng:");
        lblMa.setPreferredSize(new Dimension(labelWidth, 25));
        txtMaKH = new JTextField();
        txtMaKH.setPreferredSize(sizeTxt);
        txtMaKH.setEditable(false);
        b1.add(lblMa); b1.add(txtMaKH);

        // Tên KH
        Box b2 = Box.createHorizontalBox();
        JLabel lblTen = new JLabel("Họ tên:");
        lblTen.setPreferredSize(new Dimension(labelWidth, 25));
        txtTenKH = new JTextField();
        txtTenKH.setPreferredSize(sizeTxt);
        b2.add(lblTen); b2.add(txtTenKH);

        // Giới tính
        Box b3 = Box.createHorizontalBox();
        JLabel lblGT = new JLabel("Giới tính:");
        lblGT.setPreferredSize(new Dimension(labelWidth, 25));
        radNam = new JRadioButton("Nam", true);
        radNu = new JRadioButton("Nữ");
        ButtonGroup groupGT = new ButtonGroup();
        groupGT.add(radNam); groupGT.add(radNu);
        Box genderBox = Box.createHorizontalBox();
        genderBox.add(radNam); genderBox.add(Box.createHorizontalStrut(10)); genderBox.add(radNu);
        b3.add(lblGT); b3.add(genderBox);

        // SĐT
        Box b4 = Box.createHorizontalBox();
        JLabel lblSDT = new JLabel("SĐT:");
        lblSDT.setPreferredSize(new Dimension(labelWidth, 25));
        txtSDT = new JTextField();
        txtSDT.setPreferredSize(sizeTxt);
        b4.add(lblSDT); b4.add(txtSDT);

        // Ngày tạo
        Box b5 = Box.createHorizontalBox();
        JLabel lblNgay = new JLabel("Ngày tạo:");
        lblNgay.setPreferredSize(new Dimension(labelWidth, 25));
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setDate(new Date());
        dateChooser.setPreferredSize(sizeTxt);
        b5.add(lblNgay); b5.add(dateChooser);

        // Điểm tích lũy
        Box b6 = Box.createHorizontalBox();
        JLabel lblDiem = new JLabel("Điểm tích lũy:");
        lblDiem.setPreferredSize(new Dimension(labelWidth, 25));
        txtDiemTichLuy = new JTextField();
        txtDiemTichLuy.setPreferredSize(sizeTxt);
        txtDiemTichLuy.setEditable(false);
        b6.add(lblDiem); b6.add(txtDiemTichLuy);

        // Buttons
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnThem.setPreferredSize(sizeBtn);
        btnSua.setPreferredSize(sizeBtn);
        btnXoa.setPreferredSize(sizeBtn);

        Box bBtns = Box.createHorizontalBox();
        bBtns.add(btnThem);
        bBtns.add(Box.createHorizontalStrut(10));
        bBtns.add(btnSua);
        bBtns.add(Box.createHorizontalStrut(10));
        bBtns.add(btnXoa);

        // Add to vertical box
        box.add(b1); box.add(Box.createVerticalStrut(8));
        box.add(b2); box.add(Box.createVerticalStrut(8));
        box.add(b3); box.add(Box.createVerticalStrut(8));
        box.add(b4); box.add(Box.createVerticalStrut(8));
        box.add(b5); box.add(Box.createVerticalStrut(8));
        box.add(b6); box.add(Box.createVerticalStrut(12));
        box.add(bBtns);

        pLeft.add(box, BorderLayout.NORTH);

        // ===== RIGHT PANEL (Table + search) =====
        JPanel pRight = new JPanel(new BorderLayout());
        TitledBorder border_right = BorderFactory.createTitledBorder("Danh sách khách hàng");
        border_right.setTitleColor(Color.BLUE);
        border_right.setTitleFont(font);
        pRight.setBorder(border_right);

        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblTim = new JLabel("Tìm khách:");
        lblTim.setPreferredSize(new Dimension(100, 25));
        txtTim = new JTextField(20);
        btnTim = new JButton("Tìm");

        pSearch.add(lblTim);
        pSearch.add(txtTim);
        pSearch.add(btnTim);

        String[] header = {"Mã KH", "Tên KH", "SĐT", "Điểm tích lũy", "Ngày tạo"};
        modelNV = new DefaultTableModel(header, 0);
        tableNV = new JTable(modelNV);
        JScrollPane pane = new JScrollPane(tableNV);

        pRight.add(pSearch, BorderLayout.NORTH);
        pRight.add(pane, BorderLayout.CENTER);

        // Add to center
        pCenter.add(pLeft, BorderLayout.WEST);
        pCenter.add(pRight, BorderLayout.CENTER);

        add(pCenter, BorderLayout.CENTER);
    }
}
