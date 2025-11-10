package com.gui;

import com.dao.DAO_KhuyenMai;
import com.dao.DAO_ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.entity.ChiTietKhuyenMai;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

public class TAB_KhuyenMai extends JPanel implements ActionListener, MouseListener {

    // DAO
    private final DAO_KhuyenMai daoKM = new DAO_KhuyenMai();
    private final DAO_ChiTietKhuyenMai daoCT = new DAO_ChiTietKhuyenMai();

    // ==== KM (trái)
    private JTextField txtKM_Ma, txtKM_Ten, txtKM_DieuKien, txtKM_NgayBD, txtKM_NgayKT, txtKM_Tim;
    private JComboBox<String> cbKM_Loai;
    private JButton btnKM_Them, btnKM_Sua, btnKM_Xoa, btnKM_Moi, btnKM_Tim, btnKM_BoLoc, btnKM_XuatCSV;
    private JTable tblKM; private DefaultTableModel mdlKM;

    // ==== CT (phải)
    private JTextField txtCT_MaSP, txtCT_MaKM, txtCT_TiLe, txtCT_Ngay;
    private JButton btnCT_Them, btnCT_Sua, btnCT_Xoa, btnCT_Moi;
    private JTable tblCT; private DefaultTableModel mdlCT;

    // ==== Validate
    private static final Pattern RX_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    public TAB_KhuyenMai() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        bind();
        loadLoaiKM();
        loadTableKM();
    }

    // ================= UI =================
    private JComponent buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(Color.WHITE);
        JLabel title = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 90, 200));
        p.add(title);
        return p;
    }

    private JComponent buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.58);
        split.setLeftComponent(buildLeftKM());
        split.setRightComponent(buildRightCT());
        return split;
    }

    private JComponent buildLeftKM() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                "CHƯƠNG TRÌNH KHUYẾN MÃI",
                TitledBorder.LEFT, TitledBorder.TOP
        ));

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtKM_Ma = new JTextField(); txtKM_Ma.setEditable(false);
        txtKM_Ten = new JTextField();
        cbKM_Loai = new JComboBox<>();
        txtKM_DieuKien = new JTextField();
        txtKM_NgayBD = new JTextField();
        txtKM_NgayKT = new JTextField();

        addField(form, g, 0, "Mã KM:", txtKM_Ma);
        addField(form, g, 1, "Tên CT:", txtKM_Ten);
        addField(form, g, 2, "Loại CT:", cbKM_Loai);
        addField(form, g, 3, "Điều kiện:", txtKM_DieuKien);
        addField(form, g, 4, "Ngày bắt đầu (YYYY-MM-DD):", txtKM_NgayBD);
        addField(form, g, 5, "Ngày kết thúc (YYYY-MM-DD):", txtKM_NgayKT);

        // tools
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        tools.setBackground(Color.WHITE);
        txtKM_Tim  = new JTextField(18);
        btnKM_Tim  = mkBtn("Tìm", new Color(78, 21, 114));
        btnKM_BoLoc= mkBtn("Bỏ lọc", new Color(120,120,120));

        btnKM_XuatCSV= mkBtn("Xuất CSV", new Color(0,150,136));
        tools.add(new JLabel("Tìm:")); tools.add(txtKM_Tim);
        tools.add(btnKM_Tim); tools.add(btnKM_BoLoc); tools.add(btnKM_XuatCSV);

        // actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actions.setBackground(Color.WHITE);
        btnKM_Them = mkBtn("Thêm", new Color(27,160,79));
        btnKM_Sua  = mkBtn("Sửa",  new Color(0,120,215));
        btnKM_Xoa  = mkBtn("Xóa",  new Color(200,50,50));
        btnKM_Moi  = mkBtn("Mới",  new Color(255,140,0));
        actions.add(btnKM_Them); actions.add(btnKM_Sua); actions.add(btnKM_Xoa); actions.add(btnKM_Moi);

        JPanel north = new JPanel(new BorderLayout(8,8));
        north.setBackground(Color.WHITE);
        north.add(form, BorderLayout.NORTH);
        north.add(tools, BorderLayout.CENTER);
        north.add(actions, BorderLayout.SOUTH);

        // table
        String[] colsKM = {"Mã KM","Tên CT","Loại CT","Điều kiện","Ngày BĐ","Ngày KT"};
        mdlKM = new DefaultTableModel(colsKM, 0) { public boolean isCellEditable(int r,int c){return false;} };
        tblKM = new JTable(mdlKM); tblKM.setRowHeight(26); tblKM.addMouseListener(this);

        root.add(north, BorderLayout.NORTH);
        root.add(new JScrollPane(tblKM), BorderLayout.CENTER);
        return root;
    }

    private JComponent buildRightCT() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                "CHI TIẾT ÁP DỤNG",
                TitledBorder.LEFT, TitledBorder.TOP
        ));

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,10,6,10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtCT_MaSP = new JTextField();
        txtCT_MaKM = new JTextField(); // sẽ tự fill theo KM đang chọn
        txtCT_TiLe = new JTextField();
        txtCT_Ngay = new JTextField();

        addField(form, g, 0, "Mã SP:", txtCT_MaSP);
        addField(form, g, 1, "Mã KM:", txtCT_MaKM);
        addField(form, g, 2, "Tỉ lệ (% 0-100):", txtCT_TiLe);
        addField(form, g, 3, "Ngày mua (YYYY-MM-DD):", txtCT_Ngay);

        // actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actions.setBackground(Color.WHITE);
        btnCT_Them = mkBtn("Thêm", new Color(27,160,79));
        btnCT_Sua  = mkBtn("Sửa",  new Color(0,120,215));
        btnCT_Xoa  = mkBtn("Xóa",  new Color(200,50,50));
        btnCT_Moi  = mkBtn("Mới",  new Color(255,140,0));
        actions.add(btnCT_Them); actions.add(btnCT_Sua); actions.add(btnCT_Xoa); actions.add(btnCT_Moi);

        String[] colsCT = {"Mã SP","Mã KM","Tỉ lệ","Ngày mua"};
        mdlCT = new DefaultTableModel(colsCT, 0) { public boolean isCellEditable(int r,int c){return false;} };
        tblCT = new JTable(mdlCT); tblCT.setRowHeight(26); tblCT.addMouseListener(this);

        JPanel north = new JPanel(new BorderLayout(8,8));
        north.setBackground(Color.WHITE);
        north.add(form, BorderLayout.NORTH);
        north.add(actions, BorderLayout.CENTER);

        root.add(north, BorderLayout.NORTH);
        root.add(new JScrollPane(tblCT), BorderLayout.CENTER);
        return root;
    }

    private void bind() {
        btnKM_Them.addActionListener(this);
        btnKM_Sua.addActionListener(this);
        btnKM_Xoa.addActionListener(this);
        btnKM_Moi.addActionListener(this);
        btnKM_Tim.addActionListener(this);
        btnKM_BoLoc.addActionListener(this);

        btnKM_XuatCSV.addActionListener(this);
        txtKM_Tim.addActionListener(this);

        btnCT_Them.addActionListener(this);
        btnCT_Sua.addActionListener(this);
        btnCT_Xoa.addActionListener(this);
        btnCT_Moi.addActionListener(this);
    }

    // ================== LOAD & REFRESH ==================
    private void loadLoaiKM() {
        try {
            cbKM_Loai.removeAllItems();
            List<String> ls = daoKM.findAllLoaiKM();
            if (ls != null) for (String s : ls) cbKM_Loai.addItem(s);
            if (cbKM_Loai.getItemCount()==0) {
                // fallback nếu bảng LoaiKM rỗng
                cbKM_Loai.addItem("GiamGiaPhanTramSP");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải LoạiKM: " + ex.getMessage());
        }
    }

    private void loadTableKM() {
        try {
            mdlKM.setRowCount(0);
            for (KhuyenMai k : daoKM.findAll()) {
                mdlKM.addRow(new Object[]{
                        k.getMaKM(), k.getTenKM(), k.getLoaiKM(), k.getMoTaKM(),
                        k.getNgayBatDau(), k.getNgayKetThuc()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải KM: " + ex.getMessage());
        }
    }

    private void loadTableKMFilter(String kw) {
        try {
            mdlKM.setRowCount(0);
            for (KhuyenMai k : daoKM.search(kw)) {
                mdlKM.addRow(new Object[]{
                        k.getMaKM(), k.getTenKM(), k.getLoaiKM(), k.getMoTaKM(),
                        k.getNgayBatDau(), k.getNgayKetThuc()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm KM: " + ex.getMessage());
        }
    }

    private void loadTableCT(int maKM) {
        try {
            mdlCT.setRowCount(0);
            for (ChiTietKhuyenMai ct : daoCT.findByMaKM(maKM)) {
                mdlCT.addRow(new Object[]{ ct.getMaSP(), ct.getMaKM(), ct.getTiLe(), ct.getNgayApDung() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải CT: " + ex.getMessage());
        }
    }

    // ================== VALIDATE ==================
    private boolean validKM(boolean forUpdate) {
        if (forUpdate && txtKM_Ma.getText().trim().isEmpty()) { msg("Chưa chọn bản ghi"); return false; }
        if (txtKM_Ten.getText().trim().isEmpty())           { msg("Tên không được rỗng"); return false; }
        if (cbKM_Loai.getSelectedItem()==null)              { msg("Chưa chọn loại KM"); return false; }

        String bd = txtKM_NgayBD.getText().trim();
        String kt = txtKM_NgayKT.getText().trim();
        if (!RX_DATE.matcher(bd).matches() || !RX_DATE.matcher(kt).matches()) { msg("Ngày phải YYYY-MM-DD"); return false; }

        try {
            LocalDate d1 = LocalDate.parse(bd, ISO_DATE);
            LocalDate d2 = LocalDate.parse(kt, ISO_DATE);
            if (d1.isAfter(d2)) { msg("Ngày bắt đầu phải ≤ ngày kết thúc"); return false; }
        } catch (Exception ex) { msg("Ngày không hợp lệ"); return false; }
        return true;
    }

    private boolean validCT(boolean forUpdate) {
        int maKM = getSelectedKMId();
        if (maKM == -1) { msg("Chưa chọn khuyến mãi"); return false; }

        String maSP = txtCT_MaSP.getText().trim();
        String tiLeStr = txtCT_TiLe.getText().trim();
        String ngay = txtCT_Ngay.getText().trim();

        if (maSP.isEmpty()) { msg("Mã SP không rỗng"); return false; }
        double tl;
        try { tl = Double.parseDouble(tiLeStr); } catch (Exception e) { msg("Tỉ lệ phải là số"); return false; }
        if (tl < 0 || tl > 100) { msg("Tỉ lệ % trong khoảng 0–100"); return false; }
        if (!RX_DATE.matcher(ngay).matches()) { msg("Ngày mua phải YYYY-MM-DD"); return false; }
        try { LocalDate.parse(ngay, ISO_DATE); } catch (Exception e) { msg("Ngày mua không hợp lệ"); return false; }

        // gợi ý: ensure CT nằm trong thời gian hiệu lực KM (nếu muốn)
        return true;
    }

    // ================== COLLECT ==================
    private KhuyenMai collectKMForInsert() {
        return new KhuyenMai(
                txtKM_Ten.getText().trim(),
                txtKM_DieuKien.getText().trim(),        // map -> moTaKM
                Date.valueOf(txtKM_NgayBD.getText().trim()),
                Date.valueOf(txtKM_NgayKT.getText().trim()),
                String.valueOf(cbKM_Loai.getSelectedItem())
        );
    }

    private KhuyenMai collectKMForUpdate() {
        return new KhuyenMai(
                Integer.parseInt(txtKM_Ma.getText().trim()),
                txtKM_Ten.getText().trim(),
                txtKM_DieuKien.getText().trim(),        // map -> moTaKM
                Date.valueOf(txtKM_NgayBD.getText().trim()),
                Date.valueOf(txtKM_NgayKT.getText().trim()),
                String.valueOf(cbKM_Loai.getSelectedItem())
        );
    }

    // ================== EVENTS ==================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        // KM
        if (s == btnKM_Them) {
            if (!validKM(false)) return;
            try {
                int id = daoKM.insert(collectKMForInsert());
                msg("Đã thêm KM, mã = " + id);
                loadTableKM();
                selectKMRowById(id);
            } catch (Exception ex) { msg("Lỗi thêm KM: " + ex.getMessage()); }
            return;
        }

        if (s == btnKM_Sua) {
            if (!validKM(true)) return;
            try {
                daoKM.update(collectKMForUpdate());
                msg("Đã cập nhật KM");
                loadTableKM();
                selectKMRowById(Integer.parseInt(txtKM_Ma.getText().trim()));
            } catch (Exception ex) { msg("Lỗi cập nhật KM: " + ex.getMessage()); }
            return;
        }

        if (s == btnKM_Xoa) {
            int r = tblKM.getSelectedRow();
            if (r == -1) { msg("Chọn dòng để xóa"); return; }
            int id = Integer.parseInt(String.valueOf(tblKM.getValueAt(r,0)));
            if (JOptionPane.showConfirmDialog(this,"Xóa KM "+id+"? (sẽ xóa luôn chi tiết)","Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                try {
                    daoCT.deleteAllOf(id);
                    daoKM.delete(id);
                    loadTableKM();
                    clearKMForm();
                } catch (Exception ex) { msg("Lỗi xóa KM: " + ex.getMessage()); }
            }
            return;
        }

        if (s == btnKM_Moi)   { clearKMForm(); return; }
        if (s == btnKM_Tim)   { loadTableKMFilter(txtKM_Tim.getText().trim()); return; }
        if (s == txtKM_Tim)   { loadTableKMFilter(txtKM_Tim.getText().trim()); return; }
        if (s == btnKM_BoLoc) { txtKM_Tim.setText(""); loadTableKM(); return; }

        if (s == btnKM_XuatCSV){ exportKMToCSV(); return; }

        // CT
        if (s == btnCT_Moi) { clearCTForm(); return; }

        if (s == btnCT_Them) {
            if (!validCT(false)) return;
            try {
                int km = getSelectedKMId();
                ChiTietKhuyenMai ct = new ChiTietKhuyenMai(
                        km,
                        txtCT_MaSP.getText().trim(),
                        Double.parseDouble(txtCT_TiLe.getText().trim()),
                        Date.valueOf(txtCT_Ngay.getText().trim())
                );
                daoCT.insert(ct);
                loadTableCT(km);
            } catch (Exception ex) { msg("Lỗi thêm CT: " + ex.getMessage()); }
            return;
        }

        if (s == btnCT_Sua) {
            if (!validCT(true)) return;
            try {
                int km = getSelectedKMId();
                ChiTietKhuyenMai ct = new ChiTietKhuyenMai(
                        km,
                        txtCT_MaSP.getText().trim(),
                        Double.parseDouble(txtCT_TiLe.getText().trim()),
                        Date.valueOf(txtCT_Ngay.getText().trim())
                );
                daoCT.update(ct);
                loadTableCT(km);
            } catch (Exception ex) { msg("Lỗi lưu CT: " + ex.getMessage()); }
            return;
        }

        if (s == btnCT_Xoa) {
            int r = tblCT.getSelectedRow();
            if (r == -1) { msg("Chọn dòng CT để xóa"); return; }
            int km = Integer.parseInt(String.valueOf(tblCT.getValueAt(r,1)));
            String maSP = String.valueOf(tblCT.getValueAt(r,0));
            if (JOptionPane.showConfirmDialog(this,"Xóa CT SP "+maSP+"?","Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                try {
                    daoCT.delete(km, maSP);
                    loadTableCT(km);
                    clearCTForm();
                } catch (Exception ex) { msg("Lỗi xóa CT: " + ex.getMessage()); }
            }
        }
    }

    // ================== TABLE/FORM HELPERS ==================
    private void selectKMRowById(int id) {
        for (int i=0;i<mdlKM.getRowCount();i++) {
            if (Integer.parseInt(String.valueOf(mdlKM.getValueAt(i,0))) == id) {
                tblKM.setRowSelectionInterval(i,i);
                tblKM.scrollRectToVisible(tblKM.getCellRect(i,0,true));
                fillKMFormFromRow(i);
                loadTableCT(id);
                break;
            }
        }
    }

    private int getSelectedKMId() {
        int r = tblKM.getSelectedRow();
        if (r < 0) return -1;
        return Integer.parseInt(String.valueOf(tblKM.getValueAt(r,0)));
    }

    private void fillKMFormFromRow(int r) {
        txtKM_Ma.setText(String.valueOf(tblKM.getValueAt(r,0)));
        txtKM_Ten.setText(String.valueOf(tblKM.getValueAt(r,1)));
        cbKM_Loai.setSelectedItem(String.valueOf(tblKM.getValueAt(r,2)));
        txtKM_DieuKien.setText(String.valueOf(tblKM.getValueAt(r,3)));
        txtKM_NgayBD.setText(String.valueOf(tblKM.getValueAt(r,4)));
        txtKM_NgayKT.setText(String.valueOf(tblKM.getValueAt(r,5)));
        // tự fill Mã KM cho form CT
        txtCT_MaKM.setText(txtKM_Ma.getText().trim());
    }

    private void clearKMForm() {
        txtKM_Ma.setText("");
        txtKM_Ten.setText("");
        txtKM_DieuKien.setText("");
        txtKM_NgayBD.setText("");
        txtKM_NgayKT.setText("");
        if (cbKM_Loai.getItemCount()>0) cbKM_Loai.setSelectedIndex(0);
        tblKM.clearSelection();
        txtKM_Ten.requestFocus();
        // clear CT
        mdlCT.setRowCount(0);
        clearCTForm();
    }

    private void clearCTForm() {
        txtCT_MaSP.setText("");
        txtCT_MaKM.setText(txtKM_Ma.getText().trim());
        txtCT_TiLe.setText("");
        txtCT_Ngay.setText("");
        tblCT.clearSelection();
    }

    // ================== Mouse ==================
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == tblKM) {
            int r = tblKM.getSelectedRow();
            if (r >= 0) {
                fillKMFormFromRow(r);
                loadTableCT(Integer.parseInt(String.valueOf(tblKM.getValueAt(r,0))));
            }
        }
        if (e.getSource() == tblCT) {
            int r = tblCT.getSelectedRow();
            if (r >= 0) {
                txtCT_MaSP.setText(String.valueOf(tblCT.getValueAt(r,0)));
                txtCT_MaKM.setText(String.valueOf(tblCT.getValueAt(r,1)));
                txtCT_TiLe.setText(String.valueOf(tblCT.getValueAt(r,2)));
                txtCT_Ngay.setText(String.valueOf(tblCT.getValueAt(r,3)));
            }
        }
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // ================== Utils ==================
    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridy = row;
        g.gridx = 0; g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        p.add(comp, g);
    }

    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(110, 34));
        return b;
    }

    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }

    private void seedKM() {
        try {
            // tạo nhanh vài mẫu
            if (daoKM.findAll().isEmpty()) {
                daoKM.insert(new KhuyenMai("Giảm 10% Noel", "ĐH > 200k",
                        Date.valueOf("2025-12-01"), Date.valueOf("2025-12-31"), "GiamGiaPhanTramSP"));
                daoKM.insert(new KhuyenMai("Giảm 30k HĐ", "ĐH > 300k",
                        Date.valueOf("2025-11-01"), Date.valueOf("2025-11-30"), "GiamGiaTienSP"));
            }
            msg("Đã tạo mẫu!");
        } catch (Exception e) {
            msg("Tạo mẫu lỗi: " + e.getMessage());
        }
    }

    private void exportKMToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu danh sách khuyến mãi (CSV)");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = Files.newBufferedWriter(
                    Path.of(fc.getSelectedFile().getAbsolutePath()),
                    StandardCharsets.UTF_8)) {
                bw.write("MaKM,TenCT,LoaiCT,DieuKien,NgayBD,NgayKT\n");
                for (int i=0;i<mdlKM.getRowCount();i++) {
                    bw.write(csv(mdlKM.getValueAt(i,0)) + "," +
                            csv(mdlKM.getValueAt(i,1)) + "," +
                            csv(mdlKM.getValueAt(i,2)) + "," +
                            csv(mdlKM.getValueAt(i,3)) + "," +
                            csv(mdlKM.getValueAt(i,4)) + "," +
                            csv(mdlKM.getValueAt(i,5)) + "\n");
                }
                msg("Đã xuất CSV thành công!");
            } catch (Exception ex) {
                msg("Lỗi xuất CSV: " + ex.getMessage());
            }
        }
    }
    private String csv(Object v) {
        String s = v==null ? "" : String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"","\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    // Test nhanh
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Khuyến mãi");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 700);
            f.setLocationRelativeTo(null);
            f.add(new TAB_KhuyenMai());
            f.setVisible(true);
        });
    }
}
