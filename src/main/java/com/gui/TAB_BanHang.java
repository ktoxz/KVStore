package com.gui;

import com.dao.DAO_SanPham;
import com.dao.DAO_KhachHang;
import com.dao.DAO_HoaDon;
import com.dao.DAO_Staff;
import com.entity.SanPham;
import com.entity.KhachHang;
import com.entity.HoaDon;
import com.entity.CT_HoaDon;
import com.service.PDFExportService;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TAB_BanHang extends JPanel {

    private JLabel lblName;
    private JLabel lblDiem;
    private JLabel lblPhone;
    private double tongTien = 0;
    private double tienKhachTra = 0;
    private double tienGiamTuDiem = 0; // Số tiền giảm từ điểm tích lũy
    private int diemDaSuDung = 0; // Số điểm đã sử dụng

    private JTextField txtKhachTraField;
    private JLabel lblTienThoiValue;
    private JLabel lblTongTien;
    private JLabel lblVAT;
    private JLabel lblTongCongValue;
    private JLabel lblDiemGiamValue;
    private JPanel pnlMenhGia;
    private JLabel lblKhachTraRow, lblTienThoiRow;
    private JTable table;
    private DefaultTableModel mdlTable;
    private final DecimalFormat df = new DecimalFormat("#,###");

    private DAO_SanPham daoSP = new DAO_SanPham();
    private DAO_KhachHang daoKH = new DAO_KhachHang();
    private DAO_HoaDon daoHD = new DAO_HoaDon();
    private DAO_Staff daoStaff = new DAO_Staff();
    private JPopupMenu popupSearch; // Di chuyển ra ngoài để tái sử dụng
    
    // Customer info fields
    private JTextField txtPhone;
    private JTextField txtName;
    private JTextArea txtNote;
    private JTextField txtDiem;
    private JPanel pnlNewCustomer;
    private JRadioButton rbNam, rbNu;
    private JButton btnAddCustomer;
    private JButton btnSuDungDiem;
    private KhachHang currentCustomer = null;

    // Thông tin nhân viên đăng nhập (mặc định, cần truyền từ GUI_Login)
    private String maNhanVien = "NV00000001"; // Có thể set từ bên ngoài
    private String tenNhanVien = "Nhân viên"; // Có thể set từ bên ngoài

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
                            popupSearch.setVisible(false);
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
                                    JMenuItem menuItem = new JMenuItem();
                                    menuItem.setPreferredSize(new Dimension(txtSearch.getWidth(), 60));
                                    menuItem.setMaximumSize(new Dimension(txtSearch.getWidth(), 60));
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
                                        txtSearch.setText("");
                                    });
                                    popupSearch.add(menuItem);
                                }
                            }
                            popupSearch.pack();
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

        lblPhone = new JLabel("SĐT KH:");
        lblName = new JLabel("Tên KH:");
        lblDiem = new JLabel("Điểm tích lũy:");
        JLabel lblNoteLabel = new JLabel("Ghi chú:");

        txtPhone = new JTextField();
        txtName = new JTextField();
        txtName.setEnabled(false);
        txtDiem = new JTextField();
        txtDiem.setEnabled(false);
        txtNote = new JTextArea(3, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollNote = new JScrollPane(txtNote);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; pnlKH.add(lblPhone, gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlKH.add(txtPhone, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; pnlKH.add(lblName, gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlKH.add(txtName, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; pnlKH.add(lblDiem, gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlKH.add(txtDiem, gbc);

        // Panel thêm khách hàng mới (ẩn mặc định)
        pnlNewCustomer = new JPanel(new GridBagLayout());
        pnlNewCustomer.setBackground(new Color(255, 248, 220));
        pnlNewCustomer.setBorder(BorderFactory.createTitledBorder("Thêm khách hàng mới"));
        pnlNewCustomer.setVisible(false);
        
        GridBagConstraints gbcNew = new GridBagConstraints();
        gbcNew.insets = new Insets(5, 6, 5, 6);
        gbcNew.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNewName = new JLabel("Tên KH:");
        JTextField txtNewName = new JTextField();
        JLabel lblGender = new JLabel("Giới tính:");
        rbNam = new JRadioButton("Nam", true);
        rbNu = new JRadioButton("Nữ");
        rbNam.setBackground(pnlNewCustomer.getBackground());
        rbNu.setBackground(pnlNewCustomer.getBackground());
        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbNam);
        bgGender.add(rbNu);
        
        JPanel pnlGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlGender.setBackground(pnlNewCustomer.getBackground());
        pnlGender.add(rbNam);
        pnlGender.add(rbNu);

        btnAddCustomer = new JButton("Thêm khách hàng");
        styleButton(btnAddCustomer, new Color(27, 160, 79));
        btnAddCustomer.setPreferredSize(new Dimension(150, 32));

        gbcNew.gridx = 0; gbcNew.gridy = 0; gbcNew.weightx = 0; pnlNewCustomer.add(lblNewName, gbcNew);
        gbcNew.gridx = 1; gbcNew.weightx = 1; pnlNewCustomer.add(txtNewName, gbcNew);
        gbcNew.gridx = 0; gbcNew.gridy = 1; gbcNew.weightx = 0; pnlNewCustomer.add(lblGender, gbcNew);
        gbcNew.gridx = 1; gbcNew.weightx = 1; pnlNewCustomer.add(pnlGender, gbcNew);
        gbcNew.gridx = 0; gbcNew.gridy = 2; gbcNew.gridwidth = 2; gbcNew.anchor = GridBagConstraints.CENTER;
        pnlNewCustomer.add(btnAddCustomer, gbcNew);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1;
        pnlKH.add(pnlNewCustomer, gbc);

        // Ghi chú ở cuối cùng
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0; pnlKH.add(lblNoteLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH; pnlKH.add(scrollNote, gbc);

        // Sự kiện tìm kiếm khách hàng khi nhập SĐT
        txtPhone.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String sdt = txtPhone.getText().trim();
                if (sdt.isEmpty()) {
                    // Không nhập SĐT -> Khách vãng lai
                    txtName.setText("Khách vãng lai");
                    txtDiem.setText("0");
                    pnlNewCustomer.setVisible(false);
                    currentCustomer = null;
                    resetDiemSuDung();
                } else if (sdt.length() >= 10) {
                    // Tìm kiếm khách hàng
                    KhachHang kh = daoKH.timKiemKH(sdt);
                    if (kh != null) {
                        // Tìm thấy khách hàng
                        currentCustomer = kh;
                        txtName.setText(kh.getTenKH());
                        txtDiem.setText(String.valueOf(kh.getDiemTichLuy()));
                        pnlNewCustomer.setVisible(false);
                        resetDiemSuDung();
                    } else {
                        // Không tìm thấy -> hiển thị form thêm mới
                        txtName.setText("");
                        txtDiem.setText("0");
                        pnlNewCustomer.setVisible(true);
                        currentCustomer = null;
                        resetDiemSuDung();
                    }
                } else {
                    txtName.setText("");
                    txtDiem.setText("");
                    pnlNewCustomer.setVisible(false);
                    currentCustomer = null;
                    resetDiemSuDung();
                }
                pnlKH.revalidate();
                pnlKH.repaint();
            }
        });

        // Sự kiện thêm khách hàng mới
        btnAddCustomer.addActionListener(e -> {
            String sdt = txtPhone.getText().trim();
            String tenKH = txtNewName.getText().trim();
            
            if (sdt.isEmpty() || sdt.length() < 10) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (tenKH.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean gioiTinh = rbNam.isSelected();
            KhachHang newKH = new KhachHang("", tenKH, gioiTinh, sdt, LocalDate.now(), 0);
            
            if (daoKH.themKH(newKH)) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Tải lại thông tin khách hàng
                KhachHang kh = daoKH.timKiemKH(sdt);
                if (kh != null) {
                    currentCustomer = kh;
                    txtName.setText(kh.getTenKH());
                    txtDiem.setText(String.valueOf(kh.getDiemTichLuy()));
                    pnlNewCustomer.setVisible(false);
                    txtNewName.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

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

        // Giảm từ điểm (di chuyển lên đầu tiên)
        JLabel lblDiemGiamRow = new JLabel("Giảm từ điểm:");
        lblDiemGiamRow.setFont(fLabel);
        lblDiemGiamRow.setForeground(new Color(27, 160, 79));
        lblDiemGiamValue = new JLabel("- " + df.format(0));
        lblDiemGiamValue.setFont(fLabel);
        lblDiemGiamValue.setForeground(new Color(27, 160, 79));
        gbc.gridx = 0; gbc.gridy = 0; pnlTotal.add(lblDiemGiamRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblDiemGiamValue, gbc);

        // Tổng tiền (chưa VAT)
        JLabel lblThanhTienRow = new JLabel("Tổng tiền:");
        lblThanhTienRow.setFont(fLabel);
        lblTongTien = new JLabel(df.format(0));
        lblTongTien.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 1; pnlTotal.add(lblThanhTienRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblTongTien, gbc);

        // VAT 8%
        JLabel lblVATRow = new JLabel("VAT (8%):");
        lblVATRow.setFont(fLabel);
        lblVAT = new JLabel(df.format(0));
        lblVAT.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 2; pnlTotal.add(lblVATRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblVAT, gbc);

        // Tổng cộng (đã VAT)
        JLabel lblTongCongRow = new JLabel("Tổng cộng:");
        lblTongCongRow.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongCongRow.setForeground(new Color(220, 53, 69));
        lblTongCongValue = new JLabel(df.format(0));
        lblTongCongValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongCongValue.setForeground(new Color(220, 53, 69));
        gbc.gridx = 0; gbc.gridy = 3; pnlTotal.add(lblTongCongRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblTongCongValue, gbc);


        // Nút sử dụng điểm
        btnSuDungDiem = new JButton("Sử dụng điểm (F1)");
        styleButton(btnSuDungDiem, new Color(0, 123, 255));
        btnSuDungDiem.setPreferredSize(new Dimension(180, 32));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        pnlTotal.add(btnSuDungDiem, gbc);

        // Hình thức thanh toán
        JLabel lblHinhThuc = new JLabel("Hình thức:");
        lblHinhThuc.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        pnlTotal.add(lblHinhThuc, gbc);
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

        gbc.gridx = 0; gbc.gridy = 6; pnlTotal.add(lblKhachTraRow, gbc);
        gbc.gridx = 1; pnlTotal.add(txtKhachTraField, gbc);
        gbc.gridx = 0; gbc.gridy = 7; pnlTotal.add(lblTienThoiRow, gbc);
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
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; pnlTotal.add(pnlMenhGia, gbc);

        // Nút hành động
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setBackground(pnlTotal.getBackground());
        JButton btnPay = new JButton("Thanh toán");
        JButton btnClear = new JButton("Xóa tất cả");
        styleButton(btnPay, new Color(27, 160, 79));
        styleButton(btnClear, new Color(220, 53, 69));
        pnlButtons.add(btnPay);
        pnlButtons.add(btnClear);
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
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
            txtPhone.setText("");
            txtName.setText("");
            txtDiem.setText("");
            txtNote.setText("");
            pnlNewCustomer.setVisible(false);
            currentCustomer = null;
            resetDiemSuDung();
            capNhatTienThoi();
        });

        // Sự kiện sử dụng điểm
        btnSuDungDiem.addActionListener(e -> xuLySuDungDiem());

        // Key binding cho F1
        InputMap inputMap = pnlTotal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = pnlTotal.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "suDungDiem");
        actionMap.put("suDungDiem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xuLySuDungDiem();
            }
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

        // Sự kiện thanh toán
        btnPay.addActionListener(e -> {
            if (mdlTable.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng thêm sản phẩm vào giỏ hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double tongCong = getTongCong();
            double tienCanTra = tongCong - tienGiamTuDiem;

            if (rbCash.isSelected() && tienKhachTra < tienCanTra) {
                JOptionPane.showMessageDialog(this, "Tiền khách trả chưa đủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Xử lý thanh toán thành công
            int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán?\nTổng cộng: " + df.format(tienCanTra) + "đ",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Lấy mã nhân viên từ database
                    String maNV = daoStaff.getFirstMaNV();
                    if (maNV == null) {
                        JOptionPane.showMessageDialog(this,
                            "Không tìm thấy nhân viên trong hệ thống!\nVui lòng thêm nhân viên trước khi thanh toán.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Tạo hóa đơn
                    String maHD = daoHD.phatSinhMaHoaDon();
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(maHD);
                    hoaDon.setNgayGiaoDich(LocalDate.now());
                    hoaDon.setThongTinChung(txtNote.getText().trim());
                    hoaDon.setTienKhach(tienKhachTra);
                    hoaDon.setThue(tongTien * 0.08); // VAT 8%

                    // Nếu có khách hàng thì dùng mã khách hàng, không thì để mã khách vãng lai
                    if (currentCustomer != null) {
                        hoaDon.setMaKH(currentCustomer.getMaKH());
                    } else {
                        // Tìm hoặc tạo khách vãng lai
                        KhachHang khachVangLai = daoKH.timKiemKH("0000000000");
                        if (khachVangLai == null) {
                            // Tạo khách vãng lai nếu chưa có
                            khachVangLai = new KhachHang("KH00000000", "Khách vãng lai", true, "0000000000", LocalDate.now(), 0);
                            daoKH.themKH(khachVangLai);
                        }
                        hoaDon.setMaKH("KH00000000");
                    }

                    hoaDon.setMaNV(maNV); // Sử dụng mã nhân viên từ database
                    hoaDon.setMaKM(null); // Chưa có khuyến mãi

                    // Thêm chi tiết hóa đơn
                    for (int i = 0; i < mdlTable.getRowCount(); i++) {
                        String maSP = mdlTable.getValueAt(i, 1).toString();
                        String tenSP = mdlTable.getValueAt(i, 2).toString();
                        int soLuong = Integer.parseInt(mdlTable.getValueAt(i, 3).toString());
                        String priceStr = mdlTable.getValueAt(i, 4).toString().replace(",", "").replace(".", "").trim();
                        double giaSP = Double.parseDouble(priceStr);

                        CT_HoaDon chiTiet = new CT_HoaDon(maHD, maSP, soLuong, tenSP, giaSP);
                        hoaDon.addChiTiet(chiTiet);
                    }

                    // Lưu hóa đơn vào database
                    boolean luuThanhCong = daoHD.themHoaDon(hoaDon);

                    if (luuThanhCong) {
                        // Cập nhật điểm tích lũy cho khách hàng
                        if (currentCustomer != null) {
                            // Trừ điểm đã sử dụng
                            if (diemDaSuDung > 0) {
                                daoKH.truDiemTichLuy(currentCustomer.getMaKH(), diemDaSuDung);
                            }
                            // Thêm điểm tích lũy từ tổng tiền (sau khi trừ 1000)
                            int diemMoi = (int)(tongTien / 1000);
                            if (diemMoi > 0) {
                                daoKH.themDiemTichLuy(currentCustomer.getMaKH(), tongTien);
                            }
                        }

                        // Lấy số lần xuất hóa đơn
                        int soLanXuatHD = daoHD.getSoLanXuatHoaDon(hoaDon.getMaKH());

                        // Xuất PDF
                        String pdfPath = PDFExportService.xuatHoaDonPDF(
                            hoaDon,
                            currentCustomer,
                            tenNhanVien,
                            soLanXuatHD,
                            tienGiamTuDiem,
                            diemDaSuDung
                        );

                        if (pdfPath != null) {
                            JOptionPane.showMessageDialog(this,
                                "Thanh toán thành công!\n" +
                                "Mã hóa đơn: " + maHD + "\n" +
                                "Hóa đơn đã được xuất ra: " + pdfPath,
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "Thanh toán thành công!\n" +
                                "Mã hóa đơn: " + maHD + "\n" +
                                "Nhưng có lỗi khi xuất PDF!",
                                "Cảnh báo",
                                JOptionPane.WARNING_MESSAGE);
                        }

                        // Reset form về trạng thái ban đầu
                        tienKhachTra = 0;
                        txtKhachTraField.setText("0");
                        mdlTable.setRowCount(0);
                        tongTien = 0;
                        lblTongTien.setText("0");
                        lblVAT.setText("0");
                        lblTongCongValue.setText("0");
                        txtPhone.setText("");
                        txtName.setText("Khách vãng lai");
                        txtDiem.setText("0");
                        txtNote.setText("");
                        pnlNewCustomer.setVisible(false);
                        currentCustomer = null;
                        resetDiemSuDung();
                        capNhatTienThoi();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Có lỗi khi lưu hóa đơn vào cơ sở dữ liệu!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                        "Có lỗi xảy ra trong quá trình thanh toán:\n" + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
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
        
        // Khởi tạo mặc định
        txtName.setText("Khách vãng lai");
        txtDiem.setText("0");
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

        int lastRow = mdlTable.getRowCount() - 1;
        table.requestFocus();
        table.changeSelection(lastRow, 3, false, false); // chọn ô số lượng
        table.editCellAt(lastRow, 3); // mở chế độ chỉnh sửa
        Component editor = table.getEditorComponent();
        if (editor != null) {
            editor.requestFocusInWindow(); // đặt focus vào editor
        }
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

        // Cập nhật hiển thị
        lblTongTien.setText(df.format(tongTien));

        // Tính VAT 8%
        double vat = tongTien * 0.08;
        lblVAT.setText(df.format(vat));

        // Tính tổng cộng
        double tongCong = tongTien + vat;
        lblTongCongValue.setText(df.format(tongCong));

        capNhatTienThoi();
    }

    private void capNhatTienThoi() {
        double tongCong = getTongCong();
        double tienCanTra = tongCong - tienGiamTuDiem;
        double thoi = tienKhachTra - tienCanTra;
        if (thoi < 0) thoi = 0;
        lblTienThoiValue.setText(df.format(thoi));
    }

    private void xuLySuDungDiem() {
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập thông tin khách hàng để sử dụng điểm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int diemHienTai = currentCustomer.getDiemTichLuy();
        if (diemHienTai <= 0) {
            JOptionPane.showMessageDialog(this, "Khách hàng không có điểm tích lũy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tính số tiền tối đa có thể sử dụng từ điểm (10 điểm = 1,000đ)
        double tienToiDaTuDiem = (diemHienTai / 10.0) * 1000;
        double tongCong = getTongCong();

        String input = JOptionPane.showInputDialog(this,
            "Điểm hiện tại: " + diemHienTai + " điểm\n" +
            "Giá trị tối đa: " + df.format(tienToiDaTuDiem) + "đ\n" +
            "Tổng cộng cần thanh toán: " + df.format(tongCong) + "đ\n\n" +
            "Nhập số tiền muốn sử dụng (VNĐ):",
            "Sử dụng điểm tích lũy",
            JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            double tienSuDung = Double.parseDouble(input.trim().replaceAll("[^0-9]", ""));

            if (tienSuDung <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tienSuDung > tienToiDaTuDiem) {
                JOptionPane.showMessageDialog(this, "Số tiền vượt quá giá trị điểm hiện có!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tienSuDung > tongCong) {
                JOptionPane.showMessageDialog(this, "Số tiền vượt quá tổng cộng cần thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tính số điểm cần trừ (làm tròn lên)
            diemDaSuDung = (int) Math.ceil((tienSuDung / 1000.0) * 10);
            tienGiamTuDiem = tienSuDung;

            // Cập nhật hiển thị
            lblDiemGiamValue.setText("- " + df.format(tienGiamTuDiem));

            // Tự động cập nhật tiền khách trả
            double tienCanTra = tongCong - tienGiamTuDiem;
            tienKhachTra = tienCanTra;
            txtKhachTraField.setText(df.format(tienKhachTra));

            capNhatTienThoi();

            JOptionPane.showMessageDialog(this,
                "Đã áp dụng giảm giá " + df.format(tienGiamTuDiem) + "đ\n" +
                "Sử dụng " + diemDaSuDung + " điểm",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetDiemSuDung() {
        tienGiamTuDiem = 0;
        diemDaSuDung = 0;
        lblDiemGiamValue.setText("- " + df.format(0));
        capNhatTienThoi();
    }

    private double getTongCong() {
        double vat = tongTien * 0.08;
        return tongTien + vat;
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
