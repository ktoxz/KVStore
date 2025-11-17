package com.gui;

import com.dao.*;
import com.entity.*;
import com.enums.LoaiKM; // added
import com.service.PDFExportService;
import com.service.TabStyler;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AbstractDocument; // added
import javax.swing.text.AttributeSet; // added
import javax.swing.text.BadLocationException; // added
import javax.swing.text.DocumentFilter; // added
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList; // added

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
    private DAO_CT_KhuyenMai daoCTKM = new DAO_CT_KhuyenMai(); // added
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

    // Thông tin nhân viên đăng nhập (nhận từ GUI_General)
    private NhanVien nhanVien;

    // Hiển thị số tiền khách cần trả sau khi trừ điểm
    private JLabel lblCanTraValue; // vẫn giữ để tính nhưng không hiển thị dòng
    // Nút bỏ dùng điểm
    private JButton btnBoDiem;
    // Hiển thị dự kiến điểm còn lại sau khi dùng
    private JLabel lblDiemConLai;

    // Biến phục vụ điều hướng popup tìm kiếm
    private List<JMenuItem> searchItems = new ArrayList<>();
    private int searchSelectedIndex = -1;
    private String searchPlaceholder = "Tìm sản phẩm (F2)...";

    public TAB_BanHang() {
        this(null);
    }

    public TAB_BanHang(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(TabStyler.createHeader("BÁN HÀNG"), BorderLayout.NORTH);

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
        // Placeholder init
        applyPlaceholderBehavior(txtSearch);
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
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    return; // xử lý ở keyPressed
                }
                if (lastTask != null) lastTask.cancel();
                lastTask = new TimerTask() {
                    @Override public void run() {
                        SwingUtilities.invokeLater(() -> {
                            String kw = txtSearch.getText().trim();
                            popupSearch.setVisible(false);
                            popupSearch.removeAll();
                            searchItems.clear();
                            searchSelectedIndex = -1;
                            if (kw.isEmpty() || kw.equals(searchPlaceholder)) {
                                return;
                            }
                            List<SanPham> ds = daoSP.searchByNameOrMa(kw, 10);
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
                                    
                                    // Load icon
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
                                    if (icon != null) menuItem.setIcon(icon);

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
                                        applyPlaceholderBehavior(txtSearch); // reset placeholder nếu trống
                                        txtSearch.requestFocusInWindow();
                                    });
                                    popupSearch.add(menuItem);
                                    searchItems.add(menuItem);
                                }
                            }
                            popupSearch.pack();
                            popupSearch.show(txtSearch, 0, txtSearch.getHeight());
                            // chọn mặc định item đầu tiên
                            if (!searchItems.isEmpty()) {
                                searchSelectedIndex = 0;
                                updateSearchSelectionHighlight();
                            }
                        });
                    }
                };
                timer.schedule(lastTask, DELAY);
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popupSearch.isVisible() || searchItems.isEmpty()) return;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    searchSelectedIndex = (searchSelectedIndex + 1) % searchItems.size();
                    updateSearchSelectionHighlight();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    searchSelectedIndex = (searchSelectedIndex - 1 + searchItems.size()) % searchItems.size();
                    updateSearchSelectionHighlight();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (searchSelectedIndex >= 0 && searchSelectedIndex < searchItems.size()) {
                        searchItems.get(searchSelectedIndex).doClick();
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupSearch.setVisible(false);
                }
            }
        };
        txtSearch.addKeyListener(searchListener);
        // F2 key binding to focus search
        InputMap imF2 = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap amF2 = this.getActionMap();
        imF2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "focusSearch");
        amF2.put("focusSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocusInWindow();
                if (txtSearch.getText().equals(searchPlaceholder)) {
                    txtSearch.setCaretPosition(0);
                } else {
                    txtSearch.selectAll();
                }
            }
        });

        // Ẩn popup khi txtSearch mất focus
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (!popupSearch.isVisible()) return;
                    popupSearch.setVisible(false);
                });
                if (txtSearch.getText().isEmpty()) {
                    applyPlaceholderBehavior(txtSearch);
                }
            }
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(searchPlaceholder)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
        });

        // ===== BẢNG SẢN PHẨM =====
        String[] cols = {"STT", "Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Thành tiền", ""};
        mdlTable = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 3 || col == 6;
            }
        };

        // Tạo JTable với zebra striping và selection màu nhẹ
        table = new JTable(mdlTable) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                // Không can thiệp màu nền của cột nút X (để giữ nút đỏ)
                if (column == 6) return c;
                Color zebraOdd = new Color(248, 249, 252);
                Color zebraEven = Color.WHITE;
                Color selectedBg = new Color(233, 236, 239); // xám nhạt, thay thế xanh dương
                if (isRowSelected(row)) {
                    c.setBackground(selectedBg);
                } else {
                    c.setBackground((row % 2 == 0) ? zebraEven : zebraOdd);
                }
                return c;
            }
        };
        // Màu chọn (nếu renderer sử dụng selectionBackground)
        table.setSelectionBackground(new Color(233, 236, 239));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(230,230,230));

        // Tăng chiều cao dòng để hiển thị 2 dòng (giá gốc + giá sau KM) không bị cắt chữ
        int baseH = table.getFontMetrics(new Font("Segoe UI", Font.PLAIN, 14)).getHeight();
        table.setRowHeight(baseH * 2 + 8);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);

        // Điều chỉnh độ rộng các cột cho gọn gàng
        table.getColumnModel().getColumn(0).setPreferredWidth(48); // STT
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(90); // Mã SP
        table.getColumnModel().getColumn(3).setPreferredWidth(80); // Số lượng
        table.getColumnModel().getColumn(3).setMaxWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Đơn giá
        table.getColumnModel().getColumn(5).setPreferredWidth(130); // Thành tiền
        table.getColumnModel().getColumn(6).setPreferredWidth(68); // Nút X
        table.getColumnModel().getColumn(6).setMaxWidth(80);

        // Căn giữa STT và Số lượng
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Renderer cho nút X
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Renderer cho cột Đơn giá (hiển thị gạch giá cũ + giá sau KM)
        table.getColumnModel().getColumn(4).setCellRenderer(new PriceRenderer());
        // Renderer cho cột Thành tiền (hiển thị gạch nếu có KM + giá sau KM)
        table.getColumnModel().getColumn(5).setCellRenderer(new TotalRenderer());
        // Editor cho cột Số lượng: chỉ số, chọn hết khi focus để nhập nhanh
        table.getColumnModel().getColumn(3).setCellEditor(new NumericCellEditor());

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
                        String maSP = mdlTable.getValueAt(row, 1).toString();
                        SanPham sp = daoSP.findById(maSP);
                        double donGiaKM = getDonGiaSauKhuyenMai(sp);
                        double thanhTien = qty * donGiaKM;
                        // cập nhật "Thành tiền"
                        mdlTable.setValueAt(df.format(thanhTien), row, 5);
                        // cập nhật lại tổng
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
        /*
        JLabel lblCanTraRow = new JLabel("Khách cần trả:");
        lblCanTraRow.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCanTraValue = new JLabel(df.format(0));
        lblCanTraValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCanTraValue.setForeground(new Color(33, 37, 41));
        gbc.gridx = 0; gbc.gridy = 4; pnlTotal.add(lblCanTraRow, gbc);
        gbc.gridx = 1; pnlTotal.add(lblCanTraValue, gbc);
        */

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
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        pnlTotal.add(pnlPointBtns, gbc);

        // Hình thức thanh toán (đẩy xuống)
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

        // Tiền khách trả & tiền thối (đẩy xuống y: 6 và 7)
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

        // --- Mệnh giá --- (đẩy xuống y=8)
        pnlMenhGia = new JPanel(new GridLayout(2, 4, 6, 6));
        pnlMenhGia.setBackground(pnlTotal.getBackground());
        // (Khởi tạo động sau khi có tổng tiền thông qua capNhatTongTien)
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; pnlTotal.add(pnlMenhGia, gbc);

        // Nút hành động (đẩy xuống y=9)
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

        // === Custom Tab navigation chain ===
        txtKhachTraField.setFocusTraversalKeysEnabled(false);
        btnPay.setFocusTraversalKeysEnabled(false);
        btnClear.setFocusTraversalKeysEnabled(false);
        txtPhone.setFocusTraversalKeysEnabled(false);

        InputMap imKhachTra = txtKhachTraField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap amKhachTra = txtKhachTraField.getActionMap();
        imKhachTra.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "toPay");
        amKhachTra.put("toPay", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnPay.requestFocusInWindow(); } });

        InputMap imPay = btnPay.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap amPay = btnPay.getActionMap();
        imPay.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "toClear");
        amPay.put("toClear", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnClear.requestFocusInWindow(); } });

        InputMap imClear = btnClear.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap amClear = btnClear.getActionMap();
        imClear.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "toPhone");
        amClear.put("toPhone", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { txtPhone.requestFocusInWindow(); } });

        // Optional: Shift+Tab reverse chain
        imPay.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "backToKhachTra");
        amPay.put("backToKhachTra", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { txtKhachTraField.requestFocusInWindow(); } });
        imClear.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "backToPay");
        amClear.put("backToPay", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnPay.requestFocusInWindow(); } });
        InputMap imPhone = txtPhone.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap amPhone = txtPhone.getActionMap();
        imPhone.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "backToClear");
        amPhone.put("backToClear", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnClear.requestFocusInWindow(); } });
        // === End custom Tab navigation ===
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

            // Nếu chọn chuyển khoản thì hiển thị mã QR Momo trước
            if (rbBank.isSelected()) {
                showMomoDialog();
            }

            // Xử lý thanh toán thành công
            int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán?\nTổng cộng: " + df.format(tienCanTra) + "đ",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Không còn dùng DAO_NhanVien.getFirstNV(), luôn dùng nhân viên đăng nhập
                    if (nhanVien == null) {
                        JOptionPane.showMessageDialog(this,
                                "Không có thông tin nhân viên đăng nhập!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Tạo hóa đơn
                    String maHD = daoHD.phatSinhMaHoaDon();
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(maHD);
                    hoaDon.setNgayGiaoDich(LocalDate.now());
                    hoaDon.setTienKhach(tienKhachTra);
                    hoaDon.setThue(tongTien * 0.08); // VAT 8%

                    // Nếu có khách hàng thì dùng mã khách hàng, không thì để mã khách vãng lai
                    if (currentCustomer != null) {
                        hoaDon.setKhachHang(currentCustomer);
                    } else {
                        // Tìm hoặc tạo khách vãng lai
                        KhachHang khachVangLai = daoKH.timKiemKH("0000000000");
                        if (khachVangLai == null) {
                            // Tạo khách vãng lai nếu chưa có
                            khachVangLai = new KhachHang("KH00000000", "Khách vãng lai", true, "0000000000", LocalDate.now(), 0);
                            daoKH.themKH(khachVangLai);
                        }
                        hoaDon.setKhachHang("KH00000000");
                    }

                    // Gán đúng nhân viên đăng nhập cho hóa đơn
                    hoaDon.setNhanVien(nhanVien);

                    // Thêm chi tiết hóa đơn
                    for (int i = 0; i < mdlTable.getRowCount(); i++) {
                        String maSP = mdlTable.getValueAt(i, 1).toString();
                        SanPham sp = daoSP.findById(maSP);
                        String tenSP = mdlTable.getValueAt(i, 2).toString();
                        int soLuong = Integer.parseInt(mdlTable.getValueAt(i, 3).toString());
                        // Lấy giá sau KM để lưu vào hóa đơn
                        double giaSP = getDonGiaSauKhuyenMai(sp);

                        CT_HoaDon chiTiet = new CT_HoaDon(hoaDon, sp, soLuong);
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

                        // Xuất PDF
                        String pdfPath = PDFExportService.xuatHoaDonPDF(
                            hoaDon,
                            currentCustomer,
                            nhanVien.getTenNV(),
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
        TabStyler.applyContentFont(splitMain);
        
        // Khởi tạo mặc định
        txtName.setText("Khách vãng lai");
        txtDiem.setText("0");
    }

    public void setNhanVien(NhanVien nv) {
        this.nhanVien = nv;
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
                double donGiaKM = getDonGiaSauKhuyenMai(sp);
                double thanhTien = sl * donGiaKM;
                // cập nhật giá trị hiển thị và tính tiền
                mdlTable.setValueAt(df.format(thanhTien), i, 5);
                // lưu lại đơn giá sau KM ở cột 4 (để export/logic khác dùng nếu cần)
                mdlTable.setValueAt(donGiaKM, i, 4);
                capNhatTongTien();
                // focus vào số lượng
                requestFocusSoLuong(i);
                return;
            }
        }
        // Thêm sản phẩm mới
        int stt = mdlTable.getRowCount() + 1;
        int soLuong = 1;
        double donGiaKM = getDonGiaSauKhuyenMai(sp);
        double thanhTien = soLuong * donGiaKM;
        mdlTable.addRow(new Object[]{stt, sp.getMaSP(), sp.getTenSP(), soLuong,
                donGiaKM, df.format(thanhTien), "X"});

        capNhatTongTien();

        int lastRow = mdlTable.getRowCount() - 1;
        requestFocusSoLuong(lastRow);
    }

    private void requestFocusSoLuong(int rowIndex) {
        table.requestFocus();
        table.changeSelection(rowIndex, 3, false, false); // chọn ô số lượng
        table.editCellAt(rowIndex, 3); // mở chế độ chỉnh sửa
        Component editor = table.getEditorComponent();
        if (editor != null) {
            editor.requestFocusInWindow(); // đặt focus vào editor
            if (editor instanceof JTextField tf) {
                tf.selectAll(); // chọn hết để nhập nhanh số mới
            }
        }
    }

    private void capNhatTongTien() {
        tongTien = 0;
        for (int i = 0; i < mdlTable.getRowCount(); i++) {
            try {
                String maSP = mdlTable.getValueAt(i, 1).toString();
                SanPham sp = daoSP.findById(maSP);
                int qty = Integer.parseInt(mdlTable.getValueAt(i, 3).toString());
                if (qty < 1) qty = 1;
                double donGiaKM = getDonGiaSauKhuyenMai(sp);
                double line = qty * donGiaKM;
                tongTien += line;
                // cập nhật lại cột Thành tiền để đúng theo KM
                mdlTable.setValueAt(df.format(line), i, 5);
                // đồng bộ lại cột đơn giá (dưới dạng số) để các n��i khác có thể dùng
                mdlTable.setValueAt(donGiaKM, i, 4);
            } catch (Exception ex) {
                // Bỏ qua hàng lỗi
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

        // Tiền khách cần trả sau khi trừ điểm (vẫn tính nhưng không hiển thị dòng UI)
        double tienCanTra = tongTien + tongTien * 0.08 - tienGiamTuDiem; // getTongCong() - tienGiamTuDiem
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
        if (tienThoi < 250) tienThoi = 0;
        if (tienThoi > 750 && tienThoi < 1000) tienThoi = 1000;
        lblTienThoiValue.setText(df.format(tienThoi));
    }

    // Tính đơn giá sau khuyến mãi cho 1 sản phẩm (nếu có)
    private double getDonGiaSauKhuyenMai(SanPham sp) {
        if (sp == null) return 0;
        double giaGoc = sp.getGiaSP();
        try {
            CT_KhuyenMai km = daoCTKM.findBestForProduct(sp.getMaSP());
            if (km != null && km.getLoaiKM() != null) {
                if (km.getLoaiKM() == LoaiKM.GiamGiaPhanTramSP) {
                    giaGoc = giaGoc * (100.0 - km.getGiaTri()) / 100.0;
                } else if (km.getLoaiKM() == LoaiKM.GiamGiaTienSP) {
                    giaGoc = giaGoc - km.getGiaTri();
                }
                if (giaGoc < 0) giaGoc = 0;
            }
        } catch (Exception ignored) {}
        return giaGoc;
    }

    // Renderer hiển thị giá cũ (gạch) + giá sau KM cho cột Đơn giá
    class PriceRenderer extends JLabel implements TableCellRenderer {
        public PriceRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
            setFont(table.getFont()); // đồng bộ font với bảng
            String maSP = table.getValueAt(row, 1).toString();
            SanPham sp = daoSP.findById(maSP);
            double giaGoc = sp != null ? sp.getGiaSP() : 0;
            double giaSauKM = getDonGiaSauKhuyenMai(sp);
            if (giaSauKM < giaGoc - 0.001) {
                String html = "<html><div style='text-align:right; font-size:14px; line-height:14px; margin:0; padding:0;'>" +
                        "<div style='color:#6c757d; text-decoration: line-through; margin:0; padding:0;'>" + df.format(giaGoc) + "đ</div>" +
                        "<div style='color:#dc3545; font-weight:bold; margin:0; padding:0;'>" + df.format(giaSauKM) + "đ</div>" +
                        "</div></html>";
                setText(html);
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setText(df.format(giaGoc) + "đ");
                setHorizontalAlignment(SwingConstants.RIGHT);
                setForeground(new Color(33,37,41));
            }
            return this;
        }
    }

    // Renderer hiển thị tổng tiền: gạch tổng gốc + hiển thị tổng sau KM khi có khuyến mãi
    class TotalRenderer extends JLabel implements TableCellRenderer {
        public TotalRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
            setFont(table.getFont());
            String maSP = table.getValueAt(row, 1).toString();
            SanPham sp = daoSP.findById(maSP);
            int qty = 1;
            try { qty = Integer.parseInt(table.getValueAt(row, 3).toString()); } catch (Exception ignored) {}
            double giaGoc = sp != null ? sp.getGiaSP() : 0;
            double giaSauKM = getDonGiaSauKhuyenMai(sp);
            double thanhTienGoc = qty * giaGoc;
            double thanhTienSau = qty * giaSauKM;
            if (giaSauKM < giaGoc - 0.001) {
                String html = "<html><div style='text-align:right; font-size:14px; line-height:14px; margin:0; padding:0;'>" +
                        "<div style='color:#6c757d; text-decoration: line-through; margin:0; padding:0;'>" + df.format(thanhTienGoc) + "đ</div>" +
                        "<div style='color:#dc3545; font-weight:bold; margin:0; padding:0;'>" + df.format(thanhTienSau) + "đ</div>" +
                        "</div></html>";
                setText(html);
            } else {
                setText(df.format(thanhTienSau) + "đ");
            }
            setHorizontalAlignment(SwingConstants.RIGHT);
            return this;
        }
    }

    // Editor số lượng: chỉ cho số, chọn hết khi focus để nhập nhanh (2 -> thay 1 thành 2; 20 -> thành 20)
    class NumericCellEditor extends DefaultCellEditor {
        private final JTextField textField;
        public NumericCellEditor() {
            super(new JTextField());
            textField = (JTextField) getComponent();
            textField.setHorizontalAlignment(SwingConstants.CENTER);
            textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            // Chỉ cho phép số
            ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string != null && string.matches("[0-9]+")) {
                        super.insertString(fb, offset, string, attr);
                    }
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text != null && text.matches("[0-9]+")) {
                        super.replace(fb, offset, length, text, attrs);
                    } else if (text == null || text.isEmpty()) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });
            // Chọn hết khi focus để gõ là thay thế ngay
            textField.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { SwingUtilities.invokeLater(textField::selectAll); }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            SwingUtilities.invokeLater(textField::selectAll);
            return c;
        }
        @Override
        public Object getCellEditorValue() {
            String txt = textField.getText();
            if (txt == null || txt.isEmpty()) return 1;
            try {
                int v = Integer.parseInt(txt);
                return Math.max(1, v);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
    }

    // Renderer cho cột X
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("X");
            setForeground(Color.WHITE);
            setBackground(new Color(220, 53, 69));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setMargin(new Insets(2, 8, 2, 8));
            setFocusPainted(false);
            setBorderPainted(false);
            setToolTipText("Xóa sản phẩm khỏi giỏ hàng");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setMargin(new Insets(2, 8, 2, 8));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setToolTipText("Xóa sản phẩm khỏi giỏ hàng");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
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

    // Highlight lựa chọn hiện tại trong popup tìm kiếm
    private void updateSearchSelectionHighlight() {
        for (int i = 0; i < searchItems.size(); i++) {
            JMenuItem mi = searchItems.get(i);
            if (i == searchSelectedIndex) {
                mi.setBackground(new Color(230, 240, 255));
            } else {
                mi.setBackground(Color.WHITE);
            }
        }
        popupSearch.repaint();
    }

    private void applyPlaceholderBehavior(JTextField field) {
        if (field.getText().isEmpty()) {
            field.setText(searchPlaceholder);
            field.setForeground(new Color(160,160,160));
        }
    }

    // Hiển thị dialog mã QR Momo khi thanh toán chuyển khoản
    private void showMomoDialog() {
        try {
            java.net.URL imgUrl = getClass().getResource("/momo.jpg");
            if (imgUrl == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy ảnh momo.jpg trong resources!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ImageIcon originalIcon = new ImageIcon(imgUrl);
            // Scale ảnh vừa với dialog, ví dụ chiều rộng tối đa 350px
            int maxWidth = 350;
            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();
            if (width > maxWidth) {
                double ratio = (double) maxWidth / width;
                width = maxWidth;
                height = (int) (height * ratio);
            }
            Image scaled = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JLabel lbl = new JLabel(scaledIcon);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);

            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thanh toán qua Momo", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().setLayout(new BorderLayout(10, 10));
            dialog.getContentPane().add(lbl, BorderLayout.CENTER);

            JLabel note = new JLabel("Vui lòng quét mã để chuyển khoản, sau đó nhấn Xác nhận để hoàn tất.");
            note.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            dialog.getContentPane().add(note, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể hiển thị ảnh Momo:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
