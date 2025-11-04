package com.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TAB_KhuyenMai extends JPanel {

    private JTextField txtMaKM, txtTenCT, txtDieuKien, txtNgayBD, txtNgayKT;
    private JComboBox<String> cbLoaiCT;
    private JTable tblDSKM, tblChiTiet;
    private JTextField txtMaSP, txtTiLe, txtNgayMua, txtMaKMCT, txtTim;
    private JButton btnThem, btnSua, btnXoa, btnMoi, btnThemCT, btnSuaCT, btnXoaCT, btnLuu, btnTim;

    public TAB_KhuyenMai() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề
        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 90, 200));

        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeader.setBackground(Color.WHITE);
        pHeader.add(lblTitle);
        add(pHeader, BorderLayout.NORTH);

        // Split main - Sử dụng HORIZONTAL_SPLIT thay vì VERTICAL_SPLIT
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(600); // Vị trí divider (có thể điều chỉnh)
        split.setResizeWeight(0.5); // Tỷ lệ resize 50-50
        split.setOneTouchExpandable(true); // Cho phép mở rộng/thu nhỏ nhanh
        split.setLeftComponent(createPanelChuongTrinh());
        split.setRightComponent(createPanelChiTiet());
        add(split, BorderLayout.CENTER);
    }

    // =====================================
    private JPanel createPanelChuongTrinh() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "THÔNG TIN CHƯƠNG TRÌNH KHUYẾN MÃI",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0, 70, 160)));

        // FORM GRID - Chuyển sang layout dọc để phù hợp với split horizontal
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtMaKM = new JTextField(15);
        txtTenCT = new JTextField(15);
        txtDieuKien = new JTextField(15);
        txtNgayBD = new JTextField(15);
        txtNgayKT = new JTextField(15);
        cbLoaiCT = new JComboBox<>(new String[]{
                "Giảm % trên sản phẩm", "Giảm tiền trên sản phẩm",
                "Giảm % theo số lượng", "Tặng sản phẩm khi đủ tiền",
                "Giảm tiền theo hóa đơn", "Giảm % trên hóa đơn"
        });

        // Layout dạng dọc (1 cột 2 trường)
        int row = 0;
        addFieldVertical(form, gbc, row++, "Mã khuyến mãi:", txtMaKM);
        addFieldVertical(form, gbc, row++, "Loại CT:", cbLoaiCT);
        addFieldVertical(form, gbc, row++, "Tên CT:", txtTenCT);
        addFieldVertical(form, gbc, row++, "Điều kiện:", txtDieuKien);
        addFieldVertical(form, gbc, row++, "Ngày bắt đầu:", txtNgayBD);
        addFieldVertical(form, gbc, row, "Ngày kết thúc:", txtNgayKT);

        // Nút
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pButtons.setBackground(Color.WHITE);
        btnThem = makeButton("Thêm", new Color(27, 160, 79));
        btnSua = makeButton("Sửa", new Color(0, 120, 215));
        btnXoa = makeButton("Xóa", new Color(200, 50, 50));
        btnMoi = makeButton("Làm mới", new Color(255, 140, 0));
        pButtons.add(btnThem);
        pButtons.add(btnSua);
        pButtons.add(btnXoa);
        pButtons.add(btnMoi);

        // Table
        String[] cols = {"Mã KM", "Tên CT", "Loại CT", "Điều kiện", "Ngày BĐ", "Ngày KT"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        tblDSKM = new JTable(model);
        tblDSKM.setRowHeight(25);
        tblDSKM.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDSKM.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDSKM.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scroll = new JScrollPane(tblDSKM);
        scroll.setPreferredSize(new Dimension(0, 250));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(form, BorderLayout.NORTH);
        topPanel.add(pButtons, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =====================================
    private JPanel createPanelChiTiet() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "CHI TIẾT SẢN PHẨM ÁP DỤNG",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(0, 70, 160)));

        // FORM - Layout dọc
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtMaSP = new JTextField(15);
        txtMaKMCT = new JTextField(15);
        txtTiLe = new JTextField(15);
        txtNgayMua = new JTextField(15);
        txtTim = new JTextField(15);

        int row = 0;
        addFieldVertical(form, gbc, row++, "Mã sản phẩm:", txtMaSP);
        addFieldVertical(form, gbc, row++, "Mã khuyến mãi:", txtMaKMCT);
        addFieldVertical(form, gbc, row++, "Tỉ lệ KM (%):", txtTiLe);
        addFieldVertical(form, gbc, row, "Ngày mua:", txtNgayMua);

        // BUTTONS
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pBtns.setBackground(Color.WHITE);
        btnThemCT = makeButton("Thêm", new Color(27, 160, 79));
        btnSuaCT = makeButton("Sửa", new Color(0, 120, 215));
        btnXoaCT = makeButton("Xóa", new Color(200, 50, 50));
        btnLuu = makeButton("Lưu", new Color(255, 140, 0));

        pBtns.add(btnThemCT);
        pBtns.add(btnSuaCT);
        pBtns.add(btnXoaCT);
        pBtns.add(btnLuu);

        // Panel tìm kiếm
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pSearch.setBackground(Color.WHITE);
        btnTim = makeButton("Tìm", new Color(75, 0, 130));
        pSearch.add(new JLabel("Tìm mã SP:"));
        pSearch.add(txtTim);
        pSearch.add(btnTim);

        // Table
        String[] cols = {"Mã SP", "Mã KM", "Tỉ lệ KM", "Ngày mua"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        tblChiTiet = new JTable(model);
        tblChiTiet.setRowHeight(25);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setPreferredSize(new Dimension(0, 250));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(form, BorderLayout.NORTH);
        topPanel.add(pSearch, BorderLayout.CENTER);
        topPanel.add(pBtns, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ===================================== Helper cho layout dọc
    private void addFieldVertical(JPanel panel, GridBagConstraints gbc, int row,
                                   String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(comp, gbc);
    }

    // =====================================
    private void addField(JPanel panel, GridBagConstraints gbc, int row,
                          String lbl1, JComponent comp1, String lbl2, JComponent comp2) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(lbl1), gbc);
        gbc.gridx = 1;
        panel.add(comp1, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel(lbl2), gbc);
        gbc.gridx = 3;
        panel.add(comp2, gbc);
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(100, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Test
    public static void main(String[] args) {
        JFrame f = new JFrame("KVStore - Khuyến mãi");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200, 750);
        f.add(new TAB_KhuyenMai());
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
