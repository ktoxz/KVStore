package com.gui;

import com.dao.DAO_SanPham;
import com.dao.DAO_KhachHang;
import com.dao.DAO_HoaDon;
import com.dao.DAO_NhanVien;
import com.entity.SanPham;
import com.entity.KhachHang;
import com.entity.NhanVien;
import com.entity.HoaDon;
import com.entity.CT_HoaDon;
import com.service.PDFExportService;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
    private DAO_NhanVien daoStaff = new DAO_NhanVien();
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

    // Hiển thị số tiền khách cần trả sau khi trừ điểm
    private JLabel lblCanTraValue;
    // Nút bỏ dùng điểm
    private JButton btnBoDiem;
    // Hiển thị dự kiến điểm còn lại sau khi dùng
    private JLabel lblDiemConLai;

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

        // Dòng hiển thị điểm còn lại dự kiến
        lblDiemConLai = new JLabel("");
        lblDiemConLai.setForeground(new Color(0, 123, 255));
        lblDiemConLai.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1; pnlKH.add(lblDiemConLai, gbc);

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

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1;
        pnlKH.add(pnlNewCustomer, gbc);


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

        // Khách cần trả (sau khi trừ điểm)
        JLabel lblCanTraRow = new JLabel("Khách cần trả:");
        lblCanTraRow.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCanTraRow.setForeground(new Color(33, 37, 41));
        lblCanTraValue = new JLabel(df.format(0));
        lblCanTraValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCanTraValue.setForeground(new Color(33, 37, 41));
        gbc.gridx = 0; gbc.gridy = 4; pnlTotal.add(lblCanTraRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblCanTraValue, gbc);

        // Nút sử dụng điểm + bỏ dùng điểm
        btnSuDungDiem = new JButton("Sử dụng điểm (F1)");
        styleButton(btnSuDungDiem, new Color(0, 123, 255));
        btnSuDungDiem.setPreferredSize(new Dimension(180, 32));
        btnBoDiem = new JButton("Bỏ dùng điểm");
        styleButton(btnBoDiem, new Color(108, 117, 125));
        btnBoDiem.setPreferredSize(new Dimension(140, 32));
        btnBoDiem.setVisible(false);
        JPanel pnlPointBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlPointBtns.setBackground(pnlTotal.getBackground());
        pnlPointBtns.add(btnSuDungDiem);
        pnlPointBtns.add(btnBoDiem);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        pnlTotal.add(pnlPointBtns, gbc);

        // Hình thức thanh toán (đẩy xuống)
        JLabel lblHinhThuc = new JLabel("Hình thức:");
        lblHinhThuc.setFont(fLabel);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
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

        // Tiền khách trả & tiền thối (đẩy xuống)
        // Đổi y: 7 và 8
        lblKhachTraRow = new JLabel("Tiền khách trả:");
        lblKhachTraRow.setFont(fLabel);
        lblTienThoiRow = new JLabel("Tiền thối:");
        lblTienThoiRow.setFont(fLabel);

        txtKhachTraField = new JTextField("0");
        txtKhachTraField.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhachTraField.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblTienThoiValue = new JLabel(df.format(0));
        lblTienThoiValue.setFont(fLabel);

        gbc.gridx = 0; gbc.gridy = 7; pnlTotal.add(lblKhachTraRow, gbc);
        gbc.gridx = 1; pnlTotal.add(txtKhachTraField, gbc);
        gbc.gridx = 0; gbc.gridy = 8; pnlTotal.add(lblTienThoiRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblTienThoiValue, gbc);

        // --- Mệnh giá --- (đẩy xuống y=9)
        pnlMenhGia = new JPanel(new GridLayout(2, 4, 6, 6));
        pnlMenhGia.setBackground(pnlTotal.getBackground());
        // (Khởi tạo động sau khi có tổng tiền thông qua capNhatTongTien)
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; pnlTotal.add(pnlMenhGia, gbc);

        // Nút hành động (đẩy xuống y=10)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setBackground(pnlTotal.getBackground());
        JButton btnPay = new JButton("Thanh toán");
        JButton btnClear = new JButton("Xóa tất cả");
        styleButton(btnPay, new Color(27, 160, 79));
        styleButton(btnClear, new Color(220, 53, 69));
        pnlButtons.add(btnPay);
        pnlButtons.add(btnClear);
        gbc.gridy = 10; gbc.gridx = 0; gbc.gridwidth = 2;
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

        // Nút bỏ dùng điểm
        btnBoDiem.addActionListener(e -> {
            resetDiemSuDung();
            capNhatTongTien();
            JOptionPane.showMessageDialog(this, "Đã bỏ sử dụng điểm.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

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

        // Enter để thanh toán từ ô tiền khách trả
        txtKhachTraField.addActionListener(e -> btnPay.doClick());

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
                	NhanVien nv = daoStaff.getFirstNV();
                    String maNV = nv.getMaNV();
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
                            // Thêm điểm tích lũy từ tổng tiền (1k = 1 điểm)
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
                            int result = JOptionPane.showOptionDialog(
                                    this,
                                    "Thanh toán thành công!\n" +
                                            "Mã hóa đơn: " + maHD + "\n" +
                                            "Hóa đơn đã được xuất ra: " + pdfPath,
                                    "Thành công",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"Mở hóa đơn", "Đóng"},
                                    "Mở hóa đơn"
                            );

                            if (result == JOptionPane.YES_OPTION) {
                                try {
                                    File pdfFile = new File(pdfPath);
                                    if (pdfFile.exists()) {
                                        // Mở file PDF bằng app mặc định của hệ thống (Windows, macOS, Linux)
                                        Desktop.getDesktop().open(pdfFile);
                                    } else {
                                        JOptionPane.showMessageDialog(this,
                                                "Không tìm thấy file PDF!",
                                                "Lỗi",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(this,
                                            "Không thể mở file PDF.\n" + ex.getMessage(),
                                            "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }

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

        // Tiền khách cần trả sau khi trừ điểm
        double tienCanTra = tongCong - tienGiamTuDiem;
        if (tienCanTra < 0) tienCanTra = 0;
        if (lblCanTraValue != null) lblCanTraValue.setText(df.format(tienCanTra));

        // Tự động làm tròn lên nghìn và cập nhật tiền khách trả
        double tienLamTron = Math.ceil(tienCanTra / 1000.0) * 1000;
        tienKhachTra = tienLamTron;
        txtKhachTraField.setText(df.format(tienKhachTra));

        // Cập nhật các nút mệnh giá
        capNhatNutMenhGia(tienLamTron);

        capNhatTienThoi();
        updatePointsButtonsEnabled();
    }

    private void capNhatNutMenhGia(double tienLamTron) {
        pnlMenhGia.removeAll();
        // Hiển thị các mức gợi ý: +1k, +2k, +5k, +10k, +100k, +200k, +500k
        long[] increments = {1_000L, 2_000L, 5_000L, 10_000L, 100_000L, 200_000L, 500_000L};
        for (long inc : increments) {
            long giaTriNut = (long) tienLamTron + inc;
            JButton btn = new JButton(df.format(giaTriNut) + "đ");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setToolTipText("Chọn số tiền khách trả: " + df.format(giaTriNut) + "đ (" + "+" + df.format(inc) + ")");
            btn.addActionListener(e -> {
                tienKhachTra = giaTriNut;
                txtKhachTraField.setText(df.format(tienKhachTra));
                capNhatTienThoi();
            });
            pnlMenhGia.add(btn);
        }
        pnlMenhGia.revalidate();
        pnlMenhGia.repaint();
    }

    private void resetDiemSuDung() {
        tienGiamTuDiem = 0;
        diemDaSuDung = 0;
        lblDiemGiamValue.setText("- " + df.format(0));
        lblDiemConLai.setText("");
        btnBoDiem.setVisible(false);
        capNhatTienThoi();
        updatePointsButtonsEnabled();
    }

    private void updatePointsButtonsEnabled() {
        boolean canUse = currentCustomer != null && currentCustomer.getDiemTichLuy() > 0;
        btnSuDungDiem.setEnabled(canUse);
        if (currentCustomer != null) {
            btnSuDungDiem.setToolTipText("Điểm khả dụng: " + currentCustomer.getDiemTichLuy() + " điểm");
        } else {
            btnSuDungDiem.setToolTipText("Vui lòng nhập khách hàng để sử dụng điểm");
        }
        btnBoDiem.setVisible(tienGiamTuDiem > 0);
        if (tienGiamTuDiem > 0 && currentCustomer != null) {
            int conLai = Math.max(0, currentCustomer.getDiemTichLuy() - diemDaSuDung);
            lblDiemConLai.setText("Sẽ còn: " + conLai + " điểm");
        } else {
            if (lblDiemConLai != null && (tienGiamTuDiem == 0)) lblDiemConLai.setText("");
        }
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
        double tongCong = getTongCong();
        double gioiHan10Phan = tongCong * 0.1; // Giới hạn 10%
        double tienToiDaTuDiem = Math.min(diemHienTai, gioiHan10Phan);
        if (tienToiDaTuDiem < 1) {
            JOptionPane.showMessageDialog(this, "Số tiền có thể giảm quá nhỏ, không thể sử dụng điểm!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Số lẻ về bội 1.000đ
        double soLe1k = tongCong % 1000;
        double tienSuDungSoLe = Math.min(soLe1k, tienToiDaTuDiem);
        // Số lẻ để làm tròn xuống bội 10.000đ (ví dụ 123.000 -> giảm 3.000 về 120.000)
        double soLe10k = tongCong % 10_000;
        double tienSuDung10k = Math.min(soLe10k, tienToiDaTuDiem);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sử dụng điểm tích lũy", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);

        // ===== INFO HEADER =====
        JPanel pnlInfo = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 15, 12, 15),
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230,230,230))));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.add(makeInfoLabel("Điểm hiện tại: ", df.format(diemHienTai) + " điểm"));
        pnlInfo.add(makeInfoLabel("Tổng cộng: ", df.format(tongCong) + "đ"));
        pnlInfo.add(makeInfoLabel("Giới hạn 10%: ", df.format(gioiHan10Phan) + "đ"));
        pnlInfo.add(makeInfoLabel("Tối đa có thể giảm: ", df.format(tienToiDaTuDiem) + "đ"));
        JLabel lblNote = new JLabel("<html><i>1 điểm = 1đ giảm giá (tối đa 10% hóa đơn)</i></html>");
        lblNote.setForeground(new Color(108,117,125));
        pnlInfo.add(lblNote);
        dialog.add(pnlInfo, BorderLayout.NORTH);

        // ===== CENTER OPTIONS =====
        JPanel pnlCenter = new JPanel(new BorderLayout(10,10));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        pnlCenter.setBackground(Color.WHITE);

        JPanel pnlQuick = new JPanel(new GridLayout(1, 0, 10, 10));
        pnlQuick.setBackground(Color.WHITE);
        pnlQuick.setBorder(BorderFactory.createTitledBorder("Chọn nhanh"));

        double defaultSelect;
        if (tienSuDung10k > 0) defaultSelect = tienSuDung10k; else if (tienSuDungSoLe > 0) defaultSelect = tienSuDungSoLe; else defaultSelect = tienToiDaTuDiem;
        final double[] tienSuDungSelected = { defaultSelect };

        JButton btnSoLe = new JButton("Số lẻ\n" + df.format(tienSuDungSoLe) + "đ");
        JButton btnVe10k = new JButton("Về chẵn 10k\n" + df.format(tienSuDung10k) + "đ");
        JButton btnMax = new JButton("Tối đa\n" + df.format(tienToiDaTuDiem) + "đ");
        styleSelectButton(btnSoLe, false);
        styleSelectButton(btnVe10k, false);
        styleSelectButton(btnMax, false);
        if (tienSuDungSoLe <= 0) btnSoLe.setEnabled(false);
        if (tienSuDung10k <= 0) btnVe10k.setEnabled(false);

        // Tùy chỉnh nhập tay (không dùng slider)
        JTextField txtCustom = new JTextField(df.format((long) defaultSelect));

        ActionListener quickListener = e -> {
            JButton src = (JButton) e.getSource();
            tienSuDungSelected[0] = src == btnSoLe ? tienSuDungSoLe : (src == btnVe10k ? tienSuDung10k : tienToiDaTuDiem);
            // Highlight selection
            styleSelectButton(btnSoLe, src == btnSoLe);
            styleSelectButton(btnVe10k, src == btnVe10k);
            styleSelectButton(btnMax, src == btnMax);
            txtCustom.setText(df.format((long) tienSuDungSelected[0]));
        };
        btnSoLe.addActionListener(quickListener);
        btnVe10k.addActionListener(quickListener);
        btnMax.addActionListener(quickListener);
        pnlQuick.add(btnSoLe); pnlQuick.add(btnVe10k); pnlQuick.add(btnMax);

        // ===== CUSTOM INPUT =====
        JPanel pnlCustom = new JPanel(new GridBagLayout());
        pnlCustom.setBackground(Color.WHITE);
        pnlCustom.setBorder(BorderFactory.createTitledBorder("Tùy chỉnh"));
        GridBagConstraints gbcC = new GridBagConstraints();
        gbcC.insets = new Insets(5,5,5,5);
        gbcC.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCustom = new JLabel("Nhập số tiền (<= " + df.format(tienToiDaTuDiem) + "đ):");
        lblCustom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbcC.gridx = 0; gbcC.gridy = 0; gbcC.gridwidth = 2; pnlCustom.add(lblCustom, gbcC);

        txtCustom.setHorizontalAlignment(SwingConstants.RIGHT);
        txtCustom.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbcC.gridx = 0; gbcC.gridy = 1; gbcC.gridwidth = 2; pnlCustom.add(txtCustom, gbcC);

        JLabel lblPreview = new JLabel("Sẽ giảm: " + df.format((long) defaultSelect) + "đ");
        lblPreview.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPreview.setForeground(new Color(27,160,79));
        gbcC.gridx = 0; gbcC.gridy = 2; gbcC.gridwidth = 2; pnlCustom.add(lblPreview, gbcC);

        // Đồng bộ text field -> preview
        txtCustom.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String raw = txtCustom.getText().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) return;
                try {
                    long val = Long.parseLong(raw);
                    if (val > tienToiDaTuDiem) {
                        val = (long) tienToiDaTuDiem;
                        txtCustom.setText(df.format(val));
                    }
                    tienSuDungSelected[0] = val;
                    lblPreview.setText("Sẽ giảm: " + df.format(val) + "đ");
                    styleSelectButton(btnSoLe, val == tienSuDungSoLe);
                    styleSelectButton(btnVe10k, val == tienSuDung10k);
                    styleSelectButton(btnMax, val == tienToiDaTuDiem);
                } catch (NumberFormatException ignored) {}
            }
        });

        pnlCenter.add(pnlQuick, BorderLayout.NORTH);
        pnlCenter.add(pnlCustom, BorderLayout.CENTER);
        dialog.add(pnlCenter, BorderLayout.CENTER);

        // ===== ACTION BUTTONS =====
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlButtons.setBackground(Color.WHITE);
        JButton btnOK = new JButton("Xác nhận");
        JButton btnCancel = new JButton("Hủy");
        styleButton(btnOK, new Color(27,160,79));
        styleButton(btnCancel, new Color(220,53,69));
        btnOK.setPreferredSize(new Dimension(120,36));
        btnCancel.setPreferredSize(new Dimension(120,36));
        pnlButtons.add(btnOK); pnlButtons.add(btnCancel);
        dialog.add(pnlButtons, BorderLayout.SOUTH);

        // Pre-select highlight
        if (defaultSelect == tienSuDungSoLe) styleSelectButton(btnSoLe, true);
        else if (defaultSelect == tienSuDung10k) styleSelectButton(btnVe10k, true);
        else styleSelectButton(btnMax, true);

        btnOK.addActionListener(e -> {
            double tienSuDung = tienSuDungSelected[0];
            if (tienSuDung <= 0) {
                JOptionPane.showMessageDialog(dialog, "Số tiền phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (tienSuDung > tienToiDaTuDiem) {
                JOptionPane.showMessageDialog(dialog, "Vượt quá giới hạn! Tối đa: " + df.format(tienToiDaTuDiem) + "đ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            diemDaSuDung = (int) tienSuDung; // 1 điểm = 1đ
            tienGiamTuDiem = tienSuDung;
            lblDiemGiamValue.setText("- " + df.format(tienGiamTuDiem));
            capNhatTongTien();
            dialog.dispose();
            JOptionPane.showMessageDialog(TAB_BanHang.this,
                    "Đã giảm " + df.format(tienGiamTuDiem) + "đ (" + diemDaSuDung + " điểm)",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private JLabel makeInfoLabel(String prefix, String value) {
        JLabel lbl = new JLabel(prefix + value);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private void styleSelectButton(JButton btn, boolean selected) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        if (selected) {
            btn.setBackground(new Color(0,123,255));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(0,92,190), 2));
        } else {
            btn.setBackground(new Color(245,245,245));
            btn.setForeground(Color.DARK_GRAY);
            btn.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        }
    }

    // ===== Helpers restored after refactor =====
    private double getTongCong() {
        double vat = tongTien * 0.08;
        return tongTien + vat;
    }

    private void capNhatSTT() {
        for (int i = 0; i < mdlTable.getRowCount(); i++) {
            mdlTable.setValueAt(i + 1, i, 0);
        }
    }

    private void setMenhGiaVisible(boolean visible) {
        pnlMenhGia.setVisible(visible);
        txtKhachTraField.setVisible(visible);
        lblTienThoiValue.setVisible(visible);
        lblKhachTraRow.setVisible(visible);
        lblTienThoiRow.setVisible(visible);
    }

    private void capNhatTienThoi() {
        double tongCong = getTongCong();
        double tienCanTra = tongCong - tienGiamTuDiem;
        double tienThoi = tienKhachTra - tienCanTra;
        if (tienThoi < 0) tienThoi = 0;
        lblTienThoiValue.setText(df.format(tienThoi));
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
