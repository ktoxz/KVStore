package com.gui;

import com.dao.DAO_KhuyenMai;
import com.dao.DAO_CT_KhuyenMai;
import com.dao.DAO_SanPham;
import com.entity.CT_KhuyenMai;
import com.entity.KhuyenMai;
import com.entity.SanPham;
import com.enums.LoaiKM;

// (MỚI) Import thư viện Look and Feel
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.border.Border;
import com.toedter.calendar.JDateChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.util.concurrent.ExecutionException;

/**
 * Phiên bản cuối: Tối ưu Layout, chống chớp/giật, Font lớn
 * VÀ quay lại cách tô màu nút đơn giản (setBackground)
 */
public class TAB_KhuyenMai extends JPanel implements ActionListener, MouseListener {

    // DAO
    private final DAO_KhuyenMai daoKM = new DAO_KhuyenMai();
    private final DAO_CT_KhuyenMai daoCT = new DAO_CT_KhuyenMai();
    private final DAO_SanPham daoSP = new DAO_SanPham();

    // ==== Chuẩn hóa Màu sắc & Style ====
    private static final Color CLR_HEADER = new Color(0, 90, 200);
    private static final Color CLR_SUCCESS = new Color(27, 160, 79);
    private static final Color CLR_PRIMARY = new Color(0, 120, 215);
    private static final Color CLR_DANGER = new Color(200, 50, 50);
    private static final Color CLR_WARNING = new Color(255, 140, 0);
    private static final Color CLR_INFO_DARK = new Color(78, 21, 114);
    private static final Color CLR_MUTED = new Color(120, 120, 120);
    private static final Color CLR_BORDER = new Color(220, 220, 220);
    
    // Font và Kích thước đã tối ưu
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private static final Dimension DIM_BUTTON = new Dimension(110, 38);
    private static final Dimension DIM_TOOLS_PANEL = new Dimension(10, 48);

    // Style cho Bảng
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color CLR_TABLE_GRID = new Color(210, 210, 210);
    private static final Color CLR_TABLE_HEADER_BG = new Color(240, 240, 240);
    private static final Border CELL_PADDING = new EmptyBorder(7, 8, 7, 8);
    private static final int TABLE_ROW_HEIGHT = 35;

    // Style cho Live Search
    private static final int SEARCH_THUMB_SIZE = 50;
    private static final String IMG_DIR = "src/main/resources/sp_image";


    // ==== KM (trái) ====
    private JTextField txtKM_Ma, txtKM_Ten, txtKM_MoTa, txtKM_Tim;
    private JDateChooser dcKM_NgayBD, dcKM_NgayKT;
    private JButton btnKM_Them, btnKM_Sua, btnKM_Xoa, btnKM_Moi, btnKM_Tim, btnKM_BoLoc, btnKM_XuatCSV;
    private JTable tblKM;
    private DefaultTableModel mdlKM;

    // ==== CT (phải) ====
    private JTextField txtCT_MaSP, txtCT_MaKM, txtCT_TiLe;
    private JComboBox<String> cbCT_LoaiKM;
    private JButton btnCT_Them, btnCT_Sua, btnCT_Xoa, btnCT_Moi;
    private JTable tblCT;
    private DefaultTableModel mdlCT;
    private JLabel lbCT_TiLe;

    // Các biến cho Live Search (Sản Phẩm)
    private JPopupMenu productSearchPopup;
    private JPanel productListPanel;
    private Timer searchTimer;
    private SwingWorker<List<SanPham>, Void> searchWorker;
    private boolean isUpdatingFromPopup = false;
    
    // Biến cho Live Search (Khuyến Mãi)
    private JPopupMenu kmSearchPopup;
    private JPanel kmListPanel;
    private Timer kmSearchTimer;
    private SwingWorker<List<KhuyenMai>, Void> kmSearchWorker;
    private boolean isUpdatingKmFromPopup = false;
    
