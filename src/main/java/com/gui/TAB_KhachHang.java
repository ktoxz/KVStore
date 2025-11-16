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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.dao.DAO_KhachHang;
import com.entity.KhachHang;
import com.toedter.calendar.JDateChooser;

public class TAB_KhachHang extends JPanel implements ActionListener, MouseListener, TabStyleSupport {
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

    private static final Color CLR_PRIMARY = new Color(33, 150, 243);
    private static final Color CLR_WARNING = new Color(255, 193, 7);
    private static final Color CLR_SUCCESS = new Color(76, 175, 80);
    private static final Color CLR_MUTED = new Color(158, 158, 158);
    private static final Color CLR_TEXT_LIGHT = Color.WHITE;

    public TAB_KhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northWrapper = new JPanel(new BorderLayout(0, 10));
        northWrapper.setOpaque(false);
        northWrapper.add(createHeader("QUẢN LÝ KHÁCH HÀNG"), BorderLayout.NORTH);
        northWrapper.add(buildNorthSearch(), BorderLayout.CENTER);
        add(northWrapper, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftForm(), createRightTable());
        split.setResizeWeight(0.36);
        split.setContinuousLayout(true);
        split.setDividerLocation(420);
        split.setOpaque(false);
        split.setBorder(null);
        split.setEnabled(false);
        add(split, BorderLayout.CENTER);

        // ===== LOAD DATA =====
        DocDuLieuVaoDatabase();

        // ===== SỰ KIỆN =====
        btnThem.addActionListener(this);
        btnSua.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);
        tableKH.addMouseListener(this);

        applyContentFont(this);
    }

    private JPanel createLeftForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new CompoundBorder(
                createSectionBorder("Thông tin khách hàng"),
                new EmptyBorder(10, 12, 12, 12)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;
        txtMaKH = new JTextField();
        txtMaKH.setEditable(false);
        addFormRow(form, gbc, row++, "Mã khách hàng:", txtMaKH);

        txtTenKH = new JTextField();
        addFormRow(form, gbc, row++, "Họ tên:", txtTenKH);

        radNam = new JRadioButton("Nam", true);
        radNu = new JRadioButton("Nữ");
        ButtonGroup groupGT = new ButtonGroup();
        groupGT.add(radNam);
        groupGT.add(radNu);
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setOpaque(false);
        genderPanel.add(radNam);
        genderPanel.add(radNu);
        addFormRow(form, gbc, row++, "Giới tính:", genderPanel);

        txtSDT = new JTextField();
        addFormRow(form, gbc, row++, "SĐT:", txtSDT);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setDate(new Date());
        addFormRow(form, gbc, row++, "Ngày tạo:", dateChooser);

        txtDiemTichLuy = new JTextField("0");
        txtDiemTichLuy.setEditable(false);
        addFormRow(form, gbc, row++, "Điểm tích lũy:", txtDiemTichLuy);

        wrapper.add(form, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        actions.setOpaque(false);
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnLamMoi = new JButton("Làm mới");
        styleButton(btnThem, CLR_PRIMARY, CLR_TEXT_LIGHT);
        styleButton(btnSua, CLR_WARNING, Color.BLACK);
        styleButton(btnLamMoi, CLR_SUCCESS, CLR_TEXT_LIGHT);
        actions.add(btnThem);
        actions.add(btnSua);
        actions.add(btnLamMoi);
        wrapper.add(actions, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createRightTable() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
                createSectionBorder("Danh sách khách hàng"),
                new EmptyBorder(10, 12, 12, 12)));

        String[] header = {"Mã KH", "Tên KH", "Giới tính", "SĐT", "Ngày tạo", "Điểm tích lũy"};
        modelKH = new DefaultTableModel(header, 0);
        tableKH = new JTable(modelKH);
        tableKH.setRowHeight(40);
        JScrollPane pane = new JScrollPane(tableKH);
        pane.setBorder(new LineBorder(new Color(230, 230, 230)));
        card.add(pane, BorderLayout.CENTER);

        JPanel pPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pPagination.setOpaque(false);
        btnPrev = new JButton("< Trang trước");
        btnNext = new JButton("Trang sau >");
        lblPageInfo = new JLabel("Trang 1 / 1");
        styleButton(btnPrev, CLR_MUTED, CLR_TEXT_LIGHT);
        styleButton(btnNext, CLR_PRIMARY, CLR_TEXT_LIGHT);
        pPagination.add(btnPrev);
        pPagination.add(lblPageInfo);
        pPagination.add(btnNext);
        card.add(pPagination, BorderLayout.SOUTH);

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

        return card;
    }

    private JComponent buildNorthSearch() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JLabel lblTim = new JLabel("Tìm khách bằng số điện thoại:");
        txtTim = new JTextField();
        btnTim = new JButton("Tìm");
        styleButton(btnTim, CLR_PRIMARY, CLR_TEXT_LIGHT);
        panel.add(lblTim, BorderLayout.WEST);
        panel.add(txtTim, BorderLayout.CENTER);
        panel.add(btnTim, BorderLayout.EAST);
        return panel;
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

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void styleButton(AbstractButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        if (button instanceof JButton btn) {
            btn.setPreferredSize(new Dimension(130, 32));
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