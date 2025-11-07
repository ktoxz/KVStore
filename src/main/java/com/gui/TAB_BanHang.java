package com.gui;

import com.dao.DAO_SanPham;
import com.entity.SanPham;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TAB_BanHang extends JPanel {

    private double tongTien = 0;
    private double tienKhachTra = 0;

    private JTextField txtKhachTraField;
    private JLabel lblTienThoiValue;
    private JLabel lblTongTien;
    private JPanel pnlMenhGia;
    private JLabel lblKhachTraRow, lblTienThoiRow;
    private JTable table;
    private DefaultTableModel mdlTable;
    private final DecimalFormat df = new DecimalFormat("#,###");

    private DAO_SanPham daoSP = new DAO_SanPham();
    private JPopupMenu popupSearch; // Di chuyển ra ngoài để tái sử dụng

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
        txtSearch.setToolTipText("Nhập tên sản phẩm...");
        pSearch.add(txtSearch, BorderLayout.CENTER);
        pnlLeft.add(pSearch, BorderLayout.NORTH);

        // Popup kết quả tìm kiếm
        popupSearch = new JPopupMenu();
        popupSearch.setFocusable(false);
        popupSearch.setLayout(new BoxLayout(popupSearch, BoxLayout.Y_AXIS));

        // Bộ đếm thời gian debounce 300ms
        final Timer timer = new Timer();
        final int DELAY = 100;
        final KeyAdapter searchListener = new KeyAdapter() {
            TimerTask lastTask;
            @Override
            public void keyReleased(KeyEvent e) {
                if (lastTask != null) lastTask.cancel();
                lastTask = new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            String kw = txtSearch.getText().trim();
                            popupSearch.setVisible(false); // Ẩn trước khi cập nhật
                            popupSearch.removeAll();
                            if (kw.isEmpty()) {
                                return;
                            }

                            List<SanPham> ds = daoSP.findByTen(kw);
                            if (ds.isEmpty()) {
                                JMenuItem none = new JMenuItem("Không tìm thấy sản phẩm");
                                none.setEnabled(false);
                                popupSearch.add(none);
                            } else {
                                for (SanPham sp : ds) {
                                    // Tạo custom menu item với icon và text
                                    JMenuItem menuItem = new JMenuItem();
                                    menuItem.setPreferredSize(new Dimension(350, 60));
                                    menuItem.setBackground(Color.WHITE);
                                    
                                    // Load icon cho menu item
                                    ImageIcon icon = null;
                                    try {
                                        String imagePath = sp.getHinhAnhSP();
                                        if (imagePath != null && !imagePath.trim().isEmpty()) {
                                            java.net.URL imageURL = getClass().getResource("/sp_image/" + imagePath);
                                            if (imageURL != null) {
                                                ImageIcon originalIcon = new ImageIcon(imageURL);
                                                Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                                                icon = new ImageIcon(scaledImage);
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                    
                                    if (icon != null) {
                                        menuItem.setIcon(icon);
                                    }
                                    
                                    // Tạo HTML text để hiển thị tên và giá
                                    String htmlText = "<html><div style='padding:5px;'>" +
                                            "<div style='font-weight:bold; font-size:13px;'>" + sp.getTenSP() + "</div>" +
                                            "<div style='color:#dc3545; font-size:12px; margin-top:3px;'>" + df.format(sp.getGiaSP()) + "đ</div>" +
                                            "</div></html>";
                                    menuItem.setText(htmlText);
                                    
                                    menuItem.addActionListener(ev -> {
                                        themSanPham(sp);
                                        popupSearch.setVisible(false);
                                        txtSearch.setText(""); // Xóa ô tìm kiếm sau khi chọn
                                        txtSearch.requestFocus();
                                    });
                                    popupSearch.add(menuItem);
                                }
                            }
                            // Hiển thị lại popup với nội dung mới
                            popupSearch.pack(); // Đảm bảo kích thước phù hợp
                            popupSearch.show(txtSearch, 0, txtSearch.getHeight());
                        });
                    }
                };
                timer.schedule(lastTask, DELAY);
            }
        };
        txtSearch.addKeyListener(searchListener);

        // Ẩn popup khi txtSearch mất focus
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Delay nhỏ để cho phép click vào item trong popup
                SwingUtilities.invokeLater(() -> {
                    if (!popupSearch.isVisible()) return;
                    popupSearch.setVisible(false);
                });
            }
        });

        // ===== BẢNG SẢN PHẨM =====
        String[] cols = {"STT", "Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Thành tiền", ""};
        mdlTable = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 3 || col == 6;
            }
        };

        table = new JTable(mdlTable);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);

        // Renderer cho nút X
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Lắng nghe thay đổi số lượng
        mdlTable.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && e.getColumn() == 3) {
                    try {
                        String qtyStr = mdlTable.getValueAt(row, 3).toString().trim();
                        if (qtyStr.isEmpty()) {
                            mdlTable.setValueAt(1, row, 3);
                            return;
                        }
                        int qty = Integer.parseInt(qtyStr);
                        if (qty < 1) {
                            mdlTable.setValueAt(1, row, 3);
                            qty = 1;
                        }
                        String priceStr = mdlTable.getValueAt(row, 4).toString().replace(",", "").replace(".", "");
                        double price = Double.parseDouble(priceStr);
                        double thanhTien = qty * price;
                        mdlTable.setValueAt(df.format(thanhTien), row, 5);
                        capNhatTongTien();
                    } catch (Exception ex) {
                        mdlTable.setValueAt(1, row, 3);
                    }
                }
            }
        });

        pnlLeft.add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== PANEL PHẢI =====
        JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBackground(Color.WHITE);

        // --- Thông tin khách hàng ---
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

        // --- Thanh toán ---
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

        JLabel lblThanhTienRow = new JLabel("Tổng tiền:");
        lblThanhTienRow.setFont(fLabel);
        lblTongTien = new JLabel(df.format(0));
        lblTongTien.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 0; pnlTotal.add(lblThanhTienRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblTongTien, gbc);

        // Hình thức thanh toán
        JLabel lblHinhThuc = new JLabel("Hình thức:");
        lblHinhThuc.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 1; pnlTotal.add(lblHinhThuc, gbc);
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

        // Tiền khách trả & tiền thối
        lblKhachTraRow = new JLabel("Tiền khách trả:");
        lblKhachTraRow.setFont(fLabel);
        lblTienThoiRow = new JLabel("Tiền thối:");
        lblTienThoiRow.setFont(fLabel);

        txtKhachTraField = new JTextField("0");
        txtKhachTraField.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhachTraField.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblTienThoiValue = new JLabel(df.format(0));
        lblTienThoiValue.setFont(fLabel);

        gbc.gridx = 0; gbc.gridy = 2; pnlTotal.add(lblKhachTraRow, gbc);
        gbc.gridx = 1; pnlTotal.add(txtKhachTraField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; pnlTotal.add(lblTienThoiRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblTienThoiValue, gbc);

        // --- Mệnh giá ---
        pnlMenhGia = new JPanel(new GridLayout(3, 3, 6, 6));
        pnlMenhGia.setBackground(pnlTotal.getBackground());
        int[] menhGia = {1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000};
        for (int v : menhGia) {
            JButton btn = new JButton(v / 1000 + "k");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.addActionListener(e -> {
                tienKhachTra += v;
                txtKhachTraField.setText(df.format(tienKhachTra));
                capNhatTienThoi();
            });
            pnlMenhGia.add(btn);
        }
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; pnlTotal.add(pnlMenhGia, gbc);

        // Nút hành động
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setBackground(pnlTotal.getBackground());
        JButton btnPay = new JButton("Thanh toán");
        JButton btnClear = new JButton("Xóa tất cả");
        styleButton(btnPay, new Color(27, 160, 79));
        styleButton(btnClear, new Color(220, 53, 69));
        pnlButtons.add(btnPay);
        pnlButtons.add(btnClear);
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        pnlTotal.add(pnlButtons, gbc);

        // Gắn sự kiện
        rbCash.addActionListener(e -> setMenhGiaVisible(true));
        rbBank.addActionListener(e -> setMenhGiaVisible(false));
        btnClear.addActionListener(e -> {
            tienKhachTra = 0;
            txtKhachTraField.setText("0");
            mdlTable.setRowCount(0);
            tongTien = 0;
            lblTongTien.setText("0");
            capNhatTienThoi();
        });

        // Cập nhật tiền khách trả
        txtKhachTraField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String text = txtKhachTraField.getText().replaceAll("[^0-9]", "");
                if (text.isEmpty()) text = "0";
                tienKhachTra = Double.parseDouble(text);
                txtKhachTraField.setText(df.format(tienKhachTra));
                capNhatTienThoi();
            }
        });

        // --- Gắn tất cả vào panel phải ---
        pnlRight.add(pnlKHHeader);
        pnlRight.add(pnlKH);
        pnlRight.add(Box.createVerticalStrut(5));
        pnlRight.add(pnlPayHeader);
        pnlRight.add(pnlTotal);

        // Gắn vào split
        splitMain.setLeftComponent(pnlLeft);
        splitMain.setRightComponent(pnlRight);
        add(splitMain, BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 36));
    }

    private void themSanPham(SanPham sp) {
        // Kiểm tra xem sản phẩm đã có trong bảng chưa
        for (int i = 0; i < mdlTable.getRowCount(); i++) {
            if (mdlTable.getValueAt(i, 1).equals(sp.getMaSP())) {
                int sl = Integer.parseInt(mdlTable.getValueAt(i, 3).toString()) + 1;
                mdlTable.setValueAt(sl, i, 3);
                double gia = sp.getGiaSP();
                double thanhTien = sl * gia;
                mdlTable.setValueAt(df.format(thanhTien), i, 5);
                capNhatTongTien();
                return;
            }
        }
        // Thêm sản phẩm mới
        int stt = mdlTable.getRowCount() + 1;
        mdlTable.addRow(new Object[]{stt, sp.getMaSP(), sp.getTenSP(), 1,
                df.format(sp.getGiaSP()), df.format(sp.getGiaSP()), "X"});
        capNhatTongTien();
    }

    private void capNhatTongTien() {
        tongTien = 0;
        for (int i = 0; i < mdlTable.getRowCount(); i++) {
            try {
                // Lấy giá trị từ cột "Thành tiền" và xóa dấu phân cách
                String thanhTienStr = mdlTable.getValueAt(i, 5).toString().replace(",", "").replace(".", "").trim();
                if (!thanhTienStr.isEmpty()) {
                    double thanhTien = Double.parseDouble(thanhTienStr);
                    tongTien += thanhTien;
                }
            } catch (Exception e) {
                // Nếu có lỗi, tính lại từ số lượng và đơn giá
                try {
                    int qty = Integer.parseInt(mdlTable.getValueAt(i, 3).toString());
                    String priceStr = mdlTable.getValueAt(i, 4).toString().replace(",", "").replace(".", "").trim();
                    double price = Double.parseDouble(priceStr);
                    tongTien += qty * price;
                } catch (Exception ex) {
                    // Bỏ qua hàng này nếu không thể tính
                }
            }
        }
        lblTongTien.setText(df.format(tongTien));
        capNhatTienThoi();
    }

    private void capNhatTienThoi() {
        double thoi = tienKhachTra - tongTien;
        if (thoi < 0) thoi = 0;
        lblTienThoiValue.setText(df.format(thoi));
    }

    private void capNhatSTT() {
        for (int i = 0; i < mdlTable.getRowCount(); i++) {
            mdlTable.setValueAt(i + 1, i, 0);
        }
    }

    private JPanel createSearchResultPanel(SanPham sp) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        // Ảnh sản phẩm bên trái
        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(50, 50));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);

        // Tải ảnh sản phẩm
        try {
            String imagePath = sp.getHinhAnhSP();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                // Thử load ảnh từ resources
                java.net.URL imageURL = getClass().getResource("/sp_image/" + imagePath);
                if (imageURL != null) {
                    ImageIcon icon = new ImageIcon(imageURL);
                    // Scale ảnh về 50x50
                    Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    lblImage.setIcon(new ImageIcon(scaledImage));
                } else {
                    // Nếu không tìm thấy ảnh, hiển thị icon mặc định
                    lblImage.setText("📦");
                    lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 30));
                }
            } else {
                // Không có ảnh, hiển thị icon mặc định
                lblImage.setText("📦");
                lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 30));
            }
        } catch (Exception e) {
            // Lỗi khi load ảnh
            lblImage.setText("📦");
            lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        }

        panel.add(lblImage, BorderLayout.WEST);

        // Panel chứa tên và giá bên phải
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel lblName = new JLabel(sp.getTenSP());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrice = new JLabel(df.format(sp.getGiaSP()) + "đ");
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPrice.setForeground(new Color(220, 53, 69));
        lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblPrice);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private void setMenhGiaVisible(boolean visible) {
        pnlMenhGia.setVisible(visible);
        txtKhachTraField.setVisible(visible);
        lblTienThoiValue.setVisible(visible);
        lblKhachTraRow.setVisible(visible);
        lblTienThoiRow.setVisible(visible);
    }

    // Renderer cho cột X
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("X");
            setForeground(Color.WHITE);
            setBackground(new Color(220, 53, 69));
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) { return this; }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("X");
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(220, 53, 69));
            button.addActionListener(e -> {
                mdlTable.removeRow(selectedRow);
                capNhatSTT();
                capNhatTongTien();
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }
    }
}
