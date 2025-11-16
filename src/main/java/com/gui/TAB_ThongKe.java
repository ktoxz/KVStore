package com.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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

    private final int rowsPerPage = 10;

    public TAB_ThongKe() {
        setLayout(new BorderLayout(10,10));
        setBackground(Color.WHITE);

        add(TabStyler.createHeader("THỐNG KÊ DOANH THU"), BorderLayout.NORTH);

        JTabbedPane tabCon = new JTabbedPane();
        tabCon.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        tabCon.addTab("Thống kê doanh thu sản phẩm", createTabSanPham(uiSanPham));
        tabCon.addTab("Thống kê hóa đơn theo nhân viên", createTabHoaDon(uiHoaDon));
        tabCon.addTab("Thống kê khuyến mãi hiện tại", createTabKhuyenMai(uiKhuyenMai));

        add(tabCon, BorderLayout.CENTER);

        TabStyler.applyContentFont(this);
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

        // Thanh tìm kiếm trên cùng
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pSearch.setBackground(Color.WHITE);
        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        ui.txtTimKiem = new JTextField(20);
        ui.btnTimKiem = new JButton("Tìm");
        ui.btnTimKiem.setBackground(new Color(0, 90, 200));
        ui.btnTimKiem.setForeground(Color.WHITE);
        ui.btnTimKiem.setFocusPainted(false);
        pSearch.add(lblTimKiem);
        pSearch.add(ui.txtTimKiem);
        pSearch.add(ui.btnTimKiem);
        pCenter.add(pSearch, BorderLayout.NORTH);

        // Bảng + biểu đồ
        ui.tableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Mã sản phẩm","Tên sản phẩm","Số lượng bán","Doanh thu"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ui.table = new JTable(ui.tableModel);
        JScrollPane scrollSP = new JScrollPane(ui.table);
        scrollSP.setBorder(TabStyler.createSectionBorder("Doanh thu theo sản phẩm"));

        ui.dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart("Top sản phẩm bán chạy","Sản phẩm","Số lượng", ui.dataset);
        ChartPanel chartPanel = new ChartPanel(chart);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollSP, chartPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(8);
        pCenter.add(splitPane, BorderLayout.CENTER);

        // PHÂN TRANG
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

        // Sự kiện tìm kiếm: chỉ lọc trên dữ liệu đã load cho trang hiện tại
        ui.btnTimKiem.addActionListener(e -> applyFilter(ui));
        ui.txtTimKiem.addActionListener(e -> applyFilter(ui));

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

        // Thanh tìm kiếm trên cùng
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pSearch.setBackground(Color.WHITE);
        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        ui.txtTimKiem = new JTextField(20);
        ui.btnTimKiem = new JButton("Tìm");
        ui.btnTimKiem.setBackground(new Color(0, 90, 200));
        ui.btnTimKiem.setForeground(Color.WHITE);
        ui.btnTimKiem.setFocusPainted(false);
        pSearch.add(lblTimKiem);
        pSearch.add(ui.txtTimKiem);
        pSearch.add(ui.btnTimKiem);
        pCenter.add(pSearch, BorderLayout.NORTH);

        // Bảng thống kê theo nhân viên
        ui.tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Mã nhân viên", "Tên nhân viên", "Số hóa đơn", "Doanh thu"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ui.table = new JTable(ui.tableModel);
        JScrollPane scroll = new JScrollPane(ui.table);
        TitledBorder invoicesBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0,90,200)),
            "Thống kê hóa đơn theo nhân viên"
        );
        TabStyler.applySectionTitleFont(invoicesBorder);
        scroll.setBorder(invoicesBorder);

        pCenter.add(scroll, BorderLayout.CENTER);

        // PHÂN TRANG
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
                performThongKe(uiHoaDon, TabType.HOA_DON);
            }
        });
        ui.btnNext.addActionListener(e -> {
            Date tuNgay = uiHoaDon.tuNgay.getDate();
            Date denNgay = uiHoaDon.denNgay.getDate();
            if (tuNgay != null && denNgay != null) {
                int total = daoThongKe.getTongSoNhanVienCoHoaDon(tuNgay, denNgay);
                int totalPage = (int) Math.ceil((double) total / rowsPerPage);
                if (ui.currentPage < totalPage) {
                    ui.currentPage++;
                    performThongKe(uiHoaDon, TabType.HOA_DON);
                }
            }
        });

        ui.btnTimKiem.addActionListener(e -> applyFilter(ui));
        ui.txtTimKiem.addActionListener(e -> applyFilter(ui));

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

        // Thanh tìm kiếm trên cùng (chuyển từ dưới lên trên)
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pSearch.setBackground(Color.WHITE);
        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        ui.txtTimKiem = new JTextField(20);
        ui.btnTimKiem = new JButton("Tìm");
        ui.btnTimKiem.setBackground(new Color(0, 90, 200));
        ui.btnTimKiem.setForeground(Color.WHITE);
        ui.btnTimKiem.setFocusPainted(false);
        pSearch.add(lblTimKiem);
        pSearch.add(ui.txtTimKiem);
        pSearch.add(ui.btnTimKiem);
        pCenter.add(pSearch, BorderLayout.NORTH);

        // Bảng khuyến mãi: thêm cột Số lượng bán và Doanh thu
        ui.tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Mã KM", "Tên khuyến mãi", "Sản phẩm", "Bắt đầu", "Kết thúc", "Số lượng bán", "Doanh thu"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ui.table = new JTable(ui.tableModel);

        JScrollPane scroll = new JScrollPane(ui.table);
        TitledBorder promoBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 90, 200)),
            "Khuyến mãi hiện hành"
        );
        TabStyler.applySectionTitleFont(promoBorder);
        scroll.setBorder(promoBorder);
        pCenter.add(scroll, BorderLayout.CENTER);

        // PHÂN TRANG
        JPanel pPage = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ui.btnPrev = new JButton("< Trang trước");
        ui.btnNext = new JButton("Trang sau >");
        ui.lblPage = new JLabel("Trang 1 / 1");
        pPage.add(ui.btnPrev);
        pPage.add(ui.lblPage);
        pPage.add(ui.btnNext);
        pCenter.add(pPage, BorderLayout.SOUTH);

        // Sự kiện tìm kiếm + phân trang
        ui.btnTimKiem.addActionListener(e -> applyFilter(ui));
        ui.txtTimKiem.addActionListener(e -> applyFilter(ui));

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
                int totalPage = (int) Math.ceil((double) total / rowsPerPage);
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

    // Lọc dữ liệu trong bảng theo text tìm kiếm (lọc client-side trên dữ liệu đã load)
    private void applyFilter(TabUI ui) {
        String keyword = ui.txtTimKiem.getText();
        if (keyword == null) keyword = "";
        keyword = keyword.trim().toLowerCase();

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(ui.tableModel);
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        }
        ui.table.setRowSorter(sorter);
    }


    // Hỏi người dùng muốn xuất những loại báo cáo nào, và chọn khoảng thời gian, rồi tạo 1 file với nhiều sheet
    private void exportMultiSheet() {
        // Panel chọn loại báo cáo + khoảng thời gian
        JCheckBox cbSP = new JCheckBox("Doanh thu sản phẩm", true);
        JCheckBox cbNV = new JCheckBox("Hóa đơn theo nhân viên", true);
        JCheckBox cbKM = new JCheckBox("Thống kê khuyến mãi", true);

        // Dùng khoảng thời gian hiện tại của tab sản phẩm làm giá trị mặc định
        Date defaultTuNgay = uiSanPham.tuNgay.getDate();
        Date defaultDenNgay = uiSanPham.denNgay.getDate();
        if (defaultTuNgay == null) defaultTuNgay = new Date();
        if (defaultDenNgay == null) defaultDenNgay = new Date();

        JDateChooser dcTuNgay = new JDateChooser();
        dcTuNgay.setDateFormatString("dd/MM/yyyy");
        dcTuNgay.setDate(defaultTuNgay);
        JDateChooser dcDenNgay = new JDateChooser();
        dcDenNgay.setDateFormatString("dd/MM/yyyy");
        dcDenNgay.setDate(defaultDenNgay);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Chọn loại báo cáo muốn xuất:"), gbc);

        gbc.gridy++; panel.add(cbSP, gbc);
        gbc.gridy++; panel.add(cbNV, gbc);
        gbc.gridy++; panel.add(cbKM, gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        panel.add(new JLabel("Từ ngày:"), gbc);
        gbc.gridx = 1; panel.add(dcTuNgay, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Đến ngày:"), gbc);
        gbc.gridx = 1; panel.add(dcDenNgay, gbc);

        int option = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Xuất báo cáo",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) return;

        boolean exportSP = cbSP.isSelected();
        boolean exportNV = cbNV.isSelected();
        boolean exportKM = cbKM.isSelected();
        if (!exportSP && !exportNV && !exportKM) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một loại báo cáo để xuất!");
            return;
        }

        Date tuNgay = dcTuNgay.getDate();
        Date denNgay = dcDenNgay.getDate();
        if (tuNgay == null || denNgay == null || tuNgay.after(denNgay)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian hợp lệ!");
            return;
        }

        // Lấy dữ liệu thống kê đầy đủ cho khoảng thời gian đã chọn
        List<Object[]> dataSP = null;
        List<Object[]> dataNV = null;
        List<Object[]> dataKM = null;

        if (exportSP) {
            dataSP = daoThongKe.getTatCaSanPhamBan(tuNgay, denNgay);
        }
        if (exportNV) {
            dataNV = daoThongKe.getTatCaThongKeNhanVien(tuNgay, denNgay);
        }
        if (exportKM) {
            dataKM = daoThongKe.getTatCaKhuyenMaiHienHanh(tuNgay, denNgay);
        }

        // Nếu tất cả danh sách được chọn đều rỗng -> cảnh báo và không xuất
        boolean hasSP = exportSP && dataSP != null && !dataSP.isEmpty();
        boolean hasNV = exportNV && dataNV != null && !dataNV.isEmpty();
        boolean hasKM = exportKM && dataKM != null && !dataKM.isEmpty();
        if (!hasSP && !hasNV && !hasKM) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu nào trong khoảng thời gian đã chọn để xuất!");
            return;
        }

        // Hộp thoại chọn nơi lưu file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");

        String from = sdf.format(tuNgay);
        String to   = sdf.format(denNgay);

        String fileName = "BaoCaoThongKe" + from + "_" + to + ".xlsx";

        fileChooser.setSelectedFile(new File(fileName));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(fileToSave)) {
            if (hasSP) {
                Sheet sheetSP = workbook.createSheet("DoanhThuSanPham");
                Row header = sheetSP.createRow(0);
                header.createCell(0).setCellValue("Mã SP");
                header.createCell(1).setCellValue("Tên SP");
                header.createCell(2).setCellValue("Số lượng bán");
                header.createCell(3).setCellValue("Doanh thu");
                int rowIdx = 1;
                for (Object[] row : dataSP) {
                    Row r = sheetSP.createRow(rowIdx++);
                    r.createCell(0).setCellValue(row[0].toString());
                    r.createCell(1).setCellValue(row[1].toString());
                    r.createCell(2).setCellValue(((Number)row[2]).intValue());
                    r.createCell(3).setCellValue(((Number)row[3]).doubleValue());
                }
                for (int i = 0; i < 4; i++) sheetSP.autoSizeColumn(i);
            }

            if (hasNV) {
                Sheet sheetNV = workbook.createSheet("HoaDonTheoNhanVien");
                Row header = sheetNV.createRow(0);
                header.createCell(0).setCellValue("Mã NV");
                header.createCell(1).setCellValue("Tên NV");
                header.createCell(2).setCellValue("Số hóa đơn");
                header.createCell(3).setCellValue("Doanh thu");
                int rowIdx = 1;
                for (Object[] row : dataNV) {
                    Row r = sheetNV.createRow(rowIdx++);
                    r.createCell(0).setCellValue(row[0].toString());
                    r.createCell(1).setCellValue(row[1].toString());
                    r.createCell(2).setCellValue(((Number)row[2]).intValue());
                    r.createCell(3).setCellValue(((Number)row[3]).doubleValue());
                }
                for (int i = 0; i < 4; i++) sheetNV.autoSizeColumn(i);
            }

            if (hasKM) {
                Sheet sheetKM = workbook.createSheet("KhuyenMaiHienHanh");
                Row header = sheetKM.createRow(0);
                header.createCell(0).setCellValue("Mã KM");
                header.createCell(1).setCellValue("Tên khuyến mãi");
                header.createCell(2).setCellValue("Sản phẩm");
                header.createCell(3).setCellValue("Bắt đầu");
                header.createCell(4).setCellValue("Kết thúc");
                header.createCell(5).setCellValue("Số lượng bán");
                header.createCell(6).setCellValue("Doanh thu");
                int rowIdx = 1;
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
                for (Object[] row : dataKM) {
                    Row r = sheetKM.createRow(rowIdx++);
                    r.createCell(0).setCellValue(((Number)row[0]).intValue());
                    r.createCell(1).setCellValue(row[1].toString());
                    r.createCell(2).setCellValue(row[2].toString());
                    r.createCell(3).setCellValue(row[3] != null ? sdf2.format((Date)row[3]) : "");
                    r.createCell(4).setCellValue(row[4] != null ? sdf2.format((Date)row[4]) : "");
                    r.createCell(5).setCellValue(((Number)row[5]).intValue());
                    r.createCell(6).setCellValue(((Number)row[6]).doubleValue());
                }
                for (int i = 0; i < 7; i++) sheetKM.autoSizeColumn(i);
            }

            workbook.write(out);
            JOptionPane.showMessageDialog(this, "Xuất file Excel thành công:\n" + fileToSave.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel: " + ex.getMessage());
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
        } else if (o == uiSanPham.btnXuat || o == uiHoaDon.btnXuat || o == uiKhuyenMai.btnXuat) {
            // Dù bấm Xuất ở tab nào cũng dùng chung một hộp thoại chọn báo cáo
            exportMultiSheet();
        }
    }

    private void performThongKe(TabUI ui, TabType type) {
        Date tuNgay = ui.tuNgay.getDate();
        Date denNgay = ui.denNgay.getDate();
        if (tuNgay == null || denNgay == null || tuNgay.after(denNgay)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian hợp lệ!");
            return;
        }

        ui.txtTongSP.setText(String.valueOf(daoThongKe.getTongSP(tuNgay, denNgay)));
        ui.txtTongHD.setText(String.valueOf(daoThongKe.getTongHoaDon(tuNgay, denNgay)));
        ui.txtTongDoanhThu.setText(moneyFormat.format(daoThongKe.getTongDoanhThu(tuNgay, denNgay)));

        if (type == TabType.SAN_PHAM) {
            List<Object[]> list = daoThongKe.getSanPhamBanTheoTrang(ui.currentPage, rowsPerPage, tuNgay, denNgay);
            ui.tableModel.setRowCount(0);
            for (Object[] row : list) {
                ui.tableModel.addRow(new Object[]{row[0], row[1], row[2], moneyFormat.format(((Number)row[3]).doubleValue())});
            }
            int total = daoThongKe.getTongSoSanPhamBan(tuNgay, denNgay);
            int totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            ui.lblPage.setText("Trang " + ui.currentPage + " / " + totalPage);
            ui.btnPrev.setEnabled(ui.currentPage > 1);
            ui.btnNext.setEnabled(ui.currentPage < totalPage);

            ui.dataset.clear();
            List<Object[]> top5 = daoThongKe.getTop5SanPham(tuNgay, denNgay);
            for (Object[] row : top5) {
                ui.dataset.addValue(((Number)row[2]).intValue(), "Số lượng", row[1].toString());
            }
        } else if (type == TabType.HOA_DON) {
            List<Object[]> list = daoThongKe.getThongKeNhanVienTheoTrang(ui.currentPage, rowsPerPage, tuNgay, denNgay);
            ui.tableModel.setRowCount(0);
            for (Object[] row : list) {
                ui.tableModel.addRow(new Object[]{row[0], row[1], row[2], moneyFormat.format(((Number)row[3]).doubleValue())});
            }
            int total = daoThongKe.getTongSoNhanVienCoHoaDon(tuNgay, denNgay);
            int totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            ui.lblPage.setText("Trang " + ui.currentPage + " / " + totalPage);
            ui.btnPrev.setEnabled(ui.currentPage > 1);
            ui.btnNext.setEnabled(ui.currentPage < totalPage);
        } else if (type == TabType.KHUYEN_MAI) {
            List<Object[]> list = daoThongKe.getKhuyenMaiHienHanhTheoTrang(ui.currentPage, rowsPerPage, tuNgay, denNgay);
            ui.tableModel.setRowCount(0);
            for (Object[] row : list) {
                ui.tableModel.addRow(new Object[]{
                    row[0], // mã KM
                    row[1], // tên KM
                    row[2], // sản phẩm
                    formatDate((Date)row[3]), // bắt đầu
                    formatDate((Date)row[4]), // kết thúc
                    row[5], // số lượng bán
                    moneyFormat.format(((Number)row[6]).doubleValue()) // doanh thu
                });
            }
            int total = daoThongKe.getTongSoKhuyenMaiHienHanh(tuNgay, denNgay);
            int totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            ui.lblPage.setText("Trang " + ui.currentPage + " / " + totalPage);
            ui.btnPrev.setEnabled(ui.currentPage > 1);
            ui.btnNext.setEnabled(ui.currentPage < totalPage);
        }
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

        pWest.setBorder(TabStyler.createSectionBorder("Thống kê tổng quan"));

        return pWest;
    }

    private static class TabUI {
        JDateChooser tuNgay, denNgay;
        JTextField txtTongSP, txtTongHD, txtTongDoanhThu;
        JTextField txtTimKiem;
        JButton btnThongKe, btnXuat, btnTimKiem;
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
