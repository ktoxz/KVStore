package com.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;
import com.service.TabStyler;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TAB_KhachHang extends JPanel implements ActionListener, MouseListener {

    private static final long serialVersionUID = 1L;

    DAO_KhachHang dao = new DAO_KhachHang();

    // Colors giống TAB_NhanVien
    static final Color
            CLR_PRIMARY = new Color(33,150,243),
            CLR_WARNING = new Color(255,193,7),
            CLR_SUCCESS = new Color(76,175,80),
            CLR_TEXT_LIGHT = Color.WHITE,
            CLR_TEXT_DARK = Color.BLACK;

    // UI
    JTextField txtSearch, txtMaKH, txtTenKH, txtSDT, txtDiem;
    JRadioButton radNam, radNu;
    ButtonGroup grpGT;
    JDateChooser dcNgayTao;

    JButton btnThem, btnSua, btnLamMoi, btnTim;

    JTable tbl;
    DefaultTableModel mdl;

    // Paging
    JPanel pnlPaging;
    int currentPage = 1;
    int totalPages = 1;
    final int pageSize = 10;

    public TAB_KhachHang() {

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.setOpaque(false);
        northWrapper.add(TabStyler.createHeader("QUẢN LÝ KHÁCH HÀNG"), BorderLayout.NORTH);
        northWrapper.add(buildSearchBar(), BorderLayout.CENTER);
        add(northWrapper, BorderLayout.NORTH);

        JComponent leftForm = buildLeftForm();
        JComponent rightTable = buildRightTable();

        leftForm.setBorder(createTitleBorder("Thông tin khách hàng"));
        rightTable.setBorder(createTitleBorder("Danh sách khách hàng"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftForm, rightTable);
        split.setResizeWeight(0.35);
        split.setDividerLocation(450);
        split.setContinuousLayout(true);
        split.setEnabled(false);

        add(split, BorderLayout.CENTER);

        bindEvents();

        loadPage(1);

        TabStyler.applyContentFont(this);
    }

    private JComponent buildSearchBar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        JLabel lb = new JLabel("Tìm khách hàng:");
        txtSearch = new JTextField();
        btnTim = new JButton("Tìm");
        styleButton(btnTim, CLR_PRIMARY, CLR_TEXT_LIGHT);

        p.add(lb, BorderLayout.WEST);
        p.add(txtSearch, BorderLayout.CENTER);
        p.add(btnTim, BorderLayout.EAST);

        return p;
    }

    private JComponent buildLeftForm() {
        JPanel main = new JPanel(new BorderLayout(0, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        int row = 0;

        txtMaKH = new JTextField();
        txtMaKH.setEditable(false);
        addFormRow(form, gbc, row++, "Mã khách hàng:", txtMaKH);

        txtTenKH = new JTextField();
        addFormRow(form, gbc, row++, "Họ tên:", txtTenKH);

        radNam = new JRadioButton("Nam");
        radNu = new JRadioButton("Nữ");
        grpGT = new ButtonGroup();
        grpGT.add(radNam);
        grpGT.add(radNu);
        radNam.setSelected(true);

        JPanel pnlGT = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlGT.add(radNam);
        pnlGT.add(radNu);
        addFormRow(form, gbc, row++, "Giới tính:", pnlGT);

        txtSDT = new JTextField();
        addFormRow(form, gbc, row++, "SĐT:", txtSDT);

        dcNgayTao = new JDateChooser();
        dcNgayTao.setDateFormatString("dd/MM/yyyy");
        dcNgayTao.setDate(new java.util.Date());
        addFormRow(form, gbc, row++, "Ngày tạo:", dcNgayTao);

        txtDiem = new JTextField("0");
        txtDiem.setEditable(false);
        addFormRow(form, gbc, row++, "Điểm tích lũy:", txtDiem);

        main.add(form, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
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

        ImageIcon icon = new ImageIcon("src/main/resources/login_img.png");
        Image scaled = icon.getImage().getScaledInstance(260, 260, Image.SCALE_SMOOTH);
        lbImg.setIcon(new ImageIcon(scaled));

        main.add(lbImg, BorderLayout.SOUTH);

        return main;
    }

    private JComponent buildRightTable() {
        String[] cols = { "Mã KH", "Tên KH", "Giới tính", "SĐT", "Ngày tạo", "Điểm" };
        mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tbl = new JTable(mdl);
        tbl.setRowHeight(28);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(2).setCellRenderer(center);
        tbl.getColumnModel().getColumn(4).setCellRenderer(center);

        JScrollPane sp = new JScrollPane(tbl);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(sp, BorderLayout.CENTER);

        pnlPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        pnlPaging.setBorder(new EmptyBorder(6, 0, 0, 0));
        wrap.add(pnlPaging, BorderLayout.SOUTH);

        return wrap;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String name, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel(name), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void bindEvents() {
        btnThem.addActionListener(this);
        btnSua.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);
        txtSearch.addActionListener(this);
        tbl.addMouseListener(this);
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }

    private CompoundBorder createTitleBorder(String title) {
        Color color = new Color(30,144,255);
        float fontSize = 20f;

        LineBorder line = new LineBorder(new Color(200,200,200), 1, true);
        TitledBorder titled = new TitledBorder(line, title, TitledBorder.LEFT, TitledBorder.TOP);
        titled.setTitleColor(color);
        titled.setTitleFont(TabStyler.SECTION_FONT.deriveFont(fontSize));

        return new CompoundBorder(
                new EmptyBorder(8,8,8,8),
                new CompoundBorder(
                        titled,
                        new EmptyBorder(10,12,12,12)
                )
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object o = e.getSource();

        if (o.equals(btnTim) || o.equals(txtSearch)) {
            loadPage(1);
            return;
        }

        if (o.equals(btnLamMoi)) {
            clearForm();
            loadPage(1);
            return;
        }

        if (o.equals(btnThem)) {
            KhachHang kh = getKHFromForm(true);
            if (kh == null) return;

            if (!dao.themKH(kh)) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!");
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadPage(Integer.MAX_VALUE);
            selectRowByMa(kh.getMaKH());
            return;
        }

        if (o.equals(btnSua)) {
            KhachHang kh = getKHFromForm(false);
            if (kh == null) return;

            if (!dao.capNhatKH(kh)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                return;
            }

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadPage(currentPage);
            selectRowByMa(kh.getMaKH());
            return;
        }
    }

    private void clearForm() {
        txtMaKH.setText("");
        txtTenKH.setText("");
        txtSDT.setText("");
        txtDiem.setText("0");
        radNam.setSelected(true);
        dcNgayTao.setDate(new java.util.Date());
    }

    private KhachHang getKHFromForm(boolean isNew) {

        String ma = txtMaKH.getText().trim();
        String ten = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim();
        boolean gt = radNam.isSelected();
        LocalDate ngay = dcNgayTao.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên không được rỗng!");
            txtTenKH.requestFocus();
            return null;
        }

        if (!sdt.matches("0\\d{9}")) {
            JOptionPane.showMessageDialog(this, "SĐT 10 số, bắt đầu bằng 0");
            txtSDT.requestFocus();
            return null;
        }

        if (isNew) {
            ma = dao.phatSinhMaKH();
        }

        return new KhachHang(ma, ten, gt, sdt, ngay, Integer.parseInt(txtDiem.getText()));
    }

    private void loadPage(int page) {
        String keyword = txtSearch.getText().trim();

        int totalRecords = dao.getTongSoKhachHang();
        totalPages = (int) Math.ceil(totalRecords / (double) pageSize);
        if (totalPages == 0) totalPages = 1;

        if (page == Integer.MAX_VALUE) page = totalPages;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        currentPage = page;

        List<KhachHang> ds = dao.getKhachHangTheoTrang(currentPage, pageSize);

        mdl.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (KhachHang kh : ds) {
            mdl.addRow(new Object[]{
                    kh.getMaKH(), kh.getTenKH(),
                    kh.isGioiTinh() ? "Nam" : "Nữ",
                    kh.getSdt(),
                    kh.getNgayTao().format(fmt),
                    kh.getDiemTichLuy()
            });
        }

        updatePaging();
    }

    private void updatePaging() {
        pnlPaging.removeAll();

        if (totalPages <= 1) {
            pnlPaging.revalidate();
            pnlPaging.repaint();
            return;
        }

        JButton bFirst = new JButton("|<");
        bFirst.addActionListener(e -> loadPage(1));
        pnlPaging.add(bFirst);

        JButton bPrev = new JButton("<");
        bPrev.addActionListener(e -> loadPage(currentPage - 1));
        pnlPaging.add(bPrev);

        int maxBtn = 7;
        int start = Math.max(1, currentPage - 3);
        int end = Math.min(totalPages, start + maxBtn - 1);

        for (int i = start; i <= end; i++) {
            JButton btn = new JButton(String.valueOf(i));
            if (i == currentPage) btn.setEnabled(false);
            int target = i;
            btn.addActionListener(e -> loadPage(target));
            pnlPaging.add(btn);
        }

        JButton bNext = new JButton(">");
        bNext.addActionListener(e -> loadPage(currentPage + 1));
        pnlPaging.add(bNext);

        JButton bLast = new JButton(">|");
        bLast.addActionListener(e -> loadPage(totalPages));
        pnlPaging.add(bLast);

        pnlPaging.revalidate();
        pnlPaging.repaint();
    }

    private void selectRowByMa(String maKH) {
        if (maKH == null || maKH.isEmpty()) return;
        for (int i = 0; i < tbl.getRowCount(); i++) {
            if (maKH.equals(tbl.getValueAt(i, 0))) {
                tbl.setRowSelectionInterval(i, i);
                tbl.scrollRectToVisible(tbl.getCellRect(i, 0, true));
                break;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(tbl) && e.getClickCount() >= 1) {
            int row = tbl.getSelectedRow();
            if (row >= 0) {
                String ma = tbl.getValueAt(row, 0).toString();
                KhachHang kh = dao.timKiemKHById(ma);
                fillForm(kh);
            }
        }
    }

    private void fillForm(KhachHang kh) {
        if (kh == null) return;

        txtMaKH.setText(kh.getMaKH());
        txtTenKH.setText(kh.getTenKH());
        if (kh.isGioiTinh()) radNam.setSelected(true);
        else radNu.setSelected(true);

        txtSDT.setText(kh.getSdt());
        dcNgayTao.setDate(java.sql.Date.valueOf(kh.getNgayTao()));
        txtDiem.setText(String.valueOf(kh.getDiemTichLuy()));
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