    // Biến con trỏ chuột
    private static final Cursor CURSOR_HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor CURSOR_DEFAULT = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor CURSOR_WAIT = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);


    // ==== Validate ====
    private static final Pattern RX_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    public TAB_KhuyenMai() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        bindEvents();
        loadLoaiKM();
        loadTableKM(); 

        updateTiLeLabel();
        setCTControlsEnabled(false); 
    }

    // =================================================================
    // SECTION: TÁCH HÀM XÂY DỰNG GIAO DIỆN (UI BUILDERS)
    // =================================================================

    private JComponent buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false); 
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
        split.setOpaque(false); 
        return split;
    }

    // --- Cụm Khuyến Mãi (Bên trái) ---

    private JComponent buildLeftKM() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createTitledBorder(
                null, 
                "CHƯƠNG TRÌNH KHUYẾN MÃI",
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_TABLE_HEADER 
        ));
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);
        topPanel.add(buildFormKM(), BorderLayout.NORTH);
        topPanel.add(buildToolsKM(), BorderLayout.CENTER);
        JScrollPane tableScrollPane = buildTableKM();
        root.add(topPanel, BorderLayout.NORTH);
        root.add(tableScrollPane, BorderLayout.CENTER);
        root.add(buildActionsKM(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildFormKM() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        
        g.insets = new Insets(8, 10, 8, 10);
        
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        txtKM_Ma = new JTextField();
        txtKM_Ma.setEditable(false);
        txtKM_Ten = new JTextField();
        txtKM_MoTa = new JTextField();

        dcKM_NgayBD = new JDateChooser();
        dcKM_NgayKT = new JDateChooser();
        dcKM_NgayBD.setDateFormatString("yyyy-MM-dd");
        dcKM_NgayKT.setDateFormatString("yyyy-MM-dd");
        
        // Đặt font cho các trường
        txtKM_Ma.setFont(FONT_TABLE_CELL);
        txtKM_Ten.setFont(FONT_TABLE_CELL);
        txtKM_MoTa.setFont(FONT_TABLE_CELL);
        dcKM_NgayBD.setFont(FONT_TABLE_CELL);
        dcKM_NgayKT.setFont(FONT_TABLE_CELL);

        addField(form, g, 0, "Mã KM:", txtKM_Ma);
        addField(form, g, 1, "Tên CT:", txtKM_Ten);
        addField(form, g, 3, "Mô tả ", txtKM_MoTa);
        addField(form, g, 4, "Ngày bắt đầu:", dcKM_NgayBD);
        addField(form, g, 5, "Ngày kết thúc:", dcKM_NgayKT);
        return form;
    }

    private JComponent buildToolsKM() {
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        tools.setOpaque(false);
        tools.setPreferredSize(DIM_TOOLS_PANEL); 
        txtKM_Tim = new JTextField(18);
        txtKM_Tim.setFont(FONT_TABLE_CELL); 
        
        btnKM_Tim = mkBtn("Tìm", CLR_INFO_DARK);
        btnKM_BoLoc = mkBtn("Bỏ lọc", CLR_MUTED);
        btnKM_XuatCSV = mkBtn("Xuất CSV", new Color(0, 150, 136));
        
        JLabel lblTim = new JLabel("Tìm:");
        lblTim.setFont(FONT_TABLE_CELL.deriveFont(Font.BOLD)); // Đặt font
        tools.add(lblTim);
        tools.add(txtKM_Tim);
        tools.add(btnKM_Tim);
        tools.add(btnKM_BoLoc);
       
        
        kmSearchPopup = new JPopupMenu();
        kmSearchPopup.setFocusable(false); 
        kmSearchPopup.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        
        kmListPanel = new JPanel();
        kmListPanel.setLayout(new BoxLayout(kmListPanel, BoxLayout.Y_AXIS));
        
        JScrollPane kmPopupScroll = new JScrollPane(kmListPanel);
        kmPopupScroll.setBorder(null);
        kmPopupScroll.setPreferredSize(new Dimension(300, 250)); 
        kmSearchPopup.add(kmPopupScroll);

        kmSearchTimer = new Timer(300, e -> triggerKmSearchWorker()); 
        kmSearchTimer.setRepeats(false);
        
        return tools;
    }

    private JComponent buildActionsKM() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actions.setOpaque(false);
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
        tblKM.getTableHeader().setFont(FONT_TABLE_HEADER);
        
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

    // --- Cụm Chi Tiết (Bên phải) ---

    private JComponent buildRightCT() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createTitledBorder(
                null,
                "CHI TIẾT ÁP DỤNG",
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_TABLE_HEADER
        ));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        topPanel.add(buildFormCT(), BorderLayout.CENTER);

        JPanel actionsPanelWrapper = new JPanel(new BorderLayout());
        actionsPanelWrapper.setOpaque(false);
        actionsPanelWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
        actionsPanelWrapper.add(buildActionsCT(), BorderLayout.NORTH);
        
        topPanel.add(actionsPanelWrapper, BorderLayout.EAST);
        
        JScrollPane tableScrollPane = buildTableCT();
        
        root.add(topPanel, BorderLayout.NORTH);
        root.add(tableScrollPane, BorderLayout.CENTER);
        
        return root;
    }

    private JComponent buildFormCT() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        
        g.insets = new Insets(8, 10, 8, 10); 
        
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        txtCT_MaSP = new JTextField();
        txtCT_MaKM = new JTextField();
        txtCT_MaKM.setEditable(false);
        txtCT_TiLe = new JTextField();
        cbCT_LoaiKM = new JComboBox<>();
        
        txtCT_MaSP.setFont(FONT_TABLE_CELL);
        txtCT_MaKM.setFont(FONT_TABLE_CELL);
        txtCT_TiLe.setFont(FONT_TABLE_CELL);
        cbCT_LoaiKM.setFont(FONT_TABLE_CELL);

        addField(form, g, 0, "Mã SP:", txtCT_MaSP);
        addField(form, g, 1, "Mã KM:", txtCT_MaKM);
        addField(form, g, 2, "Loại chi tiết:", cbCT_LoaiKM);

        lbCT_TiLe = new JLabel("Tỉ lệ (Giá trị/Phần trăm):");
        lbCT_TiLe.setFont(FONT_TABLE_CELL.deriveFont(Font.BOLD)); 
        
        g.gridy = 3;
        g.gridx = 0; g.weightx = 0;
        form.add(lbCT_TiLe, g);
        g.gridx = 1; g.weightx = 1;
        form.add(txtCT_TiLe, g);

        productSearchPopup = new JPopupMenu();
        productSearchPopup.setFocusable(false);
        productSearchPopup.setBorder(BorderFactory.createLineBorder(CLR_BORDER));

        productListPanel = new JPanel();
        productListPanel.setLayout(new BoxLayout(productListPanel, BoxLayout.Y_AXIS));

        JScrollPane popupScroll = new JScrollPane(productListPanel);
        popupScroll.setBorder(null);
        popupScroll.setPreferredSize(new Dimension(350, 350)); 
        productSearchPopup.add(popupScroll);

        searchTimer = new Timer(300, e -> triggerSearchWorker());
        searchTimer.setRepeats(false);

        return form;
    }

    private JComponent buildActionsCT() {
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        
        btnCT_Them = mkBtn("Thêm", CLR_SUCCESS);
        btnCT_Sua = mkBtn("Sửa", CLR_PRIMARY);
        btnCT_Xoa = mkBtn("Xóa", CLR_DANGER);
        btnCT_Moi = mkBtn("Mới", CLR_WARNING);

        Dimension btnSize = DIM_BUTTON;
        btnCT_Them.setMaximumSize(btnSize);
        btnCT_Sua.setMaximumSize(btnSize);
        btnCT_Xoa.setMaximumSize(btnSize);
        btnCT_Moi.setMaximumSize(btnSize);
        
        actions.add(btnCT_Them);
        actions.add(Box.createRigidArea(new Dimension(0, 10)));
        actions.add(btnCT_Sua);
        actions.add(Box.createRigidArea(new Dimension(0, 10)));
        actions.add(btnCT_Xoa);
        actions.add(Box.createRigidArea(new Dimension(0, 10)));
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
        tblCT.getTableHeader().setFont(FONT_TABLE_HEADER);
        
        DefaultTableCellRenderer cellRendererLeft = new DefaultTableCellRenderer();
        cellRendererLeft.setHorizontalAlignment(SwingConstants.LEFT);
        cellRendererLeft.setBorder(CELL_PADDING);
        DefaultTableCellRenderer cellRendererCenter = new DefaultTableCellRenderer();
        cellRendererCenter.setHorizontalAlignment(SwingConstants.CENTER);
        cellRendererCenter.setBorder(CELL_PADDING);

        DefaultTableCellRenderer tiLeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                try {
                    String loaiKM_Str = String.valueOf(table.getValueAt(row, 2));
                    double tiLe = (Double) value;

                    if (loaiKM_Str.equals(LoaiKM.GiamGiaPhanTramSP.toString())) {
                        NumberFormat percentFormat = NumberFormat.getPercentInstance();
                        percentFormat.setMaximumFractionDigits(1);
                        ((JLabel) c).setText(percentFormat.format(tiLe));
                    } else {
                        ((JLabel) c).setText(formatGia(tiLe));
                    }

                } catch (Exception e) {
                    ((JLabel) c).setText(value.toString());
                }

                ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                ((JLabel) c).setBorder(CELL_PADDING);

                return c;
            }
        };

        tblCT.getColumnModel().getColumn(0).setCellRenderer(cellRendererCenter);
        tblCT.getColumnModel().getColumn(1).setCellRenderer(cellRendererCenter);
        tblCT.getColumnModel().getColumn(2).setCellRenderer(cellRendererLeft);
        tblCT.getColumnModel().getColumn(3).setCellRenderer(tiLeRenderer);

        tblCT.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblCT.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblCT.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblCT.getColumnModel().getColumn(3).setPreferredWidth(100);

        tblCT.addMouseListener(this);
        
        return new JScrollPane(tblCT);
    }

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
        
        txtKM_Tim.getDocument().addDocumentListener(new DocumentListener() {
            private void triggerSearch() {
                if (isUpdatingKmFromPopup) return;
                kmSearchTimer.restart(); 
            }
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });

        txtKM_Tim.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getOppositeComponent() != null &&
                    !SwingUtilities.isDescendingFrom(e.getOppositeComponent(), kmSearchPopup)) {
                    kmSearchPopup.setVisible(false);
                }
            }
        });
        
        
        tblKM.addMouseListener(this);
        btnCT_Them.addActionListener(this);
        btnCT_Sua.addActionListener(this);
        btnCT_Xoa.addActionListener(this);
        btnCT_Moi.addActionListener(this);
        tblCT.addMouseListener(this);

        cbCT_LoaiKM.addActionListener(this);

        txtCT_MaSP.getDocument().addDocumentListener(new DocumentListener() {
            private void triggerSearch() {
                if (isUpdatingFromPopup) return;
                searchTimer.restart(); 
            }
            public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });

        txtCT_MaSP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getOppositeComponent() != null &&
                    !SwingUtilities.isDescendingFrom(e.getOppositeComponent(), productSearchPopup)) {
                    productSearchPopup.setVisible(false);
                }
            }
        });
    }

    // =================================================================
    // SECTION: TẢI DỮ LIỆU & LÀM MỚI (LOAD & REFRESH)
    // =================================================================

    private void loadLoaiKM() {
        try {
            cbCT_LoaiKM.removeAllItems();
            for (LoaiKM loai : LoaiKM.values()) {
                cbCT_LoaiKM.addItem(loai.toString()); 
            }
        } catch (Exception ex) {
            msg("Lỗi tải LoạiKM: " + ex.getMessage());
        }
    }

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
        setKMControlsEnabled(false); 
        setCTControlsEnabled(false); 
        setCursor(CURSOR_WAIT);
        mdlKM.setRowCount(0); 

        new SwingWorker<List<KhuyenMai>, Void>() {
            @Override
            protected List<KhuyenMai> doInBackground() throws Exception {
                return daoKM.findAll();
            }

            @Override
            protected void done() {
                try {
                    populateTableKM(get());
                } catch (Exception e) {
                    msg("Lỗi tải KM: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setKMControlsEnabled(true);
                    setCTControlsEnabled(false); 
                    setCursor(CURSOR_DEFAULT);
                    clearKMForm(); 
                }
            }
        }.execute();
    }

    private void populateTableCT(List<CT_KhuyenMai> ds) {
        mdlCT.setRowCount(0);
        for (CT_KhuyenMai ct : ds) {
            mdlCT.addRow(new Object[]{
                    ct.getSanPham().getMaSP(),
                    ct.getKhuyenMai().getMaKM(),
                    ct.getLoaiKM().toString(), 
                    ct.getGiaTri()
            });
        }
    }

    private void loadTableCT(int maKM) {
        if (maKM == -1) {
            mdlCT.setRowCount(0);
            return;
        }
        
        setKMControlsEnabled(false); 
        setCTControlsEnabled(false); 
        setCursor(CURSOR_WAIT);
        mdlCT.setRowCount(0); 

        new SwingWorker<List<CT_KhuyenMai>, Void>() {
            @Override
            protected List<CT_KhuyenMai> doInBackground() throws Exception {
                return daoCT.findByMaKM(maKM);
            }

            @Override
            protected void done() {
                try {
                    populateTableCT(get());
                } catch (Exception e) {
                    msg("Lỗi tải Chi Tiết KM: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setKMControlsEnabled(true);
                    setCTControlsEnabled(true); 
                    setCursor(CURSOR_DEFAULT);
                }
            }
        }.execute();
    }

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

        else if (s == cbCT_LoaiKM) {
            updateTiLeLabel();
        }
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

        KhuyenMai kmMoi;
        try {
            kmMoi = collectKMForInsert();
        } catch (Exception e) {
            msg("Lỗi khi thu thập dữ liệu: " + e.getMessage());
            return;
        }

        setKMControlsEnabled(false);
        setCTControlsEnabled(false);
        setCursor(CURSOR_WAIT);

        new SwingWorker<KhuyenMai, Void>() {
            @Override
            protected KhuyenMai doInBackground() throws Exception {
                return daoKM.insert(kmMoi);
            }

            @Override
            protected void done() {
                try {
                    KhuyenMai kmDaThem = get();
                    if (kmDaThem != null) {
                        msg("Đã thêm KM, mã = " + kmDaThem.getMaKM());
                        loadTableKM();
                        selectKMRowById(kmDaThem.getMaKM());
                    } else {
                        msg("Lỗi thêm KM: DAO trả về null");
                        setKMControlsEnabled(true);
                        setCursor(CURSOR_DEFAULT);
                    }
                } catch (Exception ex) {
                    msg("Lỗi thêm KM: " + ex.getMessage());
                    setKMControlsEnabled(true);
                    setCursor(CURSOR_DEFAULT);
                }
            }
        }.execute();
    }

    private void handleSuaKM() {
        if (!validKM(true)) return;

        KhuyenMai kmSua;
        try {
            kmSua = collectKMForUpdate();
        } catch (Exception e) {
            msg("Lỗi khi thu thập dữ liệu: " + e.getMessage());
            return;
        }

        setKMControlsEnabled(false);
        setCTControlsEnabled(false);
        setCursor(CURSOR_WAIT);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return daoKM.update(kmSua);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        msg("Đã cập nhật KM");
                        loadTableKM();
                        selectKMRowById(kmSua.getMaKM());
                    } else {
                        msg("Lỗi cập nhật KM");
                        setKMControlsEnabled(true);
                        setCTControlsEnabled(true);
                        setCursor(CURSOR_DEFAULT);
                    }
                } catch (Exception ex) {
                    msg("Lỗi cập nhật KM: " + ex.getMessage());
                    setKMControlsEnabled(true);
                    setCTControlsEnabled(true);
                    setCursor(CURSOR_DEFAULT);
                }
            }
        }.execute();
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
            
            setKMControlsEnabled(false);
            setCTControlsEnabled(false);
            setCursor(CURSOR_WAIT);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return daoKM.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            loadTableKM(); 
                            clearKMForm();
                        } else {
                            msg("Lỗi xóa KM");
                            setKMControlsEnabled(true);
                            setCursor(CURSOR_DEFAULT);
                        }
                    } catch (Exception ex) {
                        msg("Lỗi xóa KM: " + ex.getMessage());
                        setKMControlsEnabled(true);
                        setCursor(CURSOR_DEFAULT);
                    }
                }
            }.execute();
        }
    }

    private void handleTimKM() {
        String keyword = txtKM_Tim.getText().trim();
        setKMControlsEnabled(false);
        setCTControlsEnabled(false);
        setCursor(CURSOR_WAIT);
        mdlKM.setRowCount(0); 
        
        if (kmSearchPopup != null) {
            kmSearchPopup.setVisible(false);
        }

        new SwingWorker<List<KhuyenMai>, Void>() {
            @Override
            protected List<KhuyenMai> doInBackground() throws Exception {
                return daoKM.search(keyword);
            }

            @Override
            protected void done() {
                try {
                    populateTableKM(get());
                } catch (Exception ex) {
                    msg("Lỗi tìm KM: " + ex.getMessage());
                } finally {
                    setKMControlsEnabled(true);
                    setCTControlsEnabled(false);
                    setCursor(CURSOR_DEFAULT);
                    clearKMForm();
                }
            }
        }.execute();
    }

    private void handleBoLocKM() {
        txtKM_Tim.setText("");
        if(kmSearchPopup != null) { 
            kmSearchPopup.setVisible(false);
        }
        loadTableKM(); 
        clearKMForm(); 
    }

    private void handleThemCT() {
        if (!validCT(false)) {
            return;
        }

        CT_KhuyenMai ctMoi;
        int kmID;
        try {
            ctMoi = collectCT();
            kmID = getSelectedKMId();
            if (kmID == -1) {
                msg("Chưa chọn chương trình khuyến mãi (bên trái).");
                return;
            }
        } catch (Exception e) {
            msg("Lỗi khi thu thập dữ liệu: " + e.getMessage());
            return;
        }

        setKMControlsEnabled(false);
        setCTControlsEnabled(false);
        setCursor(CURSOR_WAIT);

        new SwingWorker<Boolean, String>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                int maKM_check = ctMoi.getKhuyenMai().getMaKM();
                String maSP_check = ctMoi.getSanPham().getMaSP();
                String loaiKM_check_Name = ctMoi.getLoaiKM().name(); 

                if (daoSP.findById(maSP_check) == null) {
                    throw new Exception("SP_KHONG_TON_TAI"); 
                }
                
                if (daoCT.findOne(maKM_check, maSP_check, loaiKM_check_Name) != null) {
                    throw new Exception("TRUNG_KHOA");
                }

                return daoCT.insert(ctMoi);
            }

            @Override
            protected void done() {
                try {
                    boolean insertSuccess = get(); 
                    if (insertSuccess) {
                        msg("Thêm chi tiết thành công.");
                        loadTableCT(kmID); 
                        clearCTForm();
                    } else {
                        msg("Lỗi thêm CT: Không thể lưu vào CSDL.");
                        setKMControlsEnabled(true);
                        setCTControlsEnabled(true);
                        setCursor(CURSOR_DEFAULT);
                    }
                } catch (Exception e) {
                    String nguyenNhan = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                    
                    if ("SP_KHONG_TON_TAI".equals(nguyenNhan)) {
                        msg("LỖI THÊM: Mã sản phẩm '" + ctMoi.getSanPham().getMaSP() + "' không tồn tại.\n" +
                            "Vui lòng kiểm tra lại hoặc dùng chức năng tìm kiếm.");
                        txtCT_MaSP.requestFocus(); 
                    } else if ("TRUNG_KHOA".equals(nguyenNhan)) {
                        msg("LỖI THÊM: Chi tiết khuyến mãi này đã tồn tại.\n" +
                            "Không thể thêm trùng (Mã KM, Mã SP, Loại KM).\n" +
                            "Vui lòng chọn 'Sửa' nếu muốn cập nhật.");
                    } else {
                        msg("Lỗi nghiêm trọng khi thêm CT: " + nguyenNhan);
                        e.printStackTrace();
                    }
                    
                    setKMControlsEnabled(true);
                    setCTControlsEnabled(true);
                    setCursor(CURSOR_DEFAULT);
                }
            }
        }.execute();
    }

    private void handleSuaCT() {
        int r = tblCT.getSelectedRow();
        if (r == -1) {
            msg("Phải chọn 1 dòng chi tiết để sửa");
            return;
        }
        if (!validCT(true)) return;
        
        int modelRow = tblCT.convertRowIndexToModel(r);
        CT_KhuyenMai ctSua;
        int kmID;
        
        try {
            ctSua = collectCT();
            kmID = getSelectedKMId();
        } catch (Exception e) {
            msg("Lỗi khi thu thập dữ liệu: " + e.getMessage());
            return;
        }

        String spGoc = String.valueOf(mdlCT.getValueAt(modelRow, 0));
        String loaiGoc = String.valueOf(mdlCT.getValueAt(modelRow, 2)); 

        if (!ctSua.getSanPham().getMaSP().equals(spGoc) || !ctSua.getLoaiKM().toString().equals(loaiGoc)) {
            msg("Không thể thay đổi Mã SP hoặc Loại CT khi cập nhật. Hãy Xóa và Thêm mới.");
            return;
        }

        setKMControlsEnabled(false);
        setCTControlsEnabled(false);
        setCursor(CURSOR_WAIT);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return daoCT.update(ctSua);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        msg("Cập nhật chi tiết thành công.");
                        loadTableCT(kmID); 
                        clearCTForm();
                    } else {
                        msg("Lỗi lưu CT");
                        setKMControlsEnabled(true);
                        setCTControlsEnabled(true);
                        setCursor(CURSOR_DEFAULT);
                    }
                } catch (Exception ex) {
                    msg("Lỗi lưu CT: " + ex.getMessage());
                    setKMControlsEnabled(true);
                    setCTControlsEnabled(true);
                    setCursor(CURSOR_DEFAULT);
                }
            }
        }.execute();
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
        String loaiKM_Str = String.valueOf(mdlCT.getValueAt(modelRow, 2));

        if (JOptionPane.showConfirmDialog(this, "Xóa CT SP " + maSP + " (" + loaiKM_Str + ") ?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            setKMControlsEnabled(false);
            setCTControlsEnabled(false);
            setCursor(CURSOR_WAIT);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    LoaiKM loaiKM_Enum = getEnumFromDisplayString(loaiKM_Str);
                    if (loaiKM_Enum == null) {
                        throw new Exception("Lỗi nhận dạng LoaiKM: " + loaiKM_Str);
                    }
                    
                    return daoCT.delete(kmID, maSP, loaiKM_Enum.name()); 
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            msg("Xóa chi tiết thành công.");
                            loadTableCT(kmID); 
                            clearCTForm();
                        } else {
                            msg("Lỗi xóa CT: Không tìm thấy");
                            setKMControlsEnabled(true);
                            setCTControlsEnabled(true);
                            setCursor(CURSOR_DEFAULT);
                        }
                    } catch (Exception ex) {
                        msg("Lỗi xóa CT: " + ex.getMessage());
                        setKMControlsEnabled(true);
                        setCTControlsEnabled(true);
                        setCursor(CURSOR_DEFAULT);
                    }
                }
            }.execute();
        }
    }

    private void handleClickTableKM() {
        if (!btnKM_Tim.isEnabled()) {
            return;
        }
        
        int r = tblKM.getSelectedRow();
        if (r >= 0) {
            int modelRow = tblKM.convertRowIndexToModel(r);
            fillKMFormFromRow(modelRow);
            
            int maKM = Integer.parseInt(String.valueOf(mdlKM.getValueAt(modelRow, 0)));
            loadTableCT(maKM); 
            
            setCTControlsEnabled(true); 
        }
    }

    private void handleClickTableCT() {
        if (!btnCT_Them.isEnabled()) {
            return;
        }
        
        int r = tblCT.getSelectedRow();
        if (r >= 0) {
            int modelRow = tblCT.convertRowIndexToModel(r);
            txtCT_MaSP.setText(String.valueOf(mdlCT.getValueAt(modelRow, 0)));
            txtCT_MaKM.setText(String.valueOf(mdlCT.getValueAt(modelRow, 1)));
            cbCT_LoaiKM.setSelectedItem(String.valueOf(mdlCT.getValueAt(modelRow, 2)));
            txtCT_TiLe.setText(String.valueOf(mdlCT.getValueAt(modelRow, 3)));

            updateTiLeLabel();
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
            txtKM_Ten.requestFocus();
            return false;
        }

        java.util.Date ngayBD = dcKM_NgayBD.getDate();
        java.util.Date ngayKT = dcKM_NgayKT.getDate();

        if (ngayBD == null || ngayKT == null) {
            msg("Ngày bắt đầu và kết thúc không được rỗng");
            return false;
        }

        if (ngayBD.after(ngayKT)) {
            msg("Ngày bắt đầu phải ≤ ngày kết thúc");
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
            txtCT_MaSP.requestFocus();
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
            txtCT_TiLe.requestFocus();
            return false;
        }
        if (tl < 0) {
            msg("Tỉ lệ/Giá trị phải >= 0");
            txtCT_TiLe.requestFocus();
            return false;
        }

        String loaiKM_Str = String.valueOf(cbCT_LoaiKM.getSelectedItem());
        if (loaiKM_Str.equals(LoaiKM.GiamGiaPhanTramSP.toString())) {
            if (tl > 1) {
                msg("Giá trị Phần trăm phải từ 0 đến 1 (ví dụ: 0.1 cho 10%)");
                txtCT_TiLe.requestFocus();
                return false;
            }
        }
        
        return true;
    }

    private KhuyenMai collectKMForInsert() {
        java.util.Date utilNgayBD = dcKM_NgayBD.getDate();
        java.util.Date utilNgayKT = dcKM_NgayKT.getDate();

        return new KhuyenMai(
                txtKM_Ten.getText().trim(),
                txtKM_MoTa.getText().trim(),
                new java.sql.Date(utilNgayBD.getTime()),
                new java.sql.Date(utilNgayKT.getTime())
        );
    }

    private KhuyenMai collectKMForUpdate() {
        java.util.Date utilNgayBD = dcKM_NgayBD.getDate();
        java.util.Date utilNgayKT = dcKM_NgayKT.getDate();

        return new KhuyenMai(
                Integer.parseInt(txtKM_Ma.getText().trim()),
                txtKM_Ten.getText().trim(),
                txtKM_MoTa.getText().trim(),
                new java.sql.Date(utilNgayBD.getTime()),
                new java.sql.Date(utilNgayKT.getTime())
        );
    }

    private CT_KhuyenMai collectCT() {
        int kmID = Integer.parseInt(txtCT_MaKM.getText().trim());
        String spID = txtCT_MaSP.getText().trim();
        float tiLe = Float.parseFloat(txtCT_TiLe.getText().trim());

        String loaiKM_Str = String.valueOf(cbCT_LoaiKM.getSelectedItem());
        LoaiKM loaiKM_Enum = getEnumFromDisplayString(loaiKM_Str);
        if (loaiKM_Enum == null) {
            throw new IllegalArgumentException("Loại KM không hợp lệ: " + loaiKM_Str);
        }

        KhuyenMai km = new KhuyenMai();
        km.setMaKM(kmID);
        
        SanPham sp = new SanPham(loaiKM_Str, loaiKM_Str, tiLe); 
        sp.setMaSP(spID);
        
        return new CT_KhuyenMai(km, sp, tiLe, loaiKM_Enum);
    }

    // =================================================================
    // SECTION: HÀM TIỆN ÍCH (HELPER UTILS)
    // =================================================================

    private LoaiKM getEnumFromDisplayString(String displayName) {
        for (LoaiKM loai : LoaiKM.values()) {
            if (loai.toString().equals(displayName)) {
                return loai;
            }
        }
        return null; 
    }

    /** Tắt/Bật các control của panel KM (TRỪ BẢNG) */
    private void setKMControlsEnabled(boolean enabled) {
        btnKM_Them.setEnabled(enabled);
        btnKM_Sua.setEnabled(enabled);
        btnKM_Xoa.setEnabled(enabled);
        btnKM_Moi.setEnabled(enabled);
        btnKM_Tim.setEnabled(enabled);
        btnKM_BoLoc.setEnabled(enabled);
        btnKM_XuatCSV.setEnabled(enabled);
        txtKM_Ten.setEnabled(enabled);
        txtKM_MoTa.setEnabled(enabled);
        txtKM_Tim.setEnabled(enabled);
        dcKM_NgayBD.setEnabled(enabled);
        dcKM_NgayKT.setEnabled(enabled);
    }

    /** Tắt/Bật các control của panel CT (TRỪ BẢNG) */
    private void setCTControlsEnabled(boolean enabled) {
        btnCT_Them.setEnabled(enabled);
        btnCT_Sua.setEnabled(enabled);
        btnCT_Xoa.setEnabled(enabled);
        btnCT_Moi.setEnabled(enabled);

        txtCT_MaSP.setEnabled(enabled);
        cbCT_LoaiKM.setEnabled(enabled);
        txtCT_TiLe.setEnabled(enabled);
    }

    private void updateTiLeLabel() {
        if (lbCT_TiLe == null) return;

        String selected = String.valueOf(cbCT_LoaiKM.getSelectedItem());

        if (selected.equals(LoaiKM.GiamGiaPhanTramSP.toString())) {
            lbCT_TiLe.setText("Phần trăm (0.0 - 1.0):");
        } else if (selected.equals(LoaiKM.GiamGiaTienSP.toString())) {
            lbCT_TiLe.setText("Giá trị (VNĐ):");
        } else {
            lbCT_TiLe.setText("Tỉ lệ (Giá trị/Phần trăm):");
        }
    }

    // --- Live Search cho SẢN PHẨM ---
    
    private void triggerSearchWorker() {
        if (searchWorker != null && !searchWorker.isDone()) {
            searchWorker.cancel(true);
        }

        String keyword = txtCT_MaSP.getText().trim();
        if (keyword.isEmpty()) {
            productSearchPopup.setVisible(false);
            return;
        }

        productListPanel.removeAll();
        productListPanel.add(new JLabel("  Đang tìm sản phẩm...  "));
        showProductPopup();

        searchWorker = new SwingWorker<List<SanPham>, Void>() {
            @Override
            protected List<SanPham> doInBackground() throws Exception {
                return daoSP.searchByNameOrMa(keyword, 10);
            }

            @Override
            protected void done() {
                if (isCancelled()) return; 
                try {
                    List<SanPham> results = get();
                    productListPanel.removeAll();
                    if (results.isEmpty()) {
                        productListPanel.add(new JLabel("  Không tìm thấy sản phẩm...  "));
                    } else {
                        for (SanPham sp : results) {
                            JPanel itemPanel = createProductSearchItem(sp);
                            productListPanel.add(itemPanel);
                        }
                    }
                    showProductPopup(); 
                } catch (Exception e) {
                    productListPanel.removeAll();
                    productListPanel.add(new JLabel("  Lỗi tìm kiếm: " + e.getMessage()));
                    showProductPopup();
                }
            }
        };
        searchWorker.execute();
    }
    
    private void showProductPopup() {
        productSearchPopup.pack();
        productSearchPopup.setPopupSize(txtCT_MaSP.getWidth(), Math.min(300, productSearchPopup.getPreferredSize().height));
        if (!productSearchPopup.isVisible()) {
            productSearchPopup.show(txtCT_MaSP, 0, txtCT_MaSP.getHeight());
        }
        txtCT_MaSP.requestFocusInWindow();
    }
    
    // --- Live Search cho KHUYẾN MÃI ---
    
    private void triggerKmSearchWorker() {
        if (kmSearchWorker != null && !kmSearchWorker.isDone()) {
            kmSearchWorker.cancel(true);
        }

        String keyword = txtKM_Tim.getText().trim();
        if (keyword.isEmpty()) {
            kmSearchPopup.setVisible(false);
            return;
        }

        kmListPanel.removeAll();
        kmListPanel.add(new JLabel("  Đang tìm chương trình...  "));
        showKmPopup();

        kmSearchWorker = new SwingWorker<List<KhuyenMai>, Void>() {
            @Override
            protected List<KhuyenMai> doInBackground() throws Exception {
                return daoKM.search(keyword); 
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<KhuyenMai> results = get();
                    kmListPanel.removeAll();
                    if (results.isEmpty()) {
                        kmListPanel.add(new JLabel("  Không tìm thấy...  "));
                    } else {
                        for (KhuyenMai km : results) {
                            JPanel itemPanel = createKmSearchItem(km);
                            kmListPanel.add(itemPanel);
                        }
                    }
                    showKmPopup(); 
                } catch (Exception e) {
                    kmListPanel.removeAll();
                    kmListPanel.add(new JLabel("  Lỗi tìm kiếm: " + e.getMessage()));
                    showKmPopup();
                }
            }
        };
        kmSearchWorker.execute();
    }

    private void showKmPopup() {
        kmSearchPopup.pack();
        kmSearchPopup.setPopupSize(txtKM_Tim.getWidth(), Math.min(250, kmSearchPopup.getPreferredSize().height));
        if (!kmSearchPopup.isVisible()) {
            kmSearchPopup.show(txtKM_Tim, 0, txtKM_Tim.getHeight());
        }
        txtKM_Tim.requestFocusInWindow(); 
    }
    
    private JPanel createKmSearchItem(KhuyenMai km) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
        itemPanel.setBackground(UIManager.getColor("Panel.background"));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER), 
            new EmptyBorder(8, 10, 8, 10) // ✅ THAY ĐỔI: Tăng padding
        ));
        
        itemPanel.setCursor(CURSOR_HAND); 
        
        JLabel nameLabel = new JLabel(km.getTenKM());
        nameLabel.setFont(FONT_TABLE_CELL.deriveFont(Font.BOLD)); // ✅ THAY ĐỔI: In đậm
        itemPanel.add(nameLabel, BorderLayout.CENTER);
        
        if (km.getMoTaKM() != null && !km.getMoTaKM().isEmpty()) {
            JLabel descLabel = new JLabel(km.getMoTaKM());
            descLabel.setFont(FONT_TABLE_CELL.deriveFont(Font.ITALIC, 13f)); // ✅ THAY ĐỔI: Tăng font
            descLabel.setForeground(CLR_MUTED);
            itemPanel.add(descLabel, BorderLayout.SOUTH);
        }
        
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("Table.hoverBackground")); 
            }
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("Panel.background"));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isUpdatingKmFromPopup = true;
                txtKM_Tim.setText(km.getTenKM());
                isUpdatingKmFromPopup = false;
                kmSearchPopup.setVisible(false);
                
                handleTimKM(); 
            }
        });
        
        return itemPanel;
    }
    
    // --- Các hàm tiện ích khác ---

    private String formatGia(double v) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(v) + "đ";
    }

    private JPanel createProductSearchItem(SanPham sp) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
        itemPanel.setBackground(UIManager.getColor("Panel.background"));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER),
            new EmptyBorder(10, 10, 10, 10) // ✅ THAY ĐỔI: Tăng padding
        ));
        
        itemPanel.setCursor(CURSOR_HAND);

        ImageIcon thumb = scaledOrPlaceholder(sp.getHinhAnhSP(), SEARCH_THUMB_SIZE, SEARCH_THUMB_SIZE);
        JLabel imgLabel = new JLabel(thumb);
        imgLabel.setPreferredSize(new Dimension(SEARCH_THUMB_SIZE, SEARCH_THUMB_SIZE));
        imgLabel.setVerticalAlignment(SwingConstants.TOP);
        itemPanel.add(imgLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(sp.getTenSP());
        nameLabel.setFont(FONT_TABLE_CELL.deriveFont(Font.BOLD)); // Giữ in đậm

        JLabel priceLabel = new JLabel(formatGia(sp.getGiaSP()));
        priceLabel.setForeground(Color.RED);
        priceLabel.setFont(FONT_TABLE_CELL); // ✅ THAY ĐỔI: Tăng font

        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4))); // Thêm khoảng cách
        textPanel.add(priceLabel);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(textPanel, BorderLayout.NORTH);

        itemPanel.add(wrapperPanel, BorderLayout.CENTER);

        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("Table.hoverBackground"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("Panel.background"));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                productSearchPopup.setVisible(false);
                isUpdatingFromPopup = true;
                txtCT_MaSP.setText(sp.getMaSP());
                isUpdatingFromPopup = false;
            }
        });

        return itemPanel;
    }


    private void selectKMRowById(int id) {
        for (int i = 0; i < mdlKM.getRowCount(); i++) {
            if (Integer.parseInt(String.valueOf(mdlKM.getValueAt(i, 0))) == id) {
                tblKM.setRowSelectionInterval(i, i);
                tblKM.scrollRectToVisible(tblKM.getCellRect(i, 0, true));
                fillKMFormFromRow(i);
                loadTableCT(id);
                setCTControlsEnabled(true); 
                break;
            }
        }
    }

    private int getSelectedKMId() {
        int r = tblKM.getSelectedRow();
        if (r < 0) return -1;
        int modelRow = tblKM.convertRowIndexToModel(r);
        try {
            return Integer.parseInt(String.valueOf(mdlKM.getValueAt(modelRow, 0)));
        } catch (Exception e) {
            return -1;
        }
    }

    private void fillKMFormFromRow(int r) {
        txtKM_Ma.setText(String.valueOf(mdlKM.getValueAt(r, 0)));
        txtKM_Ten.setText(String.valueOf(mdlKM.getValueAt(r, 1)));
        txtKM_MoTa.setText(String.valueOf(mdlKM.getValueAt(r, 2)));

        dcKM_NgayBD.setDate((java.util.Date) mdlKM.getValueAt(r, 3));
        dcKM_NgayKT.setDate((java.util.Date) mdlKM.getValueAt(r, 4));

        txtCT_MaKM.setText(txtKM_Ma.getText().trim());
    }

    private void clearKMForm() {
        txtKM_Ma.setText("");
        txtKM_Ten.setText("");
        txtKM_MoTa.setText("");

        dcKM_NgayBD.setDate(null);
        dcKM_NgayKT.setDate(null);

        tblKM.clearSelection();
        txtKM_Ten.requestFocus();
        mdlCT.setRowCount(0);
        clearCTForm();
        setCTControlsEnabled(false); 
        
        if(kmSearchPopup != null) { 
            kmSearchPopup.setVisible(false);
        }
    }

    private void clearCTForm() {
        txtCT_MaSP.setText("");
        if (!txtKM_Ma.getText().trim().isEmpty()) {
            txtCT_MaKM.setText(txtKM_Ma.getText().trim());
        } else {
            txtCT_MaKM.setText("");
        }
        
        txtCT_TiLe.setText("");
        if (cbCT_LoaiKM.getItemCount() > 0) cbCT_LoaiKM.setSelectedIndex(0);
        tblCT.clearSelection();
        updateTiLeLabel();
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridy = row; g.gridx = 0; g.weightx = 0;
        
        JLabel l = new JLabel(label);
        // ✅ THAY ĐỔI: Set font cho tất cả label
        l.setFont(FONT_TABLE_CELL.deriveFont(Font.BOLD)); 
        
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        p.add(comp, g);
    }

    /**
     * ✅ THAY ĐỔI: Quay về cách set màu đơn giản (setBackground) theo ý bạn
     */
    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON); // Giữ lại font đã Sửa
        b.setPreferredSize(DIM_BUTTON); // Giữ lại kích thước đã Sửa
        b.setCursor(CURSOR_HAND); // Giữ lại con trỏ chuột

        // ✅ SỬA LỖI: Quay về code set màu GỐC
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE); // Chữ auto màu trắng

        // Xử lý ngoại lệ: Nút Vàng (Warning) nên dùng chữ đen
        if (bg == CLR_WARNING || bg == CLR_MUTED) {
            b.setForeground(Color.BLACK);
        }
        
        return b;
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void exportKMToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu danh sách khuyến mãi (CSV)");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            setKMControlsEnabled(false);
            setCTControlsEnabled(false);
            setCursor(CURSOR_WAIT);
            
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
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
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get(); 
                        msg("Đã xuất CSV (tất cả) thành công!");
                    } catch (Exception ex) {
                        msg("Lỗi xuất CSV: " + ex.getMessage());
                    } finally {
                        setKMControlsEnabled(true);
                        boolean ctEnabled = !txtKM_Ma.getText().trim().isEmpty();
                        setCTControlsEnabled(ctEnabled);
                        setCursor(CURSOR_DEFAULT);
                    }
                }
            }.execute();
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

    // =================================================================
    // SECTION: TIỆN ÍCH HÌNH ẢNH (IMAGE UTILS)
    // =================================================================

    private ImageIcon scaledOrPlaceholder(String filename, int w, int h) {
        Image img = null;
        if (filename != null && !filename.isEmpty()) {
            File f = new File(IMG_DIR, filename);
            if (f.exists() && f.isFile()) {
                ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                img = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            }
        }
        if (img == null)
            img = placeholderImage(w, h, "NO IMG");
        return new ImageIcon(img);
    }

    private Image placeholderImage(int w, int h, String text) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        try {
            g.setColor(new Color(230, 230, 230));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(180, 180, 180));
            g.drawRect(0, 0, w - 1, h - 1);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.setColor(new Color(100, 100, 100));
            g.drawString(text, (w - tw) / 2, (h + fm.getAscent()) / 2 - 2);
        } finally {
            g.dispose();
        }
        return bi;
    }

    // --- (Các hàm MouseListener trống) ---
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
         try {
             // ✅ THAY ĐỔI: Dùng FlatIntelliJLaf cho đẹp hơn
             // UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatLightLaf() );
             UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatIntelliJLaf() ); 
             // UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatDarculaLaf() ); 
         } catch( Exception ex ) {
             System.err.println( "Failed to initialize LaF" );
             try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
        
         SwingUtilities.invokeLater(() -> {
             JFrame f = new JFrame("Quản lý Khuyến mãi");
             f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             f.setSize(1200, 700);
             f.setLocationRelativeTo(null);
             f.add(new TAB_KhuyenMai());
             f.setVisible(true);
         });
    }
}