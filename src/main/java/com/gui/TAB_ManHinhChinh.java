package com.gui;

import com.dao.*;
import com.entity.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class TAB_ManHinhChinh extends JPanel {

    private final DAO_HoaDon daoHoaDon = new DAO_HoaDon();
    private final DAO_SanPham daoSanPham = new DAO_SanPham();
    private final DAO_KhuyenMai daoKhuyenMai = new DAO_KhuyenMai();

    private JLabel lblTenNhanVien, lblMaNhanVien, lblEmail, lblSdt, lblChucVu;
    private JLabel lblTongDoanhThu;
    private JPanel pnlPieChart;

    private JTable tblHoaDon;
    private DefaultTableModel hoaDonModel;

    private JTable tblKhuyenMaiSP;
    private DefaultTableModel kmModel;
    private int kmCurrentPage = 1;
    private final int KM_PAGE_SIZE = 5;
    private JButton btnKmPrev;
    private JButton btnKmNext;
    private JLabel lblKmPageInfo;

    private String maNhanVien;
    private String tenNhanVien;
    private NhanVien nhanVien;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public TAB_ManHinhChinh() {
        this(null, null);
    }

    public TAB_ManHinhChinh(String maNV, String tenNV) {
        this.maNhanVien = maNV;
        this.tenNhanVien = tenNV;
        initUI();
        reloadData();
    }

    public void setNhanVien(NhanVien nv) {
        this.nhanVien = nv;
        if (nv != null) {
            this.maNhanVien = nv.getMaNV();
            this.tenNhanVien = nv.getTenNV();
            lblMaNhanVien.setText("Mã nhân viên: " + nv.getMaNV());
            lblTenNhanVien.setText("Tên nhân viên: " + nv.getTenNV());
            lblEmail.setText("Email: " + (nv.getEmail() != null ? nv.getEmail() : "-"));
            lblSdt.setText("SĐT: " + (nv.getSdt() != null ? nv.getSdt() : "-"));
            lblChucVu.setText("Chức vụ: " + (nv.getChucVu() != null ? nv.getChucVu() : "-"));
        }
        reloadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // =================== TOP SECTION ===================
        JPanel pnlTop = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlTop.setOpaque(false);

        pnlTop.add(buildLeftInfoPanel());
        pnlTop.add(buildRevenuePanel());

        add(pnlTop, BorderLayout.NORTH);

        // =================== CENTER SECTION ===================
        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlCenter.setOpaque(false);

        pnlCenter.add(buildInvoicePanel());
        pnlCenter.add(buildKhuyenMaiPanel());

        add(pnlCenter, BorderLayout.CENTER);
    }

    // =============== LEFT PANEL ===============
    private JPanel buildLeftInfoPanel() {
        JPanel card = createCardPanel();

        JLabel title = new JLabel("Thông tin nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // ======= Avatar tròn =======
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(220, 220, 220));
                g.fillOval(0, 0, getWidth(), getHeight());

                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                String txt = "Avatar";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 3);
            }
        };
        avatarPanel.setPreferredSize(new Dimension(80, 80));
        avatarPanel.setMaximumSize(new Dimension(80, 80));

        // ======= Panel chứa avatar =======
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatarPanel);

        // ======= Tạo các dòng thông tin đẹp mắt =======
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Label bold (Tên trường)
        // Value màu đậm – dễ nhìn
        infoPanel.add(buildInfoRow("Tên nhân viên:", lblTenNhanVien = new JLabel("-")), gbc); gbc.gridy++;
        infoPanel.add(buildInfoRow("Mã nhân viên:", lblMaNhanVien = new JLabel("-")), gbc); gbc.gridy++;
        infoPanel.add(buildInfoRow("Email:", lblEmail = new JLabel("-")), gbc); gbc.gridy++;
        infoPanel.add(buildInfoRow("SĐT:", lblSdt = new JLabel("-")), gbc); gbc.gridy++;
        infoPanel.add(buildInfoRow("Chức vụ:", lblChucVu = new JLabel("-")), gbc); gbc.gridy++;

        // ======= Layout trái – phải =======
        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setOpaque(false);
        content.add(avatarWrapper, BorderLayout.WEST);
        content.add(infoPanel, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }


    private JLabel infoLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lb.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return lb;
    }

    private JPanel buildInfoRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(60, 60, 60));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(30, 30, 30));
        valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        row.add(label);
        row.add(valueLabel);

        return row;
    }

    // =============== REVENUE + PIECHART PANEL ===============
    private JPanel buildRevenuePanel() {
        JPanel card = createCardPanel();

        JLabel title = new JLabel("Doanh thu hôm nay", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        lblTongDoanhThu = new JLabel("Tổng doanh thu: 0 đ", SwingConstants.CENTER);
        lblTongDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 18));

        pnlPieChart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart((Graphics2D) g);
            }
        };
        pnlPieChart.setOpaque(false);
        pnlPieChart.setPreferredSize(new Dimension(350, 240));

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(title);
        mid.add(Box.createVerticalStrut(10));
        mid.add(lblTongDoanhThu);
        mid.add(Box.createVerticalStrut(10));
        mid.add(pnlPieChart);

        card.add(mid, BorderLayout.CENTER);
        return card;
    }

    // =============== INVOICE TABLE PANEL ===============
    private JPanel buildInvoicePanel() {
        JPanel card = createCardPanel();

        JLabel title = new JLabel("Hóa đơn hôm nay");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        hoaDonModel = new DefaultTableModel(new Object[]{"Mã HĐ", "Khách hàng", "Ngày", "Tổng tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblHoaDon = new JTable(hoaDonModel);
        tblHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHoaDon.setRowHeight(25);

        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHoaDon.getSelectedRow() != -1) {
                    String maHD = tblHoaDon.getValueAt(tblHoaDon.getSelectedRow(), 0).toString();
                    openInvoicePDF(maHD);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblHoaDon);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // =============== KHUYẾN MÃI PANEL ===============
    private JPanel buildKhuyenMaiPanel() {
        JPanel card = createCardPanel();

        JLabel title = new JLabel("Sản phẩm đang khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        kmModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Giá", "KM"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblKhuyenMaiSP = new JTable(kmModel);
        tblKhuyenMaiSP.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKhuyenMaiSP.setRowHeight(24);

        JScrollPane scroll = new JScrollPane(tblKhuyenMaiSP);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paging.setOpaque(false);

        btnKmPrev = new JButton("<");
        btnKmNext = new JButton(">");
        lblKmPageInfo = new JLabel("Trang 1/1");

        btnKmPrev.addActionListener(e -> {
            if (kmCurrentPage > 1) {
                kmCurrentPage--;
                loadKhuyenMaiPage();
            }
        });
        btnKmNext.addActionListener(e -> {
            kmCurrentPage++;
            loadKhuyenMaiPage();
        });

        paging.add(btnKmPrev);
        paging.add(btnKmNext);
        paging.add(lblKmPageInfo);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(paging, BorderLayout.SOUTH);

        return card;
    }

    // =============== COMMON CARD STYLE ===============
    private JPanel createCardPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return p;
    }

    // =============== LOAD DATA ===============
    public void reloadData() {
        LocalDate today = LocalDate.now();

        double tongDoanhThu = daoHoaDon.getTongDoanhThuTrongNgay(today, maNhanVien);
        lblTongDoanhThu.setText("Tổng doanh thu: " + df.format(tongDoanhThu) + " đ");

        hoaDonModel.setRowCount(0);
        List<HoaDon> hoaDonList = daoHoaDon.getHoaDonTrongNgay(today, maNhanVien);

        for (HoaDon hd : hoaDonList) {
            String tenKH = (hd.getKhachHang() != null && hd.getKhachHang().getTenKH() != null)
                    ? hd.getKhachHang().getTenKH()
                    : "Khách lẻ";

            hoaDonModel.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    tenKH,
                    hd.getNgayGiaoDich(),
                    df.format(hd.getTongTien())
            });
        }

        Map<String, Integer> raw = daoHoaDon.getThongKeSanPhamTrongNgay(today, maNhanVien);
        Map<String, Integer> top4 = buildTop4PlusOthers(raw);
        pnlPieChart.putClientProperty("data", top4);
        pnlPieChart.repaint();

        loadKhuyenMaiPage();
    }

    private Map<String, Integer> buildTop4PlusOthers(Map<String, Integer> raw) {
        Map<String, Integer> res = new LinkedHashMap<>();
        if (raw == null) return res;
        List<Map.Entry<String, Integer>> list = new ArrayList<>(raw.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int other = 0;
        for (int i = 0; i < list.size(); i++) {
            if (i < 4) res.put(list.get(i).getKey(), list.get(i).getValue());
            else other += list.get(i).getValue();
        }
        if (other > 0) res.put("Sản phẩm khác", other);
        return res;
    }

    private void loadKhuyenMaiPage() {
        kmModel.setRowCount(0);
        List<SanPham> list = daoSanPham.getSanPhamKhuyenMaiPage(kmCurrentPage, KM_PAGE_SIZE);

        int total = daoSanPham.countSanPhamKhuyenMai();
        int maxPage = Math.max(1, (int) Math.ceil(total / (double) KM_PAGE_SIZE));
        if (kmCurrentPage > maxPage) kmCurrentPage = maxPage;

        for (SanPham sp : list) {
            CT_KhuyenMai km = new DAO_CT_KhuyenMai().findBestForProduct(sp.getMaSP());
            double gia = sp.getGiaSP();
            double kmGia = gia;

            if (km != null) {
                switch (km.getLoaiKM()) {
                    case GiamGiaTienSP -> kmGia = gia - km.getGiaTri();
                    case GiamGiaPhanTramSP -> kmGia = gia * (1 - km.getGiaTri() / 100.0);
                }
                kmGia = Math.max(kmGia, 0);
            }

            kmModel.addRow(new Object[]{
                    sp.getMaSP(),
                    sp.getTenSP(),
                    df.format(gia),
                    df.format(kmGia)
            });
        }

        lblKmPageInfo.setText("Trang " + kmCurrentPage + "/" + maxPage);
    }

    // =============== PIE CHART ===============
    private void drawPieChart(Graphics2D g2) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> data = (Map<String, Integer>) pnlPieChart.getClientProperty("data");
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) return;

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        int diameter = Math.min(pnlPieChart.getWidth() - 150, pnlPieChart.getHeight() - 40);
        diameter = Math.max(diameter, 120);

        int x = 20;
        int y = (pnlPieChart.getHeight() - diameter) / 2;

        float hue = 0;
        int start = 0;

        Map<String, Color> colors = new LinkedHashMap<>();
        for (String key : data.keySet()) {
            Color c = Color.getHSBColor(hue, 0.6f, 0.9f);
            colors.put(key, c);
            hue += 0.15f;
        }

        for (var e : data.entrySet()) {
            int angle = Math.round(360f * e.getValue() / total);
            g2.setColor(colors.get(e.getKey()));
            g2.fillArc(x, y, diameter, diameter, start, angle);
            start += angle;
        }

        int lx = x + diameter + 20;
        int ly = y;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        for (var e : data.entrySet()) {
            g2.setColor(colors.get(e.getKey()));
            g2.fillRect(lx, ly, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(e.getKey() + " (" + e.getValue() + ")", lx + 20, ly + 12);
            ly += 20;
        }
    }

    // =============== OPEN PDF ===============
    private void openInvoicePDF(String maHoaDon) {
        String path = "HoaDon/HD_" + maHoaDon + ".pdf";
        File f = new File(path);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy file: " + f.getAbsolutePath());
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
