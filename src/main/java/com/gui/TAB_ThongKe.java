package com.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import javax.swing.table.TableModel;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import com.dao.DAO_ThongKe;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class TAB_ThongKe extends JPanel implements ActionListener {
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final DAO_ThongKe daoThongKe = new DAO_ThongKe();

    // Tạo 3 nhóm UI - 1 nhóm cho mỗi tab để tránh trùng lặp biến
    private final TabUI uiSanPham = new TabUI(TabType.SAN_PHAM);
    private final TabUI uiHoaDon  = new TabUI(TabType.HOA_DON);
    private final TabUI uiKhuyenMai = new TabUI(TabType.KHUYEN_MAI);
    
 // ===== PHÂN TRANG CHO MỖI TAB =====
    private int currentPageSP = 1, currentPageHD = 1, currentPageKM = 1;
    private final int rowsPerPage = 10;

    // UI phân trang
    private JLabel lblPageSP, lblPageHD, lblPageKM;
    private JButton btnPrevSP, btnNextSP, btnPrevHD, btnNextHD, btnPrevKM, btnNextKM;

    public TAB_ThongKe() {
        setLayout(new BorderLayout(10,10));
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Thống kê doanh thu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0,90,200));
        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeader.setBackground(Color.WHITE);
        pHeader.add(lblTitle);
        add(pHeader, BorderLayout.NORTH);

        JTabbedPane tabCon = new JTabbedPane();
        tabCon.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        tabCon.addTab("Thống kê doanh thu sản phẩm", createTabSanPham(uiSanPham));
        tabCon.addTab("Thống kê hóa đơn theo nhân viên", createTabHoaDon(uiHoaDon));
        tabCon.addTab("Thống kê khuyến mãi hiện tại", createTabKhuyenMai(uiKhuyenMai));

        add(tabCon, BorderLayout.CENTER);
    }
    
    private String formatDate(Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    // ----- Tạo mỗi tab -----
    private JPanel createTabSanPham(TabUI ui) {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(Color.WHITE);

        JPanel pWest = createLeftOverview(ui);
        JPanel pCenter = new JPanel(new BorderLayout(10,10));
        pCenter.setBackground(Color.WHITE);

        // Bảng + biểu đồ
        ui.tableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Mã sản phẩm","Tên sản phẩm","Số lượng bán","Doanh thu"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ui.table = new JTable(ui.tableModel);
        JScrollPane scrollSP = new JScrollPane(ui.table);
        scrollSP.setBorder(BorderFactory.createTitledBorder("Doanh thu theo sản phẩm"));

        ui.dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart("Top sản phẩm bán chạy","Sản phẩm","Số lượng", ui.dataset);
        ChartPanel chartPanel = new ChartPanel(chart);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollSP, chartPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(8);
        pCenter.add(splitPane, BorderLayout.CENTER);

        // --- PHÂN TRANG ---
        JPanel pPage = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ui.btnPrev = new JButton("< Trang trước");
        ui.btnNext = new JButton("Trang sau >");
        ui.lblPage = new JLabel("Trang 1 / 1");
        pPage.add(ui.btnPrev);
        pPage.add(ui.lblPage);
        pPage.add(ui.btnNext);
        pCenter.add(pPage, BorderLayout.SOUTH);

        ui.btnPrev.addActionListener(e -> {
            if (ui.currentPage > 1) {
                ui.currentPage--;
                performThongKe(uiSanPham, TabType.SAN_PHAM);
            }
        });
        ui.btnNext.addActionListener(e -> {
            int total = daoThongKe.getTongSoSanPhamBan(ui.tuNgay.getDate(), ui.denNgay.getDate());
            int totalPage = (int) Math.ceil((double) total / rowsPerPage);
            if (ui.currentPage < totalPage) {
                ui.currentPage++;
                performThongKe(uiSanPham, TabType.SAN_PHAM);
            }
        });

        panel.add(pWest, BorderLayout.WEST);
        panel.add(pCenter, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTabHoaDon(TabUI ui) {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(Color.WHITE);

        JPanel pWest = createLeftOverview(ui);
        JPanel pCenter = new JPanel(new BorderLayout(10,10));
        pCenter.setBackground(Color.WHITE);

        // Bảng thống kê theo nhân viên
        ui.tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Mã nhân viên", "Tên nhân viên", "Số hóa đơn", "Doanh thu"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ui.table = new JTable(ui.tableModel);
        JScrollPane scroll = new JScrollPane(ui.table);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0,90,200)),
            "Thống kê hóa đơn theo nhân viên",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14),
            new Color(0,90,200)
        ));

        pCenter.add(scroll, BorderLayout.CENTER);

        // === THÊM PHÂN TRANG ===
        JPanel pPage = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ui.btnPrev = new JButton("< Trang trước");
        ui.btnNext = new JButton("Trang sau >");
        ui.lblPage = new JLabel("Trang 1 / 1");
        pPage.add(ui.btnPrev);
        pPage.add(ui.lblPage);
        pPage.add(ui.btnNext);
        pCenter.add(pPage, BorderLayout.SOUTH);

        // === SỰ KIỆN PHÂN TRANG ===
        ui.btnPrev.addActionListener(e -> {
            if (ui.currentPage > 1) {
                ui.currentPage--;
                performThongKe(uiHoaDon, TabType.HOA_DON);
            }
        });
        ui.btnNext.addActionListener(e -> {
            Date tuNgay = uiHoaDon.tuNgay.getDate();
            Date denNgay = uiHoaDon.denNgay.getDate();
            if (tuNgay != null && denNgay != null) {
                int total = daoThongKe.getTongSoNhanVienCoHoaDon(tuNgay, denNgay);
                int totalPage = (int) Math.ceil((double) total / rowsPerPage); // dùng rowsPerPage
                if (ui.currentPage < totalPage) {
                    ui.currentPage++;
                    performThongKe(uiHoaDon, TabType.HOA_DON);
                }
            }
        });


        panel.add(pWest, BorderLayout.WEST);
        panel.add(pCenter, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTabKhuyenMai(TabUI ui) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JPanel pWest = createLeftOverview(ui);
        JPanel pCenter = new JPanel(new BorderLayout(10, 10));
        pCenter.setBackground(Color.WHITE);

        // --- Bảng khuyến mãi ---
        ui.tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Mã KM", "Tên khuyến mãi", "Sản phẩm", "Bắt đầu", "Kết thúc"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ui.table = new JTable(ui.tableModel);

        JScrollPane scroll = new JScrollPane(ui.table);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 90, 200)),
            "Khuyến mãi hiện hành",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14),
            new Color(0, 90, 200)
        ));
        pCenter.add(scroll, BorderLayout.CENTER);

        // --- Panel tìm kiếm (SOUTH) ---
        JPanel pSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pSouth.setBackground(Color.WHITE);
        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        ui.txtTimKiemKM = new JTextField(20);
        ui.btnTimKiemKM = new JButton("Tìm");
        ui.btnTimKiemKM.setBackground(new Color(0, 90, 200));
        ui.btnTimKiemKM.setForeground(Color.WHITE);
        ui.btnTimKiemKM.setFocusPainted(false);
        pSouth.add(lblTimKiem);
        pSouth.add(ui.txtTimKiemKM);
        pSouth.add(ui.btnTimKiemKM);
        pCenter.add(pSouth, BorderLayout.SOUTH);

        // === THÊM PHÂN TRANG (dưới tìm kiếm) ===
        JPanel pPage = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ui.btnPrev = new JButton("< Trang trước");
        ui.btnNext = new JButton("Trang sau >");
        ui.lblPage = new JLabel("Trang 1 / 1");
        pPage.add(ui.btnPrev);
        pPage.add(ui.lblPage);
        pPage.add(ui.btnNext);
        pCenter.add(pPage, BorderLayout.SOUTH); // Đè lên pSouth → dùng JSplitPane hoặc Box nếu cần

        // === SỬA: Dùng Box để xếp SOUTH + PAGE ===
        JPanel pSouthWrapper = new JPanel(new BorderLayout());
        pSouthWrapper.add(pSouth, BorderLayout.NORTH);
        pSouthWrapper.add(pPage, BorderLayout.SOUTH);
        pCenter.add(pSouthWrapper, BorderLayout.SOUTH);

        // === SỰ KIỆN TÌM KIẾM + PHÂN TRANG ===
        ui.btnTimKiemKM.addActionListener(this);
        ui.btnPrev.addActionListener(e -> {
            if (ui.currentPage > 1) {
                ui.currentPage--;
                performThongKe(uiKhuyenMai, TabType.KHUYEN_MAI);
            }
        });
        ui.btnNext.addActionListener(e -> {
            Date tuNgay = uiKhuyenMai.tuNgay.getDate();
            Date denNgay = uiKhuyenMai.denNgay.getDate();
            if (tuNgay != null && denNgay != null) {
                int total = daoThongKe.getTongSoKhuyenMaiHienHanh(tuNgay, denNgay);
                int totalPage = (int) Math.ceil((double) total / rowsPerPage); // dùng rowsPerPage
                if (ui.currentPage < totalPage) {
                    ui.currentPage++;
                    performThongKe(uiKhuyenMai, TabType.KHUYEN_MAI);
                }
            }
        });


        panel.add(pWest, BorderLayout.WEST);
        panel.add(pCenter, BorderLayout.CENTER);
        return panel;
    }

    // Tạo phần left chung cho mỗi tab (nhưng từng instance UI riêng)
    private JPanel createLeftOverview(TabUI ui) {
        JPanel pWest = new JPanel(new GridBagLayout());
        pWest.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font font = new Font("Arial", Font.PLAIN, 16);

        JLabel lblTuNgay = new JLabel("Từ ngày:"); lblTuNgay.setFont(font);
        ui.tuNgay = new JDateChooser(); ui.tuNgay.setDateFormatString("dd/MM/yyyy"); ui.tuNgay.setDate(new Date());
        gbc.gridx = 0; gbc.gridy = 0; pWest.add(lblTuNgay, gbc);
        gbc.gridx = 1; pWest.add(ui.tuNgay, gbc);

        JLabel lblDenNgay = new JLabel("Đến ngày:"); lblDenNgay.setFont(font);
        ui.denNgay = new JDateChooser(); ui.denNgay.setDateFormatString("dd/MM/yyyy"); ui.denNgay.setDate(new Date());
        gbc.gridx = 0; gbc.gridy = 1; pWest.add(lblDenNgay, gbc);
        gbc.gridx = 1; pWest.add(ui.denNgay, gbc);

        ui.btnThongKe = new JButton("Thống kê");
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; pWest.add(ui.btnThongKe, gbc);
        ui.btnThongKe.addActionListener(this);

        JLabel lblTongSP = new JLabel("Tổng sản phẩm:");
        ui.txtTongSP = new JTextField(10); ui.txtTongSP.setEditable(false);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=1; pWest.add(lblTongSP, gbc); gbc.gridx=1; pWest.add(ui.txtTongSP, gbc);

        JLabel lblTongHD = new JLabel("Tổng số hóa đơn:");
        ui.txtTongHD = new JTextField(10); ui.txtTongHD.setEditable(false);
        gbc.gridx=0; gbc.gridy=4; pWest.add(lblTongHD, gbc); gbc.gridx=1; pWest.add(ui.txtTongHD, gbc);

        JLabel lblTongDoanhThu = new JLabel("Tổng doanh thu:");
        ui.txtTongDoanhThu = new JTextField(10); ui.txtTongDoanhThu.setEditable(false);
        gbc.gridx=0; gbc.gridy=5; pWest.add(lblTongDoanhThu, gbc); gbc.gridx=1; pWest.add(ui.txtTongDoanhThu, gbc);

        ui.btnXuat = new JButton("Xuất File");
        gbc.gridx=0; gbc.gridy=6; gbc.gridwidth=2; pWest.add(ui.btnXuat, gbc);
        
        ui.btnXuat.addActionListener(this);

        TitledBorder border_left = BorderFactory.createTitledBorder("Thống kê tổng quan");
        border_left.setTitleColor(new Color(0,90,200));
        border_left.setTitleFont(new Font("Arial", Font.BOLD, 18));
        pWest.setBorder(border_left);

        return pWest;
    }
    
    public void exportToExcel(JTable table, String sheetName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");

        // Mặc định tên file
        fileChooser.setSelectedFile(new File(sheetName + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Tạo workbook và sheet
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet(sheetName);

                // Xuất header (tiêu đề cột)
                TableModel model = table.getModel();
                Row header = sheet.createRow(0);
                for (int col = 0; col < model.getColumnCount(); col++) {
                    header.createCell(col).setCellValue(model.getColumnName(col));
                }

                // Xuất từng dòng
                for (int row = 0; row < model.getRowCount(); row++) {
                    Row excelRow = sheet.createRow(row + 1);
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object value = model.getValueAt(row, col);
                        excelRow.createCell(col).setCellValue(
                            (value != null) ? value.toString() : ""
                        );
                    }
                }

                // Auto fit chiều rộng cột
                for (int col = 0; col < model.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }

                // Ghi file
                FileOutputStream out = new FileOutputStream(fileToSave);
                workbook.write(out);
                workbook.close();
                out.close();

                JOptionPane.showMessageDialog(null, "Xuất file Excel thành công:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi khi xuất file Excel: " + ex.getMessage());
            }
        }
    }


    // ----- Xử lý sự kiện chung -----
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == uiSanPham.btnThongKe) {
            uiSanPham.currentPage = 1;
            performThongKe(uiSanPham, TabType.SAN_PHAM);
        } else if (o == uiHoaDon.btnThongKe) {
            uiHoaDon.currentPage = 1;
            performThongKe(uiHoaDon, TabType.HOA_DON);
        } else if (o == uiKhuyenMai.btnThongKe) {
            uiKhuyenMai.currentPage = 1;
            performThongKe(uiKhuyenMai, TabType.KHUYEN_MAI);
        } 
        // ---- Xử lý xuất file ----
        else if (o == uiSanPham.btnXuat) {
            exportToExcel(uiSanPham.table, "DoanhThuSanPham");
        } else if (o == uiHoaDon.btnXuat) {
            exportToExcel(uiHoaDon.table, "DoanhThuHoaDon");
        } else if (o == uiKhuyenMai.btnXuat) {
            exportToExcel(uiKhuyenMai.table, "KhuyenMaiHienHanh");
        }
    }

    private void performThongKe(TabUI ui, TabType type) {
        Date tuNgay = ui.tuNgay.getDate();
        Date denNgay = ui.denNgay.getDate();
        if (tuNgay == null || denNgay == null || tuNgay.after(denNgay)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian hợp lệ!");
            return;
        }

        // Cập nhật tổng quan
        ui.txtTongSP.setText(String.valueOf(daoThongKe.getTongSP(tuNgay, denNgay)));
        ui.txtTongHD.setText(String.valueOf(daoThongKe.getTongHoaDon(tuNgay, denNgay)));
        ui.txtTongDoanhThu.setText(moneyFormat.format(daoThongKe.getTongDoanhThu(tuNgay, denNgay)));

        // Xử lý từng tab
        if (type == TabType.SAN_PHAM) {
            List<Object[]> list = daoThongKe.getSanPhamBanTheoTrang(ui.currentPage, rowsPerPage, tuNgay, denNgay);
            ui.tableModel.setRowCount(0);
            for (Object[] row : list) {
                ui.tableModel.addRow(new Object[]{row[0], row[1], row[2], moneyFormat.format(((Number)row[3]).doubleValue())});
            }
            int total = daoThongKe.getTongSoKhuyenMaiHienHanh(tuNgay, denNgay);
            int totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            ui.lblPage.setText("Trang " + ui.currentPage + " / " + totalPage);
            ui.btnPrev.setEnabled(ui.currentPage > 1);
            ui.btnNext.setEnabled(ui.currentPage < totalPage);

            // Biểu đồ top 5
            ui.dataset.clear();
            List<Object[]> top5 = daoThongKe.getTop5SanPham(tuNgay, denNgay);
            for (Object[] row : top5) {
                ui.dataset.addValue(((Number)row[2]).intValue(), "Số lượng", row[1].toString());
            }
        }
        else if (type == TabType.HOA_DON) {
            List<Object[]> list = daoThongKe.getThongKeNhanVienTheoTrang(ui.currentPage, rowsPerPage, tuNgay, denNgay);
            ui.tableModel.setRowCount(0);
            for (Object[] row : list) {
                ui.tableModel.addRow(new Object[]{row[0], row[1], row[2], moneyFormat.format(((Number)row[3]).doubleValue())});
            }
            int total = daoThongKe.getTongSoKhuyenMaiHienHanh(tuNgay, denNgay);
            int totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            ui.lblPage.setText("Trang " + ui.currentPage + " / " + totalPage);
            ui.btnPrev.setEnabled(ui.currentPage > 1);
            ui.btnNext.setEnabled(ui.currentPage < totalPage);

        }
        else if (type == TabType.KHUYEN_MAI) {
            List<Object[]> list = daoThongKe.getKhuyenMaiHienHanhTheoTrang(ui.currentPage, rowsPerPage, tuNgay, denNgay);
            ui.tableModel.setRowCount(0);
            for (Object[] row : list) {
                ui.tableModel.addRow(new Object[]{row[0], row[1], row[2], formatDate((Date)row[3]), formatDate((Date)row[4])});
            }
            int total = daoThongKe.getTongSoKhuyenMaiHienHanh(tuNgay, denNgay);
            int totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            ui.lblPage.setText("Trang " + ui.currentPage + " / " + totalPage);
            ui.btnPrev.setEnabled(ui.currentPage > 1);
            ui.btnNext.setEnabled(ui.currentPage < totalPage);

        }
    }

    // ----- Hỗ trợ -----
    private static class TabUI {
        JDateChooser tuNgay, denNgay;
        JTextField txtTongSP, txtTongHD, txtTongDoanhThu;
        JTextField txtTimKiemKM;
        JButton btnThongKe, btnXuat, btnTimKiemKM;
        JTable table;
        DefaultTableModel tableModel;
        DefaultCategoryDataset dataset;
        final TabType type;

        // Phân trang
        int currentPage = 1;
        JLabel lblPage;
        JButton btnPrev, btnNext;

        TabUI(TabType type) { this.type = type; }
    }

    private enum TabType { SAN_PHAM, HOA_DON, KHUYEN_MAI }
}
