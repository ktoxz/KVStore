package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.enums.ChucVu;

import java.awt.*;
import java.awt.event.*;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
import com.service.EmailService;
import com.toedter.calendar.JDateChooser;

public class TAB_NhanVien extends JPanel implements ActionListener, MouseListener {

    private static final long serialVersionUID = 1L;

    DAO_NhanVien dao;

    // Theme
    static final Color
            CLR_PRIMARY = new Color(33, 150, 243),
            CLR_WARNING = new Color(255, 193, 7),
            CLR_SUCCESS = new Color(76, 175, 80),
            CLR_MUTED = new Color(158, 158, 158),
            CLR_TEXT_LIGHT = Color.WHITE,
            CLR_TEXT_DARK = Color.BLACK;

    // UI
    JTextField txtSearch, txtMaNV, txtTenNV, txtEmail, txtSdt;
    JRadioButton radNam, radNu;
    ButtonGroup grpGioiTinh;
    JDateChooser dcNgayTao;
    JComboBox<ChucVu> cboChucVu;
    JButton btnThem, btnSua, btnLamMoi, btnTim;
    JTable tbl;
    DefaultTableModel mdl;
    JPanel pnlPaging;
    JLabel lbPageInfo;
    JButton btnFirst, btnPrev, btnNext, btnLast;

    // Paging
    int pageSize = 25, currentPage = 1, totalPages = 1, totalRows = 0;
    String currentKeyword = "";


    public TAB_NhanVien() {
        dao = new DAO_NhanVien();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildNorthSearch(), BorderLayout.NORTH);

        JComponent leftForm = buildWestForm();
        JComponent rightTable = buildCenterTable();

