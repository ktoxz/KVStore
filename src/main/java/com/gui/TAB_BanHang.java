package com.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
 * Giao diện tab Bán Hàng (POS - nhập tiền khách trả, auto tính tiền thối)
 * @author Thanh Khôi
 */
public class TAB_BanHang extends JPanel {

    private double tongTien = 283000;
    private double tienKhachTra = 0;

    private JTextField txtKhachTraField;
    private JLabel lblTienThoiValue;
    private JPanel pnlMenhGia;
    private JLabel lblKhachTraRow, lblTienThoiRow;

    private final DecimalFormat df = new DecimalFormat("#,###");

    public TAB_BanHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== HEADER =====
        JLabel lblTitle = new JLabel("BÁN HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 90, 200));
        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeader.setBackground(Color.WHITE);
        pHeader.add(lblTitle);
        add(pHeader, BorderLayout.NORTH);

        // ===== SPLIT CHÍNH =====
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(750);
        splitMain.setResizeWeight(0.7);
        splitMain.setEnabled(false);
        splitMain.setDividerSize(0);

        // ===== PANEL TRÁI =====
        JPanel pnlLeft = new JPanel(new BorderLayout(8, 8));
        pnlLeft.setBackground(Color.WHITE);

        // Thanh tìm kiếm
        JPanel pSearch = new JPanel(new BorderLayout(8, 8));
        pSearch.setBackground(Color.WHITE);
        JTextField txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton btnAdd = new JButton("+ Thêm hàng hóa");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setBackground(new Color(0, 120, 215));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setPreferredSize(new Dimension(160, 36));
        pSearch.add(txtSearch, BorderLayout.CENTER);
        pSearch.add(btnAdd, BorderLayout.EAST);
        pnlLeft.add(pSearch, BorderLayout.NORTH);

        // Bảng sản phẩm
        String[] cols = {"STT", "Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel mdlTable = new DefaultTableModel(cols, 0);
        JTable table = new JTable(mdlTable);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);
        pnlLeft.add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== PANEL PHẢI =====
        JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBackground(Color.WHITE);

        // === THÔNG TIN KHÁCH HÀNG ===
        JPanel pnlKHHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlKHHeader.setBackground(Color.WHITE);
        JLabel lblKH = new JLabel("THÔNG TIN KHÁCH HÀNG");
        lblKH.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnlKHHeader.add(lblKH);

        JPanel pnlKH = new JPanel(new GridBagLayout());
        pnlKH.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPhone = new JLabel("SĐT KH:");
        JLabel lblName = new JLabel("Tên KH:");
        JLabel lblNote = new JLabel("Ghi chú:");
        JTextField txtPhone = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtNote = new JTextField();

        gbc.gridx = 0; gbc.gridy = 0; pnlKH.add(lblPhone, gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlKH.add(txtPhone, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; pnlKH.add(lblName, gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlKH.add(txtName, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; pnlKH.add(lblNote, gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlKH.add(txtNote, gbc);

        // === THÔNG TIN THANH TOÁN ===
        JPanel pnlPayHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlPayHeader.setBackground(Color.WHITE);
        JLabel lblPay = new JLabel("THÔNG TIN THANH TOÁN");
        lblPay.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnlPayHeader.add(lblPay);

        JPanel pnlTotal = new JPanel(new GridBagLayout());
        pnlTotal.setBackground(new Color(248, 249, 252));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fLabel = new Font("Segoe UI", Font.BOLD, 14);

        addRowLabel(pnlTotal, gbc, 0, "Thành tiền:", df.format(tongTien), fLabel);
        addRowLabel(pnlTotal, gbc, 1, "VAT (8%):", "0", fLabel);
        addRowLabel(pnlTotal, gbc, 2, "Khuyến mãi:", "0", fLabel);
        addRowLabel(pnlTotal, gbc, 3, "Khách cần trả:", df.format(tongTien), fLabel);

        // Hình thức thanh toán
        JLabel lblHinhThuc = new JLabel("Hình thức:");
        lblHinhThuc.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 4; pnlTotal.add(lblHinhThuc, gbc);
        JRadioButton rbCash = new JRadioButton("Tiền mặt", true);
        JRadioButton rbBank = new JRadioButton("Chuyển khoản");
        rbCash.setBackground(pnlTotal.getBackground());
        rbBank.setBackground(pnlTotal.getBackground());
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbCash); bg.add(rbBank);
        JPanel pType = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pType.setBackground(pnlTotal.getBackground());
        pType.add(rbCash); pType.add(rbBank);
        gbc.gridx = 1; pnlTotal.add(pType, gbc);

        // ==== TIỀN KHÁCH TRẢ & TIỀN THỐI ====
        lblKhachTraRow = new JLabel("Tiền khách trả:");
        lblKhachTraRow.setFont(fLabel);
        lblTienThoiRow = new JLabel("Tiền thối:");
        lblTienThoiRow.setFont(fLabel);

        txtKhachTraField = new JTextField("0");
        txtKhachTraField.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhachTraField.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        txtKhachTraField.setHorizontalAlignment(JTextField.RIGHT);
        txtKhachTraField.setPreferredSize(new Dimension(120, 26));

        lblTienThoiValue = new JLabel(df.format(0));
        lblTienThoiValue.setFont(fLabel);

        // Cho phép nhập và tự format
        txtKhachTraField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtKhachTraField.getText().replaceAll("[^0-9]", "");
                if (text.isEmpty()) text = "0";
                try {
                    tienKhachTra = Double.parseDouble(text);
                } catch (NumberFormatException ex) {
                    tienKhachTra = 0;
                }
                txtKhachTraField.setText(df.format(tienKhachTra));
                capNhatTienThoi();
            }
        });

        gbc.gridx = 0; gbc.gridy = 5; pnlTotal.add(lblKhachTraRow, gbc);
        gbc.gridx = 1; pnlTotal.add(txtKhachTraField, gbc);
        gbc.gridx = 0; gbc.gridy = 6; pnlTotal.add(lblTienThoiRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblTienThoiValue, gbc);

        // ==== MỆNH GIÁ ====
        pnlMenhGia = new JPanel(new GridLayout(3, 3, 6, 6));
        pnlMenhGia.setBackground(new Color(248, 249, 252));
        int[] menhGia = {1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000};
        for (int v : menhGia) {
            JButton btn = new JButton(v / 1000 + "k");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) tienKhachTra += v;
                    else if (SwingUtilities.isRightMouseButton(e)) tienKhachTra -= v;
                    if (tienKhachTra < 0) tienKhachTra = 0;
                    txtKhachTraField.setText(df.format(tienKhachTra));
                    capNhatTienThoi();
                }
            });
            pnlMenhGia.add(btn);
        }
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; pnlTotal.add(pnlMenhGia, gbc);

        // ==== NÚT HÀNH ĐỘNG ====
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setBackground(pnlTotal.getBackground());
        JButton btnPay = new JButton("Thanh toán");
        JButton btnClear = new JButton("Xóa tất cả");
        styleButton(btnPay, new Color(27, 160, 79));
        styleButton(btnClear, new Color(220, 53, 69));
        pnlButtons.add(btnPay);
        pnlButtons.add(btnClear);
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        pnlTotal.add(pnlButtons, gbc);

        // Sự kiện
        rbCash.addActionListener(e -> setMenhGiaVisible(true));
        rbBank.addActionListener(e -> setMenhGiaVisible(false));

        btnClear.addActionListener(e -> {
            tienKhachTra = 0;
            txtKhachTraField.setText("0");
            capNhatTienThoi();
        });

        // === GẮN VÀO PANEL PHẢI ===
        pnlRight.add(pnlKHHeader);
        pnlRight.add(pnlKH);
        pnlRight.add(Box.createVerticalStrut(5));
        pnlRight.add(pnlPayHeader);
        pnlRight.add(pnlTotal);

        splitMain.setLeftComponent(pnlLeft);
        splitMain.setRightComponent(pnlRight);
        add(splitMain, BorderLayout.CENTER);
    }

    // ===== HÀM HỖ TRỢ =====
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 36));
    }

    private void addRowLabel(JPanel panel, GridBagConstraints gbc, int row, String label, String value, Font font) {
        JLabel lblL = new JLabel(label);
        lblL.setFont(font);
        JLabel lblV = new JLabel(value);
        lblV.setFont(font);
        gbc.gridx = 0; gbc.gridy = row; panel.add(lblL, gbc);
        gbc.gridx = 1; panel.add(lblV, gbc);
    }

    private void capNhatTienThoi() {
        double thoi = tienKhachTra - tongTien;
        if (thoi < 0) thoi = 0;
        lblTienThoiValue.setText(df.format(thoi));
    }

    private void setMenhGiaVisible(boolean visible) {
        pnlMenhGia.setVisible(visible);
        txtKhachTraField.setVisible(visible);
        lblTienThoiValue.setVisible(visible);
        lblKhachTraRow.setVisible(visible);
        lblTienThoiRow.setVisible(visible);
    }
}
