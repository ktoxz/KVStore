package com.gui;

import com.dao.DAO_CT_KhuyenMai;
import com.dao.DAO_HoaDon;
import com.dao.DAO_SanPham;
import com.dao.DAO_KhuyenMai;
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

/**
 * Màn hình chính (Dashboard mới) theo layout mockup.
 * - Bên trái: thông tin nhân viên (avatar placeholder + thông tin cá nhân)
 * - Bên phải: Doanh thu hôm nay (tổng tiền + piechart sản phẩm)
 * - Bên dưới: Danh sách hóa đơn hôm nay của nhân viên, double click để mở PDF
 */
public class TAB_ManHinhChinh extends JPanel {

    private final DAO_HoaDon daoHoaDon = new DAO_HoaDon();
    private final DAO_SanPham daoSanPham = new DAO_SanPham();
    private final DAO_KhuyenMai daoKhuyenMai = new DAO_KhuyenMai();

    private JLabel lblTenNhanVien;
    private JLabel lblMaNhanVien;
    private JLabel lblEmail;
    private JLabel lblSdt;
    private JLabel lblChucVu;
    private JLabel lblTongDoanhThu;
    private JPanel pnlPieChart;

    // bảng hóa đơn
    private JTable tblHoaDon;
    private DefaultTableModel hoaDonModel;

    // khuyến mãi
    private JTable tblKhuyenMaiSP;
    private DefaultTableModel kmModel;
    private int kmCurrentPage = 1;
    private final int KM_PAGE_SIZE = 5;
    private JButton btnKmPrev;
    private JButton btnKmNext;
    private JLabel lblKmPageInfo;

    private String maNhanVien;   // set từ GUI_General
    private String tenNhanVien;  // set từ GUI_General
    private NhanVien nhanVien; // giữ full thông tin NV

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
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TOP (giữ nguyên avatar + info + revenue) =====
        JPanel pnlTop = new JPanel(new BorderLayout(10, 10));
        pnlTop.setBackground(Color.WHITE);