        leftForm.setBorder(createTitleBorder("Thông tin nhân viên", new Color(30, 144, 255), 20f, 1));
        rightTable.setBorder(createTitleBorder("Danh sách nhân viên", new Color(30, 144, 255), 20f, 1));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftForm, rightTable);
        split.setResizeWeight(0.35);              // form bên trái, bảng bên phải
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        add(split, BorderLayout.CENTER);

        // tiêu đề lớn giống TAB_KhachHang
        setBorder(createTitleBorder("QUẢN LÝ NHÂN VIÊN", new Color(0, 102, 204), 22f, 0));

        bindEvents();
        initData();
    }

    private JComponent buildNorthSearch() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        JLabel lb = new JLabel("Tìm nhân viên :");
        txtSearch = new JTextField();
        btnTim = new JButton("Tìm");
        styleButton(btnTim, CLR_PRIMARY, CLR_TEXT_LIGHT);

        p.add(lb, BorderLayout.WEST);
        p.add(txtSearch, BorderLayout.CENTER);
        p.add(btnTim, BorderLayout.EAST);

        return p;
    }

    private JComponent buildWestForm() {
        JPanel main = new JPanel(new BorderLayout(0, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;

        txtMaNV = new JTextField();
        txtMaNV.setEditable(false);
        addFormRow(form, gbc, row++, "Mã nhân viên:", txtMaNV);

        txtTenNV = new JTextField();
        addFormRow(form, gbc, row++, "Họ tên:", txtTenNV);

        radNam = new JRadioButton("Nam");
        radNu = new JRadioButton("Nữ");
        grpGioiTinh = new ButtonGroup();
        grpGioiTinh.add(radNam);
        grpGioiTinh.add(radNu);
        radNam.setSelected(true);

        JPanel pnlGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlGender.add(radNam);
        pnlGender.add(radNu);
        addFormRow(form, gbc, row++, "Giới tính:", pnlGender);

        txtEmail = new JTextField();
        addFormRow(form, gbc, row++, "Email:", txtEmail);

        txtSdt = new JTextField();
        addFormRow(form, gbc, row++, "SĐT:", txtSdt);

        dcNgayTao = new JDateChooser();
        dcNgayTao.setDateFormatString("dd/MM/yyyy");
        addFormRow(form, gbc, row++, "Ngày tạo TK:", dcNgayTao);

        cboChucVu = new JComboBox<>();
        for (ChucVu cv : ChucVu.values()) {
            cboChucVu.addItem(cv);
        }
        addFormRow(form, gbc, row++, "Chức vụ:", cboChucVu);

        main.add(form, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnLamMoi = new JButton("Làm mới");

        styleButton(btnThem, CLR_PRIMARY, CLR_TEXT_LIGHT);
        styleButton(btnSua, CLR_WARNING, CLR_TEXT_DARK);
        styleButton(btnLamMoi, CLR_SUCCESS, CLR_TEXT_LIGHT);

        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnLamMoi);

        main.add(pnlButtons, BorderLayout.CENTER);

        JLabel lbImg = new JLabel();
        lbImg.setHorizontalAlignment(SwingConstants.CENTER);
        lbImg.setBorder(new EmptyBorder(16, 0, 0, 0));
        // Đường dẫn ảnh chỉnh lại cho đúng với project của bạn
        lbImg.setIcon(new ImageIcon("src/main/resources/ui/kvstore.png"));
        main.add(lbImg, BorderLayout.SOUTH);

        return main;
    }

    private JComponent buildCenterTable() {
        String[] cols = { "Mã NV", "Tên NV", "Giới tính", "Email", "SĐT", "Ngày tạo TK", "Chức vụ" };
        mdl = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbl = new JTable(mdl);
        tbl.setRowHeight(28);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // giới tính
        tbl.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // ngày tạo
        tbl.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // chức vụ

        JScrollPane sp = new JScrollPane(tbl);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(sp, BorderLayout.CENTER);

        // thanh phân trang bên dưới
        pnlPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        pnlPaging.setBorder(new EmptyBorder(6, 0, 0, 0));

        btnFirst = new JButton("|<");
        btnPrev  = new JButton("<");
        btnNext  = new JButton(">");
        btnLast  = new JButton(">|");
        styleButton(btnFirst, CLR_MUTED, CLR_TEXT_LIGHT);
        styleButton(btnPrev,  CLR_MUTED, CLR_TEXT_LIGHT);
        styleButton(btnNext,  CLR_MUTED, CLR_TEXT_LIGHT);
        styleButton(btnLast,  CLR_MUTED, CLR_TEXT_LIGHT);

        lbPageInfo = new JLabel("Trang 1/1");

        pnlPaging.add(btnFirst);
        pnlPaging.add(btnPrev);
        pnlPaging.add(lbPageInfo);
        pnlPaging.add(btnNext);
        pnlPaging.add(btnLast);

        wrap.add(pnlPaging, BorderLayout.SOUTH);
        return wrap;

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

    private void bindEvents() {
        btnThem.addActionListener(this);
        btnSua.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);

        if (btnFirst != null) {
            btnFirst.addActionListener(this);
            btnPrev.addActionListener(this);
            btnNext.addActionListener(this);
            btnLast.addActionListener(this);
        }

        tbl.addMouseListener(this);
    }


    private CompoundBorder createTitleBorder(String title, Color color, float fontSize, int thickness) {
        int line = thickness;
        LineBorder lineBorder = new LineBorder(new Color(200, 200, 200), line, true);
        TitledBorder titled = new TitledBorder(lineBorder, title, TitledBorder.LEFT, TitledBorder.TOP);
        titled.setTitleColor(color);
        titled.setTitleFont(new Font("SansSerif", Font.BOLD, (int) fontSize));

        return new CompoundBorder(
                new EmptyBorder(8, 8, 8, 8),            // mép ngoài
                new CompoundBorder(
                        titled,                          // viền có tiêu đề
                        new EmptyBorder(10, 12, 12, 12) // padding giữa viền và nội dung
                )
        );
    }


    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }
    
    private String nextIdFromDB() {
        return dao.getNextMaNhanVien();
    }
    
    private void initData() {
        currentKeyword = "";
        currentPage = 1;
        loadPage();
    }

    private void loadPage() {
        totalRows = dao.countNhanVien(currentKeyword);
        totalPages = (int) Math.ceil((double) totalRows / pageSize);
        if (totalPages <= 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage <= 0) currentPage = 1;

        java.util.List<NhanVien> ds = dao.getNhanVienPage(currentKeyword, currentPage, pageSize);
        fillTable(ds);
        updatePagingLabel();
    }

    private void fillTable(java.util.List<NhanVien> ds) {
        mdl.setRowCount(0);
        for (NhanVien nv : ds) {
            String gioiTinhStr = nv.isGioiTinh() ? "Nam" : "Nữ";
            java.time.LocalDate d = nv.getNgayTaoTaiKhoan();
            String ngay = d != null
                    ? d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "";
            String chucVuStr = nv.getChucVu().toString();

            mdl.addRow(new Object[] {
                    nv.getMaNV(),
                    nv.getTenNV(),
                    gioiTinhStr,
                    nv.getEmail(),
                    nv.getSdt(),
                    ngay,
                    chucVuStr
            });
        }
    }

    private void updatePagingLabel() {
        lbPageInfo.setText("Trang " + currentPage + "/" + totalPages + "  (Tổng: " + totalRows + ")");

        boolean hasPrev = currentPage > 1;
        boolean hasNext = currentPage < totalPages;

        if (btnFirst != null) {
            btnFirst.setEnabled(hasPrev);
            btnPrev.setEnabled(hasPrev);
            btnNext.setEnabled(hasNext);
            btnLast.setEnabled(hasNext);
        }
    }


    private void clearForm() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtEmail.setText("");
        txtSdt.setText("");
        radNam.setSelected(true);
        dcNgayTao.setDate(null);
        if (cboChucVu.getItemCount() > 0) {
            cboChucVu.setSelectedIndex(0);
        }
    }

    private void setFormFromNhanVien(NhanVien nv) {
        if (nv == null) return;
        txtMaNV.setText(nv.getMaNV());
        txtTenNV.setText(nv.getTenNV());
        if (nv.isGioiTinh()) radNam.setSelected(true);
        else radNu.setSelected(true);

        txtEmail.setText(nv.getEmail());
        txtSdt.setText(nv.getSdt());

        if (nv.getNgayTaoTaiKhoan() != null) {
            dcNgayTao.setDate(java.sql.Date.valueOf(nv.getNgayTaoTaiKhoan()));
        } else {
            dcNgayTao.setDate(null);
        }

        ChucVu cv = nv.getChucVu();
        if (cv != null) {
            cboChucVu.setSelectedItem(cv);
        } else {
            cboChucVu.setSelectedIndex(-1);
        }
    }

    private NhanVien getNhanVienFromForm(boolean isNew) {
        String ma = txtMaNV.getText().trim();
        String ten = txtTenNV.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSdt.getText().trim();
        boolean gioiTinh = radNam.isSelected();

        java.util.Date d = dcNgayTao.getDate();
        java.time.LocalDate ngay = d != null
                ? new java.sql.Date(d.getTime()).toLocalDate()
                : java.time.LocalDate.now();

        ChucVu cv = (ChucVu) cboChucVu.getSelectedItem();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được rỗng!");
            txtTenNV.requestFocus();
            return null;
        }
        // thêm mới: không cần kiểm tra mã, mã sẽ được sinh tự động
        if (!isNew && ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!");
            return null;
        }

        return new NhanVien(ma, ten, gioiTinh, email, sdt, ngay, cv);
    }


    private void selectRowByMa(String maNV) {
        if (maNV == null || maNV.isEmpty()) return;
        for (int i = 0; i < tbl.getRowCount(); i++) {
            Object v = tbl.getValueAt(i, 0);
            if (maNV.equals(v)) {
                tbl.setRowSelectionInterval(i, i);
                tbl.scrollRectToVisible(tbl.getCellRect(i, 0, true));
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o.equals(btnTim)) {
            currentKeyword = txtSearch.getText().trim();
            currentPage = 1;
            loadPage();
            return;
        }

        if (o.equals(btnLamMoi)) {
            txtSearch.setText("");
            currentKeyword = "";
            clearForm();
            currentPage = 1;
            loadPage();
            return;
        }

        if (o.equals(btnThem)) {
            NhanVien nv = getNhanVienFromForm(true);
            if (nv == null) return;

            // sinh mã NV mới giống Tab_SanPham
            String maMoi = nextIdFromDB();
            nv.setMaNV(maMoi);
            txtMaNV.setText(maMoi);  // hiển thị lại lên form

            if (!dao.insertNhanVien(nv, EmailService.generateActivationCode(5))) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
                return;
            }
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");

            currentKeyword = "";
            txtSearch.setText("");
            initData();
            selectRowByMa(maMoi);
            return;
        }

        if (o.equals(btnSua)) {
            NhanVien nv = getNhanVienFromForm(false);
            if (nv == null) return;

            if (!dao.updateNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!");
                return;
            }
            JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");

            loadPage();
            selectRowByMa(nv.getMaNV());
            return;
        }
        
        if (o.equals(btnFirst)) {
            if (currentPage > 1) {
                currentPage = 1;
                loadPage();
            }
            return;
        }

        if (o.equals(btnPrev)) {
            if (currentPage > 1) {
                currentPage--;
                loadPage();
            }
            return;
        }

        if (o.equals(btnNext)) {
            if (currentPage < totalPages) {
                currentPage++;
                loadPage();
            }
            return;
        }

        if (o.equals(btnLast)) {
            if (currentPage < totalPages) {
                currentPage = totalPages;
                loadPage();
            }
            return;
        }

    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(tbl) && e.getClickCount() >= 1) {
            int row = tbl.getSelectedRow();
            if (row >= 0) {
                String ma = tbl.getValueAt(row, 0).toString();
                NhanVien nv = dao.findById(ma);
                setFormFromNhanVien(nv);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public String createRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        StringBuilder password = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < length; i++) {
            int index = rnd.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}


