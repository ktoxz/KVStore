package com.gui;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;
import com.enums.ThemeColor;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class TAB_KhachHang extends JPanel implements ActionListener, MouseListener {
    private final DAO_KhachHang khDao = new DAO_KhachHang();

    private JTextField txtMaKH, txtTenKH, txtSDT, txtDiemTichLuy, txtTim;
    private JRadioButton radNam, radNu;
    private JDateChooser dateChooser;
    private JButton btnThem, btnSua, btnLamMoi, btnTim;
    private JTable tableKH;
    private DefaultTableModel modelKH;
    private JLabel lblPageInfo;
    private JButton btnPrev, btnNext;

    private List<KhachHang> listAll = null;
    private int currentPage = 1;
    private final int rowsPerPage = 10;

    public TAB_KhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeColor.LIGHT_BG.color());

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftForm(), buildRightTable());
        splitPane.setResizeWeight(0.36);
        splitPane.setContinuousLayout(true);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(0);
        add(splitPane, BorderLayout.CENTER);

        DocDuLieuVaoDatabase();
        bindEvents();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeColor.PRIMARY_DARK.color());
        header.add(lblTitle);
        return header;
    }

    private JComponent buildLeftForm() {
        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        int row = 0;

        txtMaKH = createTextField(false);
        addFormRow(form, gbc, row++, "Mã khách hàng:", txtMaKH);

        txtTenKH = createTextField(true);
        addFormRow(form, gbc, row++, "Họ tên:", txtTenKH);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setOpaque(false);
        radNam = new JRadioButton("Nam", true);
        radNu = new JRadioButton("Nữ");
        ButtonGroup groupGT = new ButtonGroup();
        groupGT.add(radNam);
        groupGT.add(radNu);
        genderPanel.add(radNam);
        genderPanel.add(radNu);
        addFormRow(form, gbc, row++, "Giới tính:", genderPanel);

        txtSDT = createTextField(true);
        addFormRow(form, gbc, row++, "SĐT:", txtSDT);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setDate(new Date());
        addFormRow(form, gbc, row++, "Ngày tạo:", dateChooser);

        txtDiemTichLuy = createTextField(false);
        txtDiemTichLuy.setText("0");
        addFormRow(form, gbc, row++, "Điểm tích lũy:", txtDiemTichLuy);

        main.add(form, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.setOpaque(false);
        btnThem = createPrimaryButton("Thêm", ThemeColor.PRIMARY.color(), ThemeColor.TEXT_LIGHT.color());
        btnSua = createPrimaryButton("Sửa", ThemeColor.WARNING.color(), ThemeColor.TEXT_DARK.color());
        btnLamMoi = createPrimaryButton("Làm mới", ThemeColor.SUCCESS.color(), ThemeColor.TEXT_LIGHT.color());
        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnLamMoi);
        main.add(buttons, BorderLayout.CENTER);

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icon = new ImageIcon("src/main/resources/login_img.png");
        Image scaled = icon.getImage().getScaledInstance(380, 320, Image.SCALE_SMOOTH);
        imgLabel.setIcon(new ImageIcon(scaled));
        imgLabel.setBorder(new EmptyBorder(16, 0, 0, 0));
        main.add(imgLabel, BorderLayout.SOUTH);

        main.setBorder(createTitleBorder("Thông tin khách hàng", ThemeColor.PRIMARY.color(), 20f));
        return main;
    }

    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        return field;
    }

    private JButton createPrimaryButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ThemeColor.TEXT_DARK.color());
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(comp, gbc);
    }

    private JComponent buildRightTable() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setOpaque(false);
        main.setBorder(createTitleBorder("Danh sách khách hàng", ThemeColor.PRIMARY.color(), 20f));

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(ThemeColor.CARD_BG.color());
        searchPanel.setBorder(new CompoundBorder(
                new LineBorder(ThemeColor.PRIMARY.color(), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        JLabel lblTim = new JLabel("Tìm khách bằng số điện thoại:");
        txtTim = new JTextField();
        btnTim = createPrimaryButton("Tìm", ThemeColor.ACCENT.color(), ThemeColor.TEXT_LIGHT.color());
        searchPanel.add(lblTim, BorderLayout.WEST);
        searchPanel.add(txtTim, BorderLayout.CENTER);
        searchPanel.add(btnTim, BorderLayout.EAST);

        String[] header = {"Mã KH", "Tên KH", "Giới tính", "SĐT", "Ngày tạo", "Điểm tích lũy"};
        modelKH = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableKH = new JTable(modelKH);
        tableKH.setRowHeight(32);
        tableKH.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableKH.getTableHeader().setReorderingAllowed(false);
        JScrollPane pane = new JScrollPane(tableKH);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        pagination.setOpaque(false);
        btnPrev = createPrimaryButton("<", ThemeColor.SECONDARY.color(), ThemeColor.TEXT_LIGHT.color());
        btnPrev.setPreferredSize(new Dimension(48, 32));
        btnNext = createPrimaryButton(">", ThemeColor.SECONDARY.color(), ThemeColor.TEXT_LIGHT.color());
        btnNext.setPreferredSize(new Dimension(48, 32));
        lblPageInfo = new JLabel("Trang 1 / 1");
        pagination.add(btnPrev);
        pagination.add(lblPageInfo);
        pagination.add(btnNext);

        main.add(searchPanel, BorderLayout.NORTH);
        main.add(pane, BorderLayout.CENTER);
        main.add(pagination, BorderLayout.SOUTH);
        return main;
    }

    private CompoundBorder createTitleBorder(String title, Color color, float fontSize) {
        LineBorder lineBorder = new LineBorder(new Color(200, 200, 200), 1, true);
        TitledBorder titled = new TitledBorder(lineBorder, title, TitledBorder.LEFT, TitledBorder.TOP);
        titled.setTitleColor(color);
        titled.setTitleFont(new Font("SansSerif", Font.BOLD, (int) fontSize));
        return new CompoundBorder(new EmptyBorder(8, 8, 8, 8),
                new CompoundBorder(titled, new EmptyBorder(10, 12, 12, 12)));
    }

    private void bindEvents() {
        btnThem.addActionListener(this);
        btnSua.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);
        tableKH.addMouseListener(this);
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadDataToTable();
            }
        });
        btnNext.addActionListener(e -> {
            int totalPage = (int) Math.ceil((double) khDao.getTongSoKhachHang() / rowsPerPage);
            if (currentPage < totalPage) {
                currentPage++;
                loadDataToTable();
            }
        });
    }

    private void DocDuLieuVaoDatabase() {
        currentPage = 1;
        listAll = khDao.getAllKhachHang();
        loadDataToTable();
    }

    private void loadDataToTable() {
        modelKH.setRowCount(0);
        List<KhachHang> listPage = khDao.getKhachHangTheoTrang(currentPage, rowsPerPage);
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
        int totalKH = khDao.getTongSoKhachHang();
        int totalPage = Math.max(1, (int) Math.ceil((double) totalKH / rowsPerPage));
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPage);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPage);
    }

    private boolean ValidDate() {
        String tenKH = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim();
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày tạo!");
            return false;
        }
        LocalDate ngayTao = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

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

        if (ngayTao.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày tạo không được lớn hơn ngày hiện tại!");
            return false;
        }
        return true;
    }

    private KhachHang revertKHFromTextfields() {
        String maKH = txtMaKH.getText().trim();
        String tenKH = txtTenKH.getText().trim();
        boolean gioiTinh = radNu.isSelected();
        String sdt = txtSDT.getText().trim();
        LocalDate ngayTaoTK = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int diemTichLuy = 0;
        try {
            diemTichLuy = Integer.parseInt(txtDiemTichLuy.getText());
        } catch (Exception ignored) {}
        return new KhachHang(maKH, tenKH, gioiTinh, sdt, ngayTaoTK, diemTichLuy);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == btnThem) {
            if (!ValidDate()) return;
            String sdt = txtSDT.getText().trim();
            if (khDao.isTrungSoDienThoai(sdt)) {
                JOptionPane.showMessageDialog(this, "Số điện thoại đã tồn tại!");
                txtSDT.requestFocus();
                return;
            }
            String maKH = khDao.phatSinhMaKH();
            txtMaKH.setText(maKH);
            KhachHang kh = revertKHFromTextfields();
            kh.setDiemTichLuy(0);
            if (khDao.themKH(kh)) {
                listAll = khDao.getAllKhachHang();
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
        } else if (o == btnSua) {
            if (!ValidDate()) return;
            if (txtMaKH.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để sửa!");
                return;
            }
            KhachHang kh = revertKHFromTextfields();
            if (khDao.capNhatKH(kh)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                int row = tableKH.getSelectedRow();
                if (row >= 0) {
                    modelKH.setValueAt(kh.getTenKH(), row, 1);
                    modelKH.setValueAt(kh.isGioiTinh() ? "Nữ" : "Nam", row, 2);
                    modelKH.setValueAt(kh.getDiemTichLuy(), row, 5);
                }
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } else if (o == btnLamMoi) {
            txtMaKH.setText("");
            txtTenKH.setText("");
            radNam.setSelected(true);
            txtSDT.setText("");
            dateChooser.setDate(new Date());
            txtDiemTichLuy.setText("0");
            txtTim.setText("");
            txtTenKH.requestFocus();
            currentPage = 1;
            loadDataToTable();
            tableKH.clearSelection();
        } else if (o == btnTim) {
            String sdt = txtTim.getText().trim();
            if (sdt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!");
                return;
            }
            if (listAll == null) listAll = khDao.getAllKhachHang();
            KhachHang kh = khDao.timKiemKH(sdt);
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

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