        // Left side: avatar + personal info container
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.X_AXIS));
        pnlLeft.setBackground(Color.WHITE);

        // Avatar placeholder
        JPanel pnlAvatar = new JPanel();
        pnlAvatar.setPreferredSize(new Dimension(200, 200));
        pnlAvatar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlAvatar.setBackground(Color.WHITE);
        pnlAvatar.setLayout(new BorderLayout());
        JLabel lblAvatar = new JLabel("Avt\nTạm thời null,\nchưa trường đó\ntrong db", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        pnlAvatar.add(lblAvatar, BorderLayout.CENTER);

        // Personal info panel
        JPanel pnlInfo = new JPanel();
        pnlInfo.setPreferredSize(new Dimension(350, 200));
        pnlInfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBorder(BorderFactory.createTitledBorder("Thông tin cá nhân"));

        lblTenNhanVien = new JLabel("Tên nhân viên: -");
        lblMaNhanVien = new JLabel("Mã nhân viên: -");
        lblEmail = new JLabel("Email: -");
        lblSdt = new JLabel("SĐT: -");
        lblChucVu = new JLabel("Chức vụ: -");

        for (JLabel lb : new JLabel[]{lblTenNhanVien, lblMaNhanVien, lblEmail, lblSdt, lblChucVu}) {
            lb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        pnlInfo.add(Box.createVerticalStrut(10));
        pnlInfo.add(lblTenNhanVien);
        pnlInfo.add(Box.createVerticalStrut(5));
        pnlInfo.add(lblMaNhanVien);
        pnlInfo.add(Box.createVerticalStrut(5));
        pnlInfo.add(lblEmail);
        pnlInfo.add(Box.createVerticalStrut(5));
        pnlInfo.add(lblSdt);
        pnlInfo.add(Box.createVerticalStrut(5));
        pnlInfo.add(lblChucVu);
        pnlInfo.add(Box.createVerticalGlue());

        pnlLeft.add(pnlAvatar);
        pnlLeft.add(Box.createHorizontalStrut(10));
        pnlLeft.add(pnlInfo);

        // Right side: revenue today
        JPanel pnlRevenue = new JPanel(new BorderLayout());
        pnlRevenue.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlRevenue.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Doanh thu hôm nay", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pnlRevenue.add(lblTitle, BorderLayout.NORTH);

        lblTongDoanhThu = new JLabel("Tổng doanh thu: 0 đ");
        lblTongDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongDoanhThu.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pnlRevenueContent = new JPanel();
        pnlRevenueContent.setBackground(Color.WHITE);
        pnlRevenueContent.setLayout(new BoxLayout(pnlRevenueContent, BoxLayout.Y_AXIS));
        pnlRevenueContent.add(Box.createVerticalStrut(20));
        pnlRevenueContent.add(lblTongDoanhThu);
        pnlRevenueContent.add(Box.createVerticalStrut(20));

        // Pie chart panel
        pnlPieChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart((Graphics2D) g);
            }
        };
        pnlPieChart.setPreferredSize(new Dimension(400, 200));
        pnlPieChart.setBackground(Color.WHITE);

        pnlRevenueContent.add(Box.createVerticalStrut(20));
        pnlRevenueContent.add(pnlPieChart);

        pnlRevenue.add(pnlRevenueContent, BorderLayout.CENTER);

        pnlTop.add(pnlLeft, BorderLayout.WEST);
        pnlTop.add(pnlRevenue, BorderLayout.CENTER);

        // ===== CENTER: chia 2 phần trái - phải =====
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBackground(Color.WHITE);

        // --- Trái: danh sách hóa đơn ---
        hoaDonModel = new DefaultTableModel(new Object[]{"Mã HĐ", "Khách hàng", "Ngày", "Tổng tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHoaDon = new JTable(hoaDonModel);
        tblHoaDon.setRowHeight(26);
        tblHoaDon.setFillsViewportHeight(true);
        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHoaDon.getSelectedRow() != -1) {
                    String maHD = (String) hoaDonModel.getValueAt(tblHoaDon.getSelectedRow(), 0);
                    openInvoicePDF(maHD);
                }
            }
        });
        JScrollPane scrollHD = new JScrollPane(tblHoaDon);
        scrollHD.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlCenter.add(scrollHD, BorderLayout.CENTER);

        // --- Phải: danh sách sản phẩm khuyến mãi ---
        JPanel pnlKm = new JPanel(new BorderLayout(5, 5));
        pnlKm.setPreferredSize(new Dimension(350, 0));
        pnlKm.setBorder(BorderFactory.createTitledBorder("Sản phẩm đang khuyến mãi"));
        pnlKm.setBackground(Color.WHITE);

        kmModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Giá", "KM"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKhuyenMaiSP = new JTable(kmModel);
        tblKhuyenMaiSP.setRowHeight(24);
        JScrollPane scrollKm = new JScrollPane(tblKhuyenMaiSP);
        pnlKm.add(scrollKm, BorderLayout.CENTER);

        // Thanh phân trang
        JPanel pnlKmPaging = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlKmPaging.setBackground(Color.WHITE);
        btnKmPrev = new JButton("<");
        btnKmNext = new JButton(">");
        lblKmPageInfo = new JLabel("Trang 1");

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

        pnlKmPaging.add(btnKmPrev);
        pnlKmPaging.add(btnKmNext);
        pnlKmPaging.add(lblKmPageInfo);
        pnlKm.add(pnlKmPaging, BorderLayout.SOUTH);

        pnlCenter.add(pnlKm, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);
    }

    /**
     * Load dữ liệu doanh thu + danh sách hóa đơn hôm nay.
     * Ở đây tạm thời gọi các hàm stub trong DAO_HoaDon, bạn có thể thay bằng
     * truy vấn thật.
     */
    public void reloadData() {
        LocalDate today = LocalDate.now();

        // Tổng doanh thu hôm nay
        double tongDoanhThu = daoHoaDon.getTongDoanhThuTrongNgay(today, maNhanVien);
        lblTongDoanhThu.setText("Tổng doanh thu: " + df.format(tongDoanhThu) + " đ");

        // Danh sách hóa đơn hôm nay, mỗi dòng tính đúng tổng tiền từ CT_HoaDon
        hoaDonModel.setRowCount(0);
        List<HoaDon> hoaDonList = daoHoaDon.getHoaDonTrongNgay(today, maNhanVien);
        for (HoaDon hd : hoaDonList) {
            double tongTienHD = hd.getTongTien();
            String tenKH = (hd.getKhachHang() != null && hd.getKhachHang().getTenKH() != null)
                    ? hd.getKhachHang().getTenKH()
                    : "Khách lẻ";
            hoaDonModel.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    tenKH,
                    hd.getNgayGiaoDich(),
                    df.format(tongTienHD)
            });
        }

        // Pie chart: lấy thống kê từ DB và chuyển thành Top 4 + Others
        Map<String, Integer> raw = daoHoaDon.getThongKeSanPhamTrongNgay(today, maNhanVien);
        Map<String, Integer> top4PlusOthers = buildTop4PlusOthers(raw);
        pnlPieChart.putClientProperty("data", top4PlusOthers);
        pnlPieChart.repaint();

        // Khuyến mãi: load trang hiện tại
        loadKhuyenMaiPage();
    }

    private Map<String, Integer> buildTop4PlusOthers(Map<String, Integer> raw) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return result;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(raw.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int others = 0;
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            if (i < 4) {
                result.put(e.getKey(), e.getValue());
            } else {
                others += e.getValue();
            }
        }
        if (others > 0) {
            result.put("Sản phẩm khác", others);
        }
        return result;
    }

    private void loadKhuyenMaiPage() {
        kmModel.setRowCount(0);
        List<SanPham> ds = daoSanPham.getSanPhamKhuyenMaiPage(kmCurrentPage, KM_PAGE_SIZE);
        int total = daoSanPham.countSanPhamKhuyenMai();
        int maxPage = Math.max(1, (int) Math.ceil(total / (double) KM_PAGE_SIZE));
        if (kmCurrentPage > maxPage) {
            kmCurrentPage = maxPage;
        }
        for (SanPham sp : ds) {
            double giaGoc = sp.getGiaSP();
            double giaSauKM = giaGoc;
            CT_KhuyenMai km = new DAO_CT_KhuyenMai().findBestForProduct(sp.getMaSP());
            if (km != null) {
                switch (km.getLoaiKM()) {
                    case GiamGiaPhanTramSP -> giaSauKM = giaGoc * (1 - km.getGiaTri() / 100.0);
                    case GiamGiaTienSP -> giaSauKM = giaGoc - km.getGiaTri();
                    default -> {}
                }
            }
            if (giaSauKM < 0) giaSauKM = 0;
            kmModel.addRow(new Object[]{
                    sp.getMaSP(),
                    sp.getTenSP(),
                    df.format(giaGoc),
                    df.format(giaSauKM)
            });
        }
        lblKmPageInfo.setText("Trang " + kmCurrentPage + "/" + maxPage);
        btnKmPrev.setEnabled(kmCurrentPage > 1);
        btnKmNext.setEnabled(kmCurrentPage < maxPage);
    }

    private void drawPieChart(Graphics2D g2) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> data = (Map<String, Integer>) pnlPieChart.getClientProperty("data");
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g2.setColor(Color.LIGHT_GRAY);
            String text = "Chưa có dữ liệu hôm nay";
            FontMetrics fm = g2.getFontMetrics();
            int x = (pnlPieChart.getWidth() - fm.stringWidth(text)) / 2;
            int y = pnlPieChart.getHeight() / 2;
            g2.drawString(text, x, y);
            return;
        }

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return;

        int diameter = Math.min(pnlPieChart.getWidth() - 220, pnlPieChart.getHeight() - 40);
        if (diameter < 80) diameter = Math.min(pnlPieChart.getWidth(), pnlPieChart.getHeight()) - 40;
        int x = 20;
        int y = (pnlPieChart.getHeight() - diameter) / 2;

        // Vẽ pie
        float hue = 0f;
        int startAngle = 0;
        Map<String, Color> colorMap = new LinkedHashMap<>();
        for (String key : data.keySet()) {
            Color c = Color.getHSBColor(hue, 0.65f, 0.95f);
            colorMap.put(key, c);
            hue += 0.15f;
        }

        for (Map.Entry<String, Integer> e : data.entrySet()) {
            int value = e.getValue();
            int angle = Math.round(360f * value / total);
            g2.setColor(colorMap.get(e.getKey()));
            g2.fillArc(x, y, diameter, diameter, startAngle, angle);
            startAngle += angle;
        }

        // Vẽ legend bên phải
        int legendX = x + diameter + 30;
        int legendY = y + 10;
        int boxSize = 14;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            Color c = colorMap.get(e.getKey());
            g2.setColor(c);
            g2.fillRect(legendX, legendY, boxSize, boxSize);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(legendX, legendY, boxSize, boxSize);

            String label = e.getKey() + " (" + e.getValue() + ")";
            g2.drawString(label, legendX + boxSize + 8, legendY + boxSize - 2);
            legendY += boxSize + 6;
        }
    }

    private void openInvoicePDF(String maHoaDon) {
        String fileName = "HoaDon/HD_" + maHoaDon + ".pdf";
        File file = new File(fileName);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy file hóa đơn: " + file.getAbsolutePath(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Hệ thống không hỗ trợ mở file tự động.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể mở file PDF.\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

