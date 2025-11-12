package com.gui;

import java.awt.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class TAB_ThongKe extends JPanel {
    private JDateChooser dateTuNgay;
    private JDateChooser dateDenNgay;
    private JTextField txtTongSP;
    private JTextField txtTongDoanhThu;
    private JTextField txtTongHD;

    public TAB_ThongKe() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ====== Header ======
        JLabel lblTitle = new JLabel("Thống kê doanh thu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 90, 200));

        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeader.setBackground(Color.WHITE);
        pHeader.add(lblTitle);
        add(pHeader, BorderLayout.NORTH);

        // ====== Panel West (Tổng quan) ======
        JPanel pWest = new JPanel(new GridBagLayout());
        pWest.setBackground(Color.WHITE);
        Font font = new Font("Arial", Font.PLAIN, 16);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;

        // Các trường thống kê
        JLabel lblTuNgay = new JLabel("Từ ngày:");
        lblTuNgay.setFont(font);
        dateTuNgay = new JDateChooser();
        dateTuNgay.setDateFormatString("dd/MM/yyyy");
        dateTuNgay.setDate(new Date());

        gbc.gridx = 0; gbc.gridy = 0;
        pWest.add(lblTuNgay, gbc);
        gbc.gridx = 1;
        pWest.add(dateTuNgay, gbc);

        JLabel lblDenNgay = new JLabel("Đến ngày:");
        lblDenNgay.setFont(font);
        dateDenNgay = new JDateChooser();
        dateDenNgay.setDateFormatString("dd/MM/yyyy");
        dateDenNgay.setDate(new Date());

        gbc.gridx = 0; gbc.gridy = 1;
        pWest.add(lblDenNgay, gbc);
        gbc.gridx = 1;
        pWest.add(dateDenNgay, gbc);

        JLabel lblTongSP = new JLabel("Tổng sản phẩm:");
        lblTongSP.setFont(font);
        txtTongSP = new JTextField(10);
        txtTongSP.setEditable(false);
        gbc.gridx = 0; gbc.gridy = 2;
        pWest.add(lblTongSP, gbc);
        gbc.gridx = 1;
        pWest.add(txtTongSP, gbc);

        JLabel lblTongHD = new JLabel("Tổng số hóa đơn:");
        lblTongHD.setFont(font);
        txtTongHD = new JTextField(10);
        txtTongHD.setEditable(false);
        gbc.gridx = 0; gbc.gridy = 3;
        pWest.add(lblTongHD, gbc);
        gbc.gridx = 1;
        pWest.add(txtTongHD, gbc);

        JLabel lblTongDoanhThu = new JLabel("Tổng doanh thu:");
        lblTongDoanhThu.setFont(font);
        txtTongDoanhThu = new JTextField(10);
        txtTongDoanhThu.setEditable(false);
        gbc.gridx = 0; gbc.gridy = 4;
        pWest.add(lblTongDoanhThu, gbc);
        gbc.gridx = 1;
        pWest.add(txtTongDoanhThu, gbc);

        // Viền
        TitledBorder border_left = BorderFactory.createTitledBorder("Thống kê tổng quan");
        border_left.setTitleColor(new Color(0, 90, 200));
        border_left.setTitleFont(new Font("Arial", Font.BOLD, 18));
        pWest.setBorder(border_left);

        add(pWest, BorderLayout.WEST);

        // ====== Panel Center ======
        JPanel pCenter = new JPanel(new BorderLayout(10, 10));
        pCenter.setBackground(Color.WHITE);

        // Phần trên: Biểu đồ + bảng sản phẩm
        JPanel pTop = new JPanel(new GridLayout(1, 2, 10, 10));
        pTop.setBackground(Color.WHITE);

        // --- Biểu đồ top 5 sản phẩm bán chạy ---
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] productNames = {"Sữa tươi", "Bánh quy", "Mì ly", "Nước ngọt", "Dầu ăn"};
        int[] soldQuantities = {120, 90, 75, 60, 50};
        for (int i = 0; i < productNames.length; i++) {
            dataset.addValue(soldQuantities[i], "Số lượng bán", productNames[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 sản phẩm bán chạy",
                "Sản phẩm",
                "Số lượng",
                dataset
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu đồ top 5 sản phẩm"));
        pTop.add(chartPanel);

        // --- Bảng doanh thu từng sản phẩm ---
        String[] columnsSP = {"Mã SP", "Tên sản phẩm", "Số lượng", "Doanh thu"};
        Object[][] dataSP = {
                {"SP01", "Sữa tươi", 120, "12.000.000"},
                {"SP02", "Bánh quy", 90, "9.000.000"},
                {"SP03", "Mì ly", 75, "6.000.000"},
                {"SP04", "Nước ngọt", 60, "4.800.000"},
                {"SP05", "Dầu ăn", 50, "5.500.000"}
        };
        JTable tableSP = new JTable(dataSP, columnsSP);
        JScrollPane scrollSP = new JScrollPane(tableSP);
        scrollSP.setBorder(BorderFactory.createTitledBorder("Doanh thu theo sản phẩm"));
        pTop.add(scrollSP);

        // Phần dưới: Thống kê hóa đơn theo nhân viên
        String[] columnsNV = {"Mã NV", "Tên nhân viên", "Tổng hóa đơn", "Doanh thu"};
        Object[][] dataNV = {
                {"NV01", "Nguyễn Văn A", 25, "45.000.000"},
                {"NV02", "Trần Thị B", 20, "30.000.000"},
                {"NV03", "Lê Văn C", 15, "22.000.000"},
                {"NV04", "Phạm Thị D", 12, "18.000.000"}
        };
        JTable tableNV = new JTable(dataNV, columnsNV);
        JScrollPane scrollNV = new JScrollPane(tableNV);
        scrollNV.setBorder(BorderFactory.createTitledBorder("Thống kê hóa đơn theo nhân viên"));

        // Tách pTop và pBottom bằng JSplitPane (hoặc BorderLayout)
        JPanel pBottom = new JPanel(new BorderLayout());
        pBottom.add(scrollNV, BorderLayout.CENTER);

        // Gộp pTop + pBottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pTop, pBottom);
        splitPane.setResizeWeight(0.5); // 50% - 50%
        splitPane.setDividerSize(8);
        splitPane.setBackground(Color.WHITE);
        pCenter.add(splitPane, BorderLayout.CENTER);

        add(pCenter, BorderLayout.CENTER);
    }
}
