package com.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import com.dao.DAO_KhachHang;
import com.entity.KhachHang;
import com.toedter.calendar.JDateChooser;

public class TAB_KhachHang extends JPanel implements ActionListener, MouseListener {
    private JTextField txtMaKH, txtTenKH, txtSDT, txtDiemTichLuy, txtTim;
    private JRadioButton radNam, radNu;
    private JDateChooser dateChooser;
    private JButton btnThem, btnSua, btnTim, btnLamMoi;
    private JTable tableKH;
    private DefaultTableModel modelKH;

    // ===== PHÂN TRANG + TÌM KIẾM =====
    private List<KhachHang> listAll = null; // ← KHỞI TẠO NULL, SẼ LOAD SAU
    private int currentPage = 1;
    private final int rowsPerPage = 10;
    private JLabel lblPageInfo;
    private JButton btnPrev, btnNext;

    private DAO_KhachHang kh_dao = new DAO_KhachHang();

    public TAB_KhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== HEADER =====
        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 90, 200));
        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeader.setBackground(Color.WHITE);
        pHeader.add(lblTitle);
        add(pHeader, BorderLayout.NORTH);

        // ===== CENTER =====
        JPanel pCenter = new JPanel(new BorderLayout());

        // ===== LEFT: FORM =====
        JPanel pLeft = createLeftForm();
        pCenter.add(pLeft, BorderLayout.WEST);

        // ===== RIGHT: TABLE + SEARCH + PAGINATION =====
        JPanel pRight = createRightTable();
        pCenter.add(pRight, BorderLayout.CENTER);

        add(pCenter, BorderLayout.CENTER);

        // ===== LOAD DATA =====
        DocDuLieuVaoDatabase();

        // ===== SỰ KIỆN =====
        btnThem.addActionListener(this);
        btnSua.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);
        tableKH.addMouseListener(this);
    }

    private JPanel createLeftForm() {
        JPanel pLeft = new JPanel();
        Font font = new Font("Arial", Font.BOLD, 18);
        TitledBorder border = BorderFactory.createTitledBorder("Thông tin khách hàng");
        border.setTitleColor(Color.BLUE);
        border.setTitleFont(font);
        pLeft.setBorder(border);

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
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setPreferredSize(new Dimension(180, 25));
        genderPanel.add(radNam); genderPanel.add(radNu);
        b3.add(lblGT); b3.add(genderPanel);

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
        dateChooser = new JDateChooser();
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
        Color textColor = Color.WHITE;
        btnThem = new JButton("Thêm");
        btnThem.setBackground(Color.BLUE);
        btnThem.setForeground(textColor);
        btnThem.setPreferredSize(sizeBtn);

        btnSua = new JButton("Sửa");
        btnSua.setBackground(Color.ORANGE);
        btnSua.setForeground(textColor);
        btnSua.setPreferredSize(sizeBtn);

        btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setBackground(Color.GREEN);
        btnLamMoi.setForeground(textColor);
        btnLamMoi.setPreferredSize(sizeBtn);

        Box bBtns = Box.createHorizontalBox();
        bBtns.add(btnThem);
        bBtns.add(Box.createHorizontalStrut(10));
        bBtns.add(btnSua);
        bBtns.add(Box.createHorizontalStrut(10));
        bBtns.add(btnLamMoi);

        // Ảnh
        ImageIcon icon = new ImageIcon("src/main/resources/login_img.png");
        Image scaled = icon.getImage().getScaledInstance(400, 420, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
        Box bIcon = Box.createHorizontalBox();
        bIcon.add(imgLabel);

        // Add to box
        box.add(b1); box.add(Box.createVerticalStrut(8));
        box.add(b2); box.add(Box.createVerticalStrut(8));
        box.add(b3); box.add(Box.createVerticalStrut(8));
        box.add(b4); box.add(Box.createVerticalStrut(8));
        box.add(b5); box.add(Box.createVerticalStrut(8));
        box.add(b6); box.add(Box.createVerticalStrut(12));
        box.add(bBtns); box.add(Box.createVerticalStrut(8));
        box.add(bIcon);

        pLeft.add(box, BorderLayout.NORTH);
        return pLeft;
    }

    private JPanel createRightTable() {
        JPanel pRight = new JPanel(new BorderLayout());
        Font font = new Font("Arial", Font.BOLD, 18);
        TitledBorder border = BorderFactory.createTitledBorder("Danh sách khách hàng");
        border.setTitleColor(Color.BLUE);
        border.setTitleFont(font);
        pRight.setBorder(border);

        // Tìm kiếm
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblTim = new JLabel("Tìm khách bằng số điện thoại:");
        lblTim.setPreferredSize(new Dimension(180, 25));
        txtTim = new JTextField(20);
        btnTim = new JButton("Tìm");
        pSearch.add(lblTim);
        pSearch.add(txtTim);
        pSearch.add(btnTim);

        // Bảng
        String[] header = {"Mã KH", "Tên KH", "Giới tính", "SĐT", "Ngày tạo", "Điểm tích lũy"};
        modelKH = new DefaultTableModel(header, 0);
        tableKH = new JTable(modelKH);
        tableKH.setRowHeight(40);
        JScrollPane pane = new JScrollPane(tableKH);

        // Phân trang
        JPanel pPagination = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPrev = new JButton("< Trang trước");
        btnNext = new JButton("Trang sau >");
        lblPageInfo = new JLabel("Trang 1 / 1");
        pPagination.add(btnPrev);
        pPagination.add(lblPageInfo);
        pPagination.add(btnNext);

        // Thêm sự kiện phân trang
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadDataToTable();
            }
        });
        btnNext.addActionListener(e -> {
            int totalKH = kh_dao.getTongSoKhachHang();
            int totalPage = (int) Math.ceil((double) totalKH / rowsPerPage);
            if (currentPage < totalPage) {
                currentPage++;
                loadDataToTable();
            }
        });

        pRight.add(pSearch, BorderLayout.NORTH);
        pRight.add(pane, BorderLayout.CENTER);
        pRight.add(pPagination, BorderLayout.SOUTH);
        return pRight;
    }

    private void loadDataToTable() {
        modelKH.setRowCount(0);
        List<KhachHang> listPage = kh_dao.getKhachHangTheoTrang(currentPage, rowsPerPage);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (KhachHang kh : listPage) {
            modelKH.addRow(new Object[]{
                kh.getMaKH(),
                kh.getTenKH(),
                kh.isGioiTinh() ? "Nữ" : "Nam",
                kh.getSdt(),
                kh.getNgayTao().format(fmt),
                kh.getDiemTichLuy()
            });
        }
        int totalKH = kh_dao.getTongSoKhachHang();
        int totalPage = (int) Math.ceil((double) totalKH / rowsPerPage);
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPage);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPage);
    }

    public void DocDuLieuVaoDatabase() {
        currentPage = 1;
        listAll = kh_dao.getAllKhachHang(); // ← KHỞI TẠO listAll
        loadDataToTable();
    }

    public boolean ValidDate() {
        String tenKH = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim();
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày tạo!");
            return false;
        }
        LocalDate ngayTao = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Tên KH
        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!");
            txtTenKH.requestFocus();
            return false;
        }
        if (!tenKH.matches("([\\p{Lu}][\\p{Ll}]+)( [\\p{Lu}][\\p{Ll}]+)*")) {
            JOptionPane.showMessageDialog(this, "Tên khách hàng chưa đúng định dạng (VD: Nguyễn Văn A)!");
            txtTenKH.requestFocus();
            return false;
        }
        if (tenKH.length() > 100) {
            JOptionPane.showMessageDialog(this, "Tên khách hàng không được vượt quá 100 ký tự!");
            txtTenKH.requestFocus();
            return false;
        }

        // SĐT
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!");
            txtSDT.requestFocus();
            return false;
        }
        if (sdt.length() != 10 || !sdt.matches("0[0-9]{9}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải có 10 chữ số, bắt đầu bằng 0!");
            txtSDT.requestFocus();
            return false;
        }

        // Ngày tạo
        if (ngayTao.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày tạo không được lớn hơn ngày hiện tại!");
            return false;
        }
        return true;
    }

    public KhachHang revertKHFromTextfields() {
        String maKH = txtMaKH.getText().trim();
        String tenKH = txtTenKH.getText().trim();
        boolean gioiTinh = radNu.isSelected();
        String sdt = txtSDT.getText().trim();
        LocalDate ngayTaoTK = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int diemTichLuy = 0;
        try {
            diemTichLuy = Integer.parseInt(txtDiemTichLuy.getText());
        } catch (Exception e) {}
        return new KhachHang(maKH, tenKH, gioiTinh, sdt, ngayTaoTK, diemTichLuy);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        // ===== THÊM =====
        if (o == btnThem) {
            if (!ValidDate()) return;

            String sdt = txtSDT.getText().trim();
            if (kh_dao.isTrungSoDienThoai(sdt)) {
                JOptionPane.showMessageDialog(this, "Số điện thoại đã tồn tại!");
                txtSDT.requestFocus();
                return;
            }

            String maKH = kh_dao.phatSinhMaKH();
            txtMaKH.setText(maKH);

            KhachHang kh = revertKHFromTextfields();
            kh.setDiemTichLuy(0);

            if (kh_dao.themKH(kh)) {
                listAll = kh_dao.getAllKhachHang(); // Cập nhật listAll
                int newIndex = listAll.size() - 1;
                currentPage = (newIndex / rowsPerPage) + 1;
                loadDataToTable();

                int rowInTable = newIndex % rowsPerPage;
                if (rowInTable < modelKH.getRowCount()) {
                    tableKH.setRowSelectionInterval(rowInTable, rowInTable);
                    tableKH.scrollRectToVisible(tableKH.getCellRect(rowInTable, 0, true));
                }
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        }

        // ===== SỬA =====
        else if (o == btnSua) {
            if (!ValidDate()) return;
            if (txtMaKH.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để sửa!");
                return;
            }
            KhachHang kh = revertKHFromTextfields();
            if (kh_dao.capNhatKH(kh)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                int row = tableKH.getSelectedRow();
                if (row >= 0) {
                    modelKH.setValueAt(kh.getTenKH(), row, 1);
                    modelKH.setValueAt(kh.isGioiTinh() ? "Nữ" : "Nam", row, 2);
                    modelKH.setValueAt(kh.getDiemTichLuy(), row, 5);
                }
                loadDataToTable(); // Tải lại để đồng bộ
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        }

        // ===== LÀM MỚI =====
        else if (o == btnLamMoi) {
            txtMaKH.setText("");
            txtTenKH.setText("");
            radNam.setSelected(true); // Chỉ set true cho cái muốn chọn
            txtSDT.setText("");
            dateChooser.setDate(new Date());
            txtDiemTichLuy.setText("0");
            txtTim.setText("");
            txtTenKH.requestFocus();
            currentPage = 1;
            loadDataToTable();
            tableKH.clearSelection();
        }

        // ===== TÌM KIẾM =====
        else if (o == btnTim) {
            String sdt = txtTim.getText().trim();
            if (sdt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!");
                return;
            }
            if (listAll == null) listAll = kh_dao.getAllKhachHang(); // Đảm bảo có dữ liệu

            KhachHang kh = kh_dao.timKiemKH(sdt);
            if (kh == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng!");
                return;
            }

            int index = -1;
            for (int i = 0; i < listAll.size(); i++) {
                if (listAll.get(i).getMaKH().equals(kh.getMaKH())) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return;

            int targetPage = (index / rowsPerPage) + 1;
            if (currentPage != targetPage) {
                currentPage = targetPage;
                loadDataToTable();
            }

            int rowInTable = index % rowsPerPage;
            tableKH.setRowSelectionInterval(rowInTable, rowInTable);
            tableKH.scrollRectToVisible(tableKH.getCellRect(rowInTable, 0, true));

            // Đưa lên form
            txtMaKH.setText(kh.getMaKH());
            txtTenKH.setText(kh.getTenKH());
            if (kh.isGioiTinh()) radNu.setSelected(true);
            else radNam.setSelected(true);
            txtSDT.setText(kh.getSdt());
            txtDiemTichLuy.setText(String.valueOf(kh.getDiemTichLuy()));
            dateChooser.setDate(java.sql.Date.valueOf(kh.getNgayTao()));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = tableKH.getSelectedRow();
        if (row < 0) return;

        txtMaKH.setText(modelKH.getValueAt(row, 0).toString());
        txtTenKH.setText(modelKH.getValueAt(row, 1).toString());
        String gt = modelKH.getValueAt(row, 2).toString();
        radNam.setSelected("Nam".equalsIgnoreCase(gt));
        radNu.setSelected("Nữ".equalsIgnoreCase(gt));
        txtSDT.setText(modelKH.getValueAt(row, 3).toString());
        txtDiemTichLuy.setText(modelKH.getValueAt(row, 5).toString());

        String dateStr = modelKH.getValueAt(row, 4).toString();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dateChooser.setDate(sdf.parse(dateStr));
        } catch (ParseException ex) {
            dateChooser.setDate(new Date());
        }
    }

    // Các mouse event khác
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}