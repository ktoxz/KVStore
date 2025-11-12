package com.gui;

import com.dao.DAO_KhuyenMai;
import com.dao.DAO_CT_KhuyenMai;
import com.entity.KhuyenMai;
import com.entity.SanPham; // Cần import SanPham
import com.entity.CT_KhuyenMai;
import com.enums.LoaiKM;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer; // Thêm
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
import javax.swing.border.Border; // Thêm

// ĐÃ XÓA: import Timer, ComponentAdapter, ComponentEvent


public class TAB_KhuyenMai extends JPanel implements ActionListener, MouseListener {

    // DAO
    private final DAO_KhuyenMai daoKM = new DAO_KhuyenMai();
    private final DAO_CT_KhuyenMai daoCT = new DAO_CT_KhuyenMai();

    // ==== Chuẩn hóa Màu sắc & Style ====
    private static final Color CLR_BG = Color.WHITE;
    private static final Color CLR_HEADER = new Color(0, 90, 200);
    private static final Color CLR_SUCCESS = new Color(27, 160, 79);
    private static final Color CLR_PRIMARY = new Color(0, 120, 215);
    private static final Color CLR_DANGER = new Color(200, 50, 50);
    private static final Color CLR_WARNING = new Color(255, 140, 0);
    private static final Color CLR_INFO_DARK = new Color(78, 21, 114);
    private static final Color CLR_MUTED = new Color(120, 120, 120);
    private static final Color CLR_BORDER = new Color(220, 220, 220);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Dimension DIM_BUTTON = new Dimension(110, 34);
    private static final Dimension DIM_TOOLS_PANEL = new Dimension(10, 44);

    // Style cho Bảng
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color CLR_TABLE_GRID = new Color(210, 210, 210); 
    private static final Color CLR_TABLE_HEADER_BG = new Color(240, 240, 240); 
    private static final Border CELL_PADDING = new EmptyBorder(5, 8, 5, 8); 
    private static final int TABLE_ROW_HEIGHT = 30; 


    // ==== KM (trái) ====
    private JTextField txtKM_Ma, txtKM_Ten, txtKM_MoTa, txtKM_NgayBD, txtKM_NgayKT, txtKM_Tim;
    private JButton btnKM_Them, btnKM_Sua, btnKM_Xoa, btnKM_Moi, btnKM_Tim, btnKM_BoLoc, btnKM_XuatCSV;
    private JTable tblKM;
    private DefaultTableModel mdlKM;
    
    // ĐÃ XÓA: Toàn bộ biến phân trang KM

    // ==== CT (phải) ====
    private JTextField txtCT_MaSP, txtCT_MaKM, txtCT_TiLe;
    private JComboBox<String> cbCT_LoaiKM; 
    private JButton btnCT_Them, btnCT_Sua, btnCT_Xoa, btnCT_Moi;
    private JTable tblCT;
    private DefaultTableModel mdlCT;
    
    // ĐÃ XÓA: Toàn bộ biến phân trang CT

    // ==== Validate ====
    private static final Pattern RX_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    public TAB_KhuyenMai() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(CLR_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        bindEvents();
        loadLoaiKM(); 
        loadTableKM(); // Tải tất cả KM (không phân trang)
    }

    // =================================================================
    // SECTION: TÁCH HÀM XÂY DỰNG GIAO DIỆN (UI BUILDERS)
    // =================================================================

    private JComponent buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(CLR_BG);
        JLabel title = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        title.setFont(FONT_HEADER);
        title.setForeground(CLR_HEADER);
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

    // --- Cụm Khuyến Mãi (Bên trái) ---

    private JComponent buildLeftKM() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(CLR_BG);
        root.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                "CHƯƠNG TRÌNH KHUYẾN MÃI",
                TitledBorder.LEFT, TitledBorder.TOP
        ));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(CLR_BG);
        topPanel.add(buildFormKM(), BorderLayout.NORTH);
        topPanel.add(buildToolsKM(), BorderLayout.CENTER);

        JScrollPane tableScrollPane = buildTableKM();
        // ĐÃ XÓA: Paging panel và TableWrap
        
        // ĐÃ XÓA: ComponentListener (tự động resize)

        root.add(topPanel, BorderLayout.NORTH);
        root.add(tableScrollPane, BorderLayout.CENTER); // Thêm trực tiếp JScrollPane
        root.add(buildActionsKM(), BorderLayout.SOUTH); 
        return root;
    }

    private JComponent buildFormKM() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CLR_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        txtKM_Ma = new JTextField();
        txtKM_Ma.setEditable(false);
        txtKM_Ten = new JTextField();
        txtKM_MoTa = new JTextField();
        txtKM_NgayBD = new JTextField();
        txtKM_NgayKT = new JTextField();
        addField(form, g, 0, "Mã KM:", txtKM_Ma);
        addField(form, g, 1, "Tên CT:", txtKM_Ten);
        addField(form, g, 3, "Mô tả (Điều kiện):", txtKM_MoTa);
        addField(form, g, 4, "Ngày bắt đầu (YYYY-MM-DD):", txtKM_NgayBD);
        addField(form, g, 5, "Ngày kết thúc (YYYY-MM-DD):", txtKM_NgayKT);
        return form;
    }

    private JComponent buildToolsKM() {
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        tools.setBackground(CLR_BG);
        tools.setPreferredSize(DIM_TOOLS_PANEL); 
        txtKM_Tim = new JTextField(18);
        btnKM_Tim = mkBtn("Tìm", CLR_INFO_DARK);
        btnKM_BoLoc = mkBtn("Bỏ lọc", CLR_MUTED);
        btnKM_XuatCSV = mkBtn("Xuất CSV", new Color(0, 150, 136));
        tools.add(new JLabel("Tìm:"));
        tools.add(txtKM_Tim);
        tools.add(btnKM_Tim);
        tools.add(btnKM_BoLoc);
        tools.add(btnKM_XuatCSV);
        return tools;
    }

    private JComponent buildActionsKM() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actions.setBackground(CLR_BG);
        btnKM_Them = mkBtn("Thêm", CLR_SUCCESS);
        btnKM_Sua = mkBtn("Sửa", CLR_PRIMARY);
        btnKM_Xoa = mkBtn("Xóa", CLR_DANGER);
        btnKM_Moi = mkBtn("Mới", CLR_WARNING);
        actions.add(btnKM_Them);
        actions.add(btnKM_Sua);
        actions.add(btnKM_Xoa);
        actions.add(btnKM_Moi);
        return actions;
    }

    private JScrollPane buildTableKM() {
        String[] colsKM = {"Mã KM", "Tên CT", "Mô tả", "Ngày BĐ", "Ngày KT"};
        mdlKM = new DefaultTableModel(colsKM, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblKM = new JTable(mdlKM);
        
        tblKM.setRowHeight(TABLE_ROW_HEIGHT); 
        tblKM.setFont(FONT_TABLE_CELL); 
        tblKM.setGridColor(CLR_TABLE_GRID); 
        tblKM.getTableHeader().setOpaque(false);
        tblKM.getTableHeader().setBackground(CLR_TABLE_HEADER_BG);
        tblKM.getTableHeader().setFont(FONT_TABLE_HEADER);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tblKM.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBorder(new EmptyBorder(5, 5, 5, 5)); 

        DefaultTableCellRenderer cellRendererLeft = new DefaultTableCellRenderer();
        cellRendererLeft.setHorizontalAlignment(SwingConstants.LEFT);
        cellRendererLeft.setBorder(CELL_PADDING);
        
        DefaultTableCellRenderer cellRendererCenter = new DefaultTableCellRenderer();
        cellRendererCenter.setHorizontalAlignment(SwingConstants.CENTER);
        cellRendererCenter.setBorder(CELL_PADDING);
        
        tblKM.getColumnModel().getColumn(0).setCellRenderer(cellRendererCenter); 
        tblKM.getColumnModel().getColumn(1).setCellRenderer(cellRendererLeft);  
        tblKM.getColumnModel().getColumn(2).setCellRenderer(cellRendererLeft);  
        tblKM.getColumnModel().getColumn(3).setCellRenderer(cellRendererCenter); 
        tblKM.getColumnModel().getColumn(4).setCellRenderer(cellRendererCenter); 
        
        tblKM.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblKM.getColumnModel().getColumn(0).setMaxWidth(80);
        tblKM.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblKM.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblKM.getColumnModel().getColumn(4).setPreferredWidth(100);

        tblKM.addMouseListener(this);
        return new JScrollPane(tblKM);
    }
    
    // ĐÃ XÓA: buildPagingKM()

    // --- Cụm Chi Tiết (Bên phải) ---

    private JComponent buildRightCT() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(CLR_BG);
        root.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                "CHI TIẾT ÁP DỤNG",
                TitledBorder.LEFT, TitledBorder.TOP
        ));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(CLR_BG);
        topPanel.add(buildFormCT(), BorderLayout.NORTH);
        
        JPanel fakeToolsPanel = new JPanel();
        fakeToolsPanel.setBackground(CLR_BG);
        fakeToolsPanel.setPreferredSize(DIM_TOOLS_PANEL); 
        topPanel.add(fakeToolsPanel, BorderLayout.CENTER);

        JScrollPane tableScrollPane = buildTableCT();
        
        // ĐÃ XÓA: Paging panel và TableWrap
        // ĐÃ XÓA: ComponentListener

        root.add(topPanel, BorderLayout.NORTH); 
        root.add(tableScrollPane, BorderLayout.CENTER); // Thêm trực tiếp JScrollPane
        root.add(buildActionsCT(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildFormCT() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CLR_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        txtCT_MaSP = new JTextField();
        txtCT_MaKM = new JTextField();
        txtCT_MaKM.setEditable(false); 
        txtCT_TiLe = new JTextField();
        cbCT_LoaiKM = new JComboBox<>();
        addField(form, g, 0, "Mã SP:", txtCT_MaSP);
        addField(form, g, 1, "Mã KM:", txtCT_MaKM);
        addField(form, g, 2, "Loại chi tiết:", cbCT_LoaiKM);
        addField(form, g, 3, "Tỉ lệ (Giá trị/Phần trăm):", txtCT_TiLe);
        return form;
    }

    private JComponent buildActionsCT() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actions.setBackground(CLR_BG);
        btnCT_Them = mkBtn("Thêm", CLR_SUCCESS);
        btnCT_Sua = mkBtn("Sửa", CLR_PRIMARY);
        btnCT_Xoa = mkBtn("Xóa", CLR_DANGER);
        btnCT_Moi = mkBtn("Mới", CLR_WARNING);
        actions.add(btnCT_Them);
        actions.add(btnCT_Sua);
        actions.add(btnCT_Xoa);
        actions.add(btnCT_Moi);
        return actions;
    }

    private JScrollPane buildTableCT() {
        String[] colsCT = {"Mã SP", "Mã KM", "Loại CT", "Tỉ lệ"};
        mdlCT = new DefaultTableModel(colsCT, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblCT = new JTable(mdlCT);
        
        tblCT.setRowHeight(TABLE_ROW_HEIGHT); 
        tblCT.setFont(FONT_TABLE_CELL); 
        tblCT.setGridColor(CLR_TABLE_GRID); 
        tblCT.getTableHeader().setOpaque(false);
        tblCT.getTableHeader().setBackground(CLR_TABLE_HEADER_BG);
        tblCT.getTableHeader().setFont(FONT_TABLE_HEADER);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tblCT.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        DefaultTableCellRenderer cellRendererLeft = new DefaultTableCellRenderer();
        cellRendererLeft.setHorizontalAlignment(SwingConstants.LEFT);
        cellRendererLeft.setBorder(CELL_PADDING);
        
        DefaultTableCellRenderer cellRendererCenter = new DefaultTableCellRenderer();
        cellRendererCenter.setHorizontalAlignment(SwingConstants.CENTER);
        cellRendererCenter.setBorder(CELL_PADDING);
        
        DefaultTableCellRenderer cellRendererRight = new DefaultTableCellRenderer();
        cellRendererRight.setHorizontalAlignment(SwingConstants.RIGHT);
        cellRendererRight.setBorder(CELL_PADDING);
        
        tblCT.getColumnModel().getColumn(0).setCellRenderer(cellRendererCenter); 
        tblCT.getColumnModel().getColumn(1).setCellRenderer(cellRendererCenter); 
        tblCT.getColumnModel().getColumn(2).setCellRenderer(cellRendererLeft);  
        tblCT.getColumnModel().getColumn(3).setCellRenderer(cellRendererRight); 
        
        tblCT.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblCT.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblCT.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblCT.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        tblCT.addMouseListener(this);
        return new JScrollPane(tblCT);
    }
    
    // ĐÃ XÓA: buildPagingCT()

    // =================================================================
    // SECTION: GÁN SỰ KIỆN (EVENT BINDING)
    // =================================================================

    private void bindEvents() {
        btnKM_Them.addActionListener(this);
        btnKM_Sua.addActionListener(this);
        btnKM_Xoa.addActionListener(this);
        btnKM_Moi.addActionListener(this);
        btnKM_Tim.addActionListener(this);
        btnKM_BoLoc.addActionListener(this);
        btnKM_XuatCSV.addActionListener(this);
        txtKM_Tim.addActionListener(this);
        tblKM.addMouseListener(this);
        btnCT_Them.addActionListener(this);
        btnCT_Sua.addActionListener(this);
        btnCT_Xoa.addActionListener(this);
        btnCT_Moi.addActionListener(this);
        tblCT.addMouseListener(this);
    }

    // =================================================================
    // SECTION: TẢI DỮ LIỆU & LÀM MỚI (LOAD & REFRESH)
    // =================================================================

    private void loadLoaiKM() {
        try {
            List<String> ls = daoKM.findAllLoaiKM();
            cbCT_LoaiKM.removeAllItems();
            if (ls != null && !ls.isEmpty()) {
                for (String s : ls) {
                    cbCT_LoaiKM.addItem(s);
                }
            } else {
                cbCT_LoaiKM.addItem("GiamGiaPhanTramSP");
                cbCT_LoaiKM.addItem("GiamGiaTienSP");
            }
        } catch (Exception ex) {
            msg("Lỗi tải LoạiKM: " + ex.getMessage());
        }
    }

    // --- Load/Refresh cho KM ---
    private void populateTableKM(List<KhuyenMai> ds) {
        mdlKM.setRowCount(0);
        for (KhuyenMai k : ds) {
            mdlKM.addRow(new Object[]{
                    k.getMaKM(), k.getTenKM(), k.getMoTaKM(),
                    k.getNgayBatDau(), k.getNgayKetThuc()
            });
        }
    }
    
    private void loadTableKM() {
        try {
            populateTableKM(daoKM.findAll());
        } catch (Exception ex) {
            msg("Lỗi tải KM: " + ex.getMessage());
            ex.printStackTrace(); // In lỗi ra console để debug
        }
    }
    
    // ĐÃ XÓA: rebuildPagingKM() và pageBtn()
    
    // --- Load/Refresh cho CT ---
    private void populateTableCT(List<CT_KhuyenMai> ds) {
        mdlCT.setRowCount(0);
        for (CT_KhuyenMai ct : ds) {
            mdlCT.addRow(new Object[]{
                    ct.getSanPham().getMaSP(),
                    ct.getKhuyenMai().getMaKM(),
                    ct.getLoaiKM(),          
                    ct.getGiaTri()
            });
        }
    }

    private void loadTableCT(int maKM) {
        if (maKM == -1) {
             mdlCT.setRowCount(0);
             return;
        }
        try {
            // Gọi hàm findByMaKM (không phân trang)
            populateTableCT(daoCT.findByMaKM(maKM)); 
        } catch (Exception ex) {
            msg("Lỗi tải CT: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // ĐÃ XÓA: rebuildPagingCT()

    // =================================================================
    // SECTION: XỬ LÝ SỰ KIỆN (EVENT HANDLERS)
    // =================================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == btnKM_Them) handleThemKM();
        else if (s == btnKM_Sua) handleSuaKM();
        else if (s == btnKM_Xoa) handleXoaKM();
        else if (s == btnKM_Moi) clearKMForm();
        else if (s == btnKM_Tim || s == txtKM_Tim) handleTimKM(); 
        else if (s == btnKM_BoLoc) handleBoLocKM(); 
        else if (s == btnKM_XuatCSV) exportKMToCSV(); 
        else if (s == btnCT_Them) handleThemCT();
        else if (s == btnCT_Sua) handleSuaCT();
        else if (s == btnCT_Xoa) handleXoaCT();
        else if (s == btnCT_Moi) clearCTForm();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == tblKM) handleClickTableKM();
        else if (e.getSource() == tblCT) handleClickTableCT();
    }

    // =================================================================
    // SECTION: LOGIC XỬ LÝ SỰ KIỆN (ACTION LOGIC)
    // =================================================================

    private void handleThemKM() {
        if (!validKM(false)) return;
        try {
            KhuyenMai kmMoi = daoKM.insert(collectKMForInsert());
            if (kmMoi != null) {
                msg("Đã thêm KM, mã = " + kmMoi.getMaKM());
                loadTableKM(); 
                selectKMRowById(kmMoi.getMaKM()); 
            } else {
                msg("Lỗi thêm KM: DAO trả về null");
            }
        } catch (Exception ex) {
            msg("Lỗi thêm KM: " + ex.getMessage());
        }
    }

    private void handleSuaKM() {
        if (!validKM(true)) return;
        try {
            KhuyenMai km = collectKMForUpdate();
            if (daoKM.update(km)) {
                msg("Đã cập nhật KM");
                loadTableKM(); 
                selectKMRowById(km.getMaKM());
            } else {
                msg("Lỗi cập nhật KM");
            }
        } catch (Exception ex) {
            msg("Lỗi cập nhật KM: " + ex.getMessage());
        }
    }

    private void handleXoaKM() {
        int r = tblKM.getSelectedRow();
        if (r == -1) {
            msg("Chọn dòng để xóa");
            return;
        }
        int modelRow = tblKM.convertRowIndexToModel(r);
        int id = Integer.parseInt(String.valueOf(mdlKM.getValueAt(modelRow, 0)));
        
        if (JOptionPane.showConfirmDialog(this, "Xóa KM " + id + "? (sẽ xóa luôn chi tiết)", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (daoKM.delete(id)) {
                    loadTableKM(); 
                    clearKMForm();
                } else {
                    msg("Lỗi xóa KM");
                }
            } catch (Exception ex) {
                msg("Lỗi xóa KM: " + ex.getMessage());
            }
        }
    }

    private void handleTimKM() {
        try {
            populateTableKM(daoKM.search(txtKM_Tim.getText().trim()));
        } catch (Exception ex) {
            msg("Lỗi tìm KM: " + ex.getMessage());
        }
    }

    private void handleBoLocKM() {
        txtKM_Tim.setText("");
        loadTableKM(); 
    }

    private void handleThemCT() {
        if (!validCT(false)) return;
        try {
            int kmID = getSelectedKMId();
            if (kmID == -1) return;
            CT_KhuyenMai ct = collectCT();
            if (daoCT.insert(ct)) {
                loadTableCT(kmID); 
            } else {
                msg("Lỗi thêm CT: Trùng khóa (maKM, maSP, loaiKM)?");
            }
        } catch (Exception ex) {
            msg("Lỗi thêm CT: " + ex.getMessage());
        }
    }

    private void handleSuaCT() {
        int r = tblCT.getSelectedRow();
        if (r == -1) {
            msg("Phải chọn 1 dòng chi tiết để sửa");
            return;
        }
        if (!validCT(true)) return;
        int modelRow = tblCT.convertRowIndexToModel(r);

        try {
            int kmID = getSelectedKMId();
            if (kmID == -1) return;
            CT_KhuyenMai ct = collectCT();
            String spGoc = String.valueOf(mdlCT.getValueAt(modelRow, 0));
            String loaiGoc = String.valueOf(mdlCT.getValueAt(modelRow, 2));

            if (!ct.getSanPham().getMaSP().equals(spGoc) || !ct.getLoaiKM().equals(loaiGoc)) {
                msg("Không thể thay đổi Mã SP hoặc Loại CT khi cập nhật. Hãy Xóa và Thêm mới.");
                return;
            }
            if (daoCT.update(ct)) {
                loadTableCT(kmID); 
            } else {
                msg("Lỗi lưu CT");
            }
        } catch (Exception ex) {
            msg("Lỗi lưu CT: " + ex.getMessage());
        }
    }

    private void handleXoaCT() {
        int r = tblCT.getSelectedRow();
        if (r == -1) {
            msg("Chọn dòng CT để xóa");
            return;
        }
        int modelRow = tblCT.convertRowIndexToModel(r);
        String maSP = String.valueOf(mdlCT.getValueAt(modelRow, 0));
        int kmID = Integer.parseInt(String.valueOf(mdlCT.getValueAt(modelRow, 1)));
        String loaiKM = String.valueOf(mdlCT.getValueAt(modelRow, 2));

        if (JOptionPane.showConfirmDialog(this, "Xóa CT SP " + maSP + " (" + loaiKM + ") ?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (daoCT.delete(kmID, maSP, loaiKM)) {
                    loadTableCT(kmID); 
                    clearCTForm();
                } else {
                    msg("Lỗi xóa CT: Không tìm thấy");
                }
            } catch (Exception ex) {
                msg("Lỗi xóa CT: " + ex.getMessage());
            }
        }
    }

    private void handleClickTableKM() {
        int r = tblKM.getSelectedRow();
        if (r >= 0) {
            int modelRow = tblKM.convertRowIndexToModel(r);
            fillKMFormFromRow(modelRow);
            loadTableCT(Integer.parseInt(String.valueOf(mdlKM.getValueAt(modelRow, 0))));
        }
    }

    private void handleClickTableCT() {
        int r = tblCT.getSelectedRow();
        if (r >= 0) {
            int modelRow = tblCT.convertRowIndexToModel(r);
            txtCT_MaSP.setText(String.valueOf(mdlCT.getValueAt(modelRow, 0)));
            txtCT_MaKM.setText(String.valueOf(mdlCT.getValueAt(modelRow, 1)));
            cbCT_LoaiKM.setSelectedItem(String.valueOf(mdlCT.getValueAt(modelRow, 2)));
            txtCT_TiLe.setText(String.valueOf(mdlCT.getValueAt(modelRow, 3)));
        }
    }

    // =================================================================
    // SECTION: VALIDATE & COLLECT (KIỂM TRA & THU THẬP DỮ LIỆU)
    // =================================================================

    private boolean validKM(boolean forUpdate) {
        if (forUpdate && txtKM_Ma.getText().trim().isEmpty()) {
            msg("Chưa chọn bản ghi");
            return false;
        }
        if (txtKM_Ten.getText().trim().isEmpty()) {
            msg("Tên không được rỗng");
            return false;
        }
        String bd = txtKM_NgayBD.getText().trim();
        String kt = txtKM_NgayKT.getText().trim();
        if (bd.isEmpty() || kt.isEmpty()) {
            msg("Ngày không được rỗng");
            return false;
        }
        if (!RX_DATE.matcher(bd).matches() || !RX_DATE.matcher(kt).matches()) {
            msg("Ngày phải YYYY-MM-DD");
            return false;
        }
        try {
            LocalDate d1 = LocalDate.parse(bd, ISO_DATE);
            LocalDate d2 = LocalDate.parse(kt, ISO_DATE);
            if (d1.isAfter(d2)) {
                msg("Ngày bắt đầu phải ≤ ngày kết thúc");
                return false;
            }
        } catch (Exception ex) {
            msg("Ngày không hợp lệ");
            return false;
        }
        return true;
    }

    private boolean validCT(boolean forUpdate) {
        int maKM = getSelectedKMId();
        if (maKM == -1) {
            msg("Chưa chọn khuyến mãi (bên trái)");
            return false;
        }
        String maSP = txtCT_MaSP.getText().trim();
        String tiLeStr = txtCT_TiLe.getText().trim();
        if (maSP.isEmpty()) {
            msg("Mã SP không rỗng");
            return false;
        }
        if (cbCT_LoaiKM.getSelectedItem() == null) {
            msg("Chưa chọn Loại chi tiết");
            return false;
        }
        double tl;
        try {
            tl = Double.parseDouble(tiLeStr);
        } catch (Exception e) {
            msg("Tỉ lệ/Giá trị phải là số");
            return false;
        }
        if (tl < 0) {
            msg("Tỉ lệ/Giá trị phải >= 0");
            return false;
        }
        return true;
    }

    private KhuyenMai collectKMForInsert() {
        return new KhuyenMai(
                txtKM_Ten.getText().trim(),
                txtKM_MoTa.getText().trim(),
                Date.valueOf(txtKM_NgayBD.getText().trim()),
                Date.valueOf(txtKM_NgayKT.getText().trim())
        );
    }

    private KhuyenMai collectKMForUpdate() {
        return new KhuyenMai(
                Integer.parseInt(txtKM_Ma.getText().trim()),
                txtKM_Ten.getText().trim(),
                txtKM_MoTa.getText().trim(),
                Date.valueOf(txtKM_NgayBD.getText().trim()),
                Date.valueOf(txtKM_NgayKT.getText().trim())
        );
    }

    /**
     * ✅ ĐÂY LÀ HÀM ĐÃ SỬA LỖI
     * Sửa lỗi "delete/update" của bạn.
     * Lỗi là do SanPham.java không có constructor rỗng 'new SanPham()'.
     * Chúng ta dùng constructor 'new SanPham(maSP, tenSP, giaSP)' mà bạn đã cung cấp.
     */
    private CT_KhuyenMai collectCT() {
        int kmID = Integer.parseInt(txtCT_MaKM.getText().trim());
        String spID = txtCT_MaSP.getText().trim();
        double tiLe = Double.parseDouble(txtCT_TiLe.getText().trim());
        LoaiKM loaiKM = LoaiKM.valueOf(String.valueOf(cbCT_LoaiKM.getSelectedItem()));
        
        KhuyenMai km = new KhuyenMai();
        km.setMaKM(kmID);

        // ✅ SỬA LỖI TẠI ĐÂY
        // SanPham sp = new SanPham(); // <-- Dòng này gây lỗi
        // sp.setMaSP(spID);
        
        // Thay bằng hàm khởi tạo 3 tham số (maSP, tenSP, giaSP)
        // Chúng ta chỉ cần mã, 2 cái kia có thể để rỗng/0
        SanPham sp = new SanPham(spID, "", 0); 
        
        return new CT_KhuyenMai(km, sp, tiLe, loaiKM);
    }

    // =================================================================
    // SECTION: HÀM TIỆN ÍCH (HELPER UTILS)
    // (Giữ nguyên)
    // =================================================================

    private void selectKMRowById(int id) {
        for (int i = 0; i < mdlKM.getRowCount(); i++) {
            if (Integer.parseInt(String.valueOf(mdlKM.getValueAt(i, 0))) == id) {
                tblKM.setRowSelectionInterval(i, i);
                tblKM.scrollRectToVisible(tblKM.getCellRect(i, 0, true));
                fillKMFormFromRow(i);
                loadTableCT(id); 
                break;
            }
        }
    }

    private int getSelectedKMId() {
        int r = tblKM.getSelectedRow();
        if (r < 0) return -1;
        int modelRow = tblKM.convertRowIndexToModel(r);
        return Integer.parseInt(String.valueOf(mdlKM.getValueAt(modelRow, 0)));
    }

    private void fillKMFormFromRow(int r) {
        txtKM_Ma.setText(String.valueOf(mdlKM.getValueAt(r, 0)));
        txtKM_Ten.setText(String.valueOf(mdlKM.getValueAt(r, 1)));
        txtKM_MoTa.setText(String.valueOf(mdlKM.getValueAt(r, 2)));
        txtKM_NgayBD.setText(String.valueOf(mdlKM.getValueAt(r, 3)));
        txtKM_NgayKT.setText(String.valueOf(mdlKM.getValueAt(r, 4)));
        txtCT_MaKM.setText(txtKM_Ma.getText().trim());
    }

    private void clearKMForm() {
        txtKM_Ma.setText("");
        txtKM_Ten.setText("");
        txtKM_MoTa.setText("");
        txtKM_NgayBD.setText("");
        txtKM_NgayKT.setText("");
        tblKM.clearSelection();
        txtKM_Ten.requestFocus();
        mdlCT.setRowCount(0);
        clearCTForm();
        // ĐÃ XÓA: rebuildPagingCT()
    }

    private void clearCTForm() {
        txtCT_MaSP.setText("");
        txtCT_MaKM.setText(txtKM_Ma.getText().trim());
        txtCT_TiLe.setText("");
        if (cbCT_LoaiKM.getItemCount() > 0) cbCT_LoaiKM.setSelectedIndex(0);
        tblCT.clearSelection();
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridy = row; g.gridx = 0; g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        p.add(comp, g);
    }

    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BUTTON);
        b.setPreferredSize(DIM_BUTTON);
        return b;
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void exportKMToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu danh sách khuyến mãi (CSV)");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = Files.newBufferedWriter(
                    Path.of(fc.getSelectedFile().getAbsolutePath()),
                    StandardCharsets.UTF_8)) {
                bw.write("MaKM,TenCT,MoTa,NgayBD,NgayKT\n");
                for (int i = 0; i < mdlKM.getRowCount(); i++) {
                    bw.write(csv(mdlKM.getValueAt(i, 0)) + "," +
                            csv(mdlKM.getValueAt(i, 1)) + "," +
                            csv(mdlKM.getValueAt(i, 2)) + "," +
                            csv(mdlKM.getValueAt(i, 3)) + "," +
                            csv(mdlKM.getValueAt(i, 4)) + "\n");
                }
                msg("Đã xuất CSV (tất cả) thành công!");
            } catch (Exception ex) {
                msg("Lỗi xuất CSV: " + ex.getMessage());
            }
        }
    }

    private String csv(Object v) {
        String s = v == null ? "" : String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

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