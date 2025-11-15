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
import java.awt.image.BufferedImage;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
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

    // Phân trang
    private int currentPageNV = 1;
    private int totalPagesNV  = 1;
    private final int pageSizeNV = 25;   // hoặc giống tab_SanPham
    private String currentKeywordNV = ""; // nếu có ô tìm kiếm theo SĐT



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
        split.setResizeWeight(0.35);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        split.setDividerLocation(450); // hoặc tỉ lệ/px cố định bạn muốn
        split.setEnabled(false);       // KHÓA không cho kéo nữa
        add(split, BorderLayout.CENTER);

        // tiêu đề lớn giống TAB_KhachHang
        setBorder(createTitleBorder("QUẢN LÝ NHÂN VIÊN", new Color(0, 102, 204), 22f, 0));

        bindEvents();
        
        currentKeywordNV = ""; // hoặc lấy từ ô tìm kiếm ban đầu
        loadPageNV(1);

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
        dcNgayTao.setEnabled(false);
        dcNgayTao.setDate(new java.util.Date());
        addFormRow(form, gbc, row++, "Ngày tạo TK:", dcNgayTao);


        cboChucVu = new JComboBox<>();
        for (ChucVu cv : ChucVu.values()) {
            cboChucVu.addItem(cv);
        }
        addFormRow(form, gbc, row++, "Chức vụ:", cboChucVu);

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
        // Đường dẫn ảnh chỉnh lại cho đúng với project của bạn
        
        ImageIcon icon = new ImageIcon("src/main/resources/login_img.png");

        // scale xuống 300x300 rồi gán cho label
        Image scaled = icon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
        lbImg.setIcon(new ImageIcon(scaled));
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
		tbl.getTableHeader().setReorderingAllowed(false);
		tbl.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // giới tính
        tbl.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // ngày tạo
        tbl.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // chức vụ

        JScrollPane sp = new JScrollPane(tbl);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(sp, BorderLayout.CENTER);

        // thanh phân trang bên dưới
        // thanh phân trang bên dưới
        pnlPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        pnlPaging.setBorder(new EmptyBorder(6, 0, 0, 0));

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
    	txtSearch.addActionListener(this);


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

    private void clearForm() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtEmail.setText("");
        txtSdt.setText("");
        radNam.setSelected(true);
        dcNgayTao.setDate(new java.util.Date());
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
        	dcNgayTao.setDate(new java.util.Date());
        }

        ChucVu cv = ChucVu.fromAny(nv.getChucVu());
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

        java.time.LocalDate ngay = null;
        if (isNew) {
            // thêm mới: luôn dùng ngày hiện tại, không cho tự chọn
            ngay = java.time.LocalDate.now();
            dcNgayTao.setDate(java.sql.Date.valueOf(ngay)); // cập nhật lại lên form cho đồng bộ
        } else {
            // sửa: giữ nguyên ngày tạo cũ từ DB (đang hiển thị trên dcNgayTao)
            java.util.Date d = dcNgayTao.getDate();
            if (d != null) {
                ngay = new java.sql.Date(d.getTime()).toLocalDate();
            }
        }

        if (ngay == null) {
            JOptionPane.showMessageDialog(this, "Ngày tạo tài khoản không được rỗng!");
            dcNgayTao.requestFocus();
            return null;
        }


        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được rỗng!");
            txtTenNV.requestFocus();
            return null;
        }
        // Họ tên: chỉ chữ + khoảng trắng, cho phép dấu tiếng Việt, 2–50 ký tự
        if (!ten.matches("^[\\p{L}\\s'.-]{2,50}$")) {
            JOptionPane.showMessageDialog(this,
                    "Họ tên chỉ được chứa chữ cái và khoảng trắng (2-50 ký tự)!");
            txtTenNV.requestFocus();
            return null;
        }

        // Email bắt buộc, định dạng đơn giản: ten@domain.tld
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email không được rỗng!");
            txtEmail.requestFocus();
            return null;
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!");
            txtEmail.requestFocus();
            return null;
        }

        // SĐT bắt buộc: 10 số, cho phép 0xxxxxxxxx hoặc +84xxxxxxxxx
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được rỗng!");
            txtSdt.requestFocus();
            return null;
        }
        if (!sdt.matches("^(0\\d{9}|\\+84\\d{9})$")) {
            JOptionPane.showMessageDialog(this,
                    "Số điện thoại không hợp lệ!\nVD: 0912345678 hoặc +84912345678");
            txtSdt.requestFocus();
            return null;
        }

        // thêm mới: không cần kiểm tra mã, mã sẽ được sinh tự động
        if (!isNew && ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!");
            return null;
        }
        
        ChucVu cv = (ChucVu) cboChucVu.getSelectedItem();
        if (cv == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ!");
            cboChucVu.requestFocus();
            return null;
        }
        String chucVuStr = cv.toDbValue();

        return new NhanVien(ma, ten, gioiTinh, email, sdt, ngay, chucVuStr);
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
    
    private void selectRowByMaNV(String maNV) {
        DefaultTableModel mdl = (DefaultTableModel) tbl.getModel();
        for (int i = 0; i < mdl.getRowCount(); i++) {
            Object val = mdl.getValueAt(i, 0); // cột 0 là mã NV
            if (val != null && maNV.equals(val.toString())) {
                tbl.setRowSelectionInterval(i, i);
                tbl.scrollRectToVisible(tbl.getCellRect(i, 0, true));
                break;
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o.equals(btnTim) || o.equals(txtSearch)) {
            currentKeywordNV = txtSearch.getText().trim();
            loadPageNV(1);
            return;
        }

        if (o.equals(btnLamMoi)) {
            txtSearch.setText("");
            currentKeywordNV = "";
            clearForm();
            loadPageNV(1);
            return;
        }

        if (o.equals(btnThem)) {
            NhanVien nv = getNhanVienFromForm(true);
            if (nv == null) return;

            // sinh mã NV mới giống Tab_SanPham
            String maMoi = nextIdFromDB();
            nv.setMaNV(maMoi);
            txtMaNV.setText(maMoi);  // hiển thị lại lên form

            if (!dao.insertNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
                return;
            }
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");

            // chuyển đến trang cuối (nơi có NV mới) và chọn đúng nhân viên
            int totalRecords = dao.countNhanVien(currentKeywordNV);
            totalPagesNV = (int) Math.ceil(totalRecords / (double) pageSizeNV);
            if (totalPagesNV == 0) totalPagesNV = 1;

            loadPageNV(totalPagesNV);
            selectRowByMaNV(nv.getMaNV());
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
            
            loadPageNV(currentPageNV);     // stay on current page
            selectRowByMaNV(nv.getMaNV()); // focus lại đúng nhân viên vừa sửa
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
    
    /**
     * Tải 1 trang nhân viên từ DB và đổ vào bảng
     */
    private void loadPageNV(int page) {
        if (page < 1) page = 1;
        if (page > totalPagesNV) page = totalPagesNV;

        currentPageNV = page;

        // Lấy tổng số bản ghi
        int totalRecords = dao.countNhanVien(currentKeywordNV);
        totalPagesNV = (int) Math.ceil(totalRecords / (double) pageSizeNV);
        if (totalPagesNV == 0) {
            totalPagesNV = 1;
        }
        if (currentPageNV > totalPagesNV) {
            currentPageNV = totalPagesNV;
        }

        // Lấy đúng 1 trang từ DB
        java.util.List<NhanVien> ds = dao.getNhanVienPage(
                currentKeywordNV, currentPageNV, pageSizeNV);

        // Đổ vào bảng
        DefaultTableModel mdl = (DefaultTableModel) tbl.getModel();
        mdl.setRowCount(0);

        for (NhanVien nv : ds) {
            String gioiTinhStr = nv.isGioiTinh() ? "Nam" : "Nữ";
            String ngayStr = nv.getNgayTaoTaiKhoan() != null
                    ? nv.getNgayTaoTaiKhoan().toString()
                    : "";

            mdl.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getTenNV(),
                    gioiTinhStr,
                    nv.getEmail(),
                    nv.getSdt(),
                    ngayStr,
                    nv.getChucVu()
            });
        }

        updatePagingNV();
    }

    /**
     * Vẽ lại dãy nút phân trang dạng số trong pnlPaging
     */
    private void updatePagingNV() {
        pnlPaging.removeAll();

        // Nếu chỉ có 1 trang thì thôi
        if (totalPagesNV <= 1) {
            pnlPaging.revalidate();
            pnlPaging.repaint();
            return;
        }

        // Nút về đầu & lùi 1
        JButton bFirst = new JButton("|<");
        bFirst.addActionListener(e -> loadPageNV(1));
        pnlPaging.add(bFirst);

        JButton bPrev = new JButton("<");
        bPrev.addActionListener(e -> loadPageNV(Math.max(1, currentPageNV - 1)));
        pnlPaging.add(bPrev);

        // Các nút số trang (giống bên tab_sanpham thường là 7 nút)
        int maxButtons = 7;
        int start = Math.max(1, currentPageNV - 3);
        int end = Math.min(totalPagesNV, start + maxButtons - 1);
        if (end - start + 1 < maxButtons) {
            start = Math.max(1, end - maxButtons + 1);
        }

        for (int i = start; i <= end; i++) {
            final int page = i;
            JButton btn = new JButton(String.valueOf(i));
            if (i == currentPageNV) {
                btn.setEnabled(false); // đang ở trang hiện tại
            }
            btn.addActionListener(e -> loadPageNV(page));
            pnlPaging.add(btn);
        }

        // Nút tiến 1 & về cuối
        JButton bNext = new JButton(">");
        bNext.addActionListener(e -> loadPageNV(Math.min(totalPagesNV, currentPageNV + 1)));
        pnlPaging.add(bNext);

        JButton bLast = new JButton(">|");
        bLast.addActionListener(e -> loadPageNV(totalPagesNV));
        pnlPaging.add(bLast);

        pnlPaging.revalidate();
        pnlPaging.repaint();
    }


    // Demo nhanh (tuỳ chọn)
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JFrame f = new JFrame("QL Nhân viên");
//            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            f.setContentPane(new TAB_NhanVien());
//            f.setSize(1140, 660);
//            f.setLocationRelativeTo(null);
//            f.setVisible(true);
//        });
//    }
}
