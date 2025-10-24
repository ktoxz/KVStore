package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class TAB_KhuyenMa extends JFrame {

    private JTextField textMaKhuyenMai;
    private JComboBox<String> cmbLoaiCT;
    private JTextField textTenCT, textDieuKien;
    private JTextField textNgayBatDau, textNgayKetThuc; 
    
    private JButton btnThemDS, btnXoaDS, btnSuaDS, btnTaoMoiDS;
    private JTable tableDSKhuyenMai;
    private DefaultTableModel modelDSKM;

    private JTextField textMaSanPham, textMaKMChiTiet;
    private JTextField textTiLeKM;
    private JTextField textNgayMua;
    
    private JTextField textTimKiemCT; 
    private JButton btnThemCT, btnXoaCT, btnSuaCT;
    private JButton btnTimKiemCT, btnHuyTimKiemCT, btnTrongCT;
    private JTable tableChiTietKM;
    private DefaultTableModel modelChiTietKM;
	private JButton btnLuudulieuCT;


    public TAB_KhuyenMa() {
        setTitle("Quản Lý Khuyến Mãi - Cửa Hàng Tiện Lợi");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel pDanhSach = createDanhSachPanel();
        JPanel pChiTiet = createChiTietPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pDanhSach, pChiTiet);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(5);

        this.add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createDanhSachPanel() {
        JPanel pDanhSach = new JPanel(new BorderLayout(5, 10));
        pDanhSach.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        JPanel pFormVaNut = new JPanel(new BorderLayout(5, 5));

        TitledBorder titleBorderDS = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY), "THÔNG TIN CHƯƠNG TRÌNH",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 24), Color.BLUE);

        JPanel pForm = new JPanel(new GridLayout(6, 2, 10, 8));
        pForm.setBorder(titleBorderDS);

        textMaKhuyenMai = new JTextField();
        
        cmbLoaiCT = new JComboBox<>(new String[]{
            "Giảm % trên Sản phẩm",
            "Giảm Tiền trên Sản phẩm",
            "Giảm % theo Số lượng Sản phẩm",
            "Giảm Tiền theo Số lượng Sản phẩm",
            "Tặng Sản phẩm khi đủ tiền",
            "Giảm % SP khi Hóa đơn đủ tiền",
            "Giảm Tiền SP khi Hóa đơn đủ tiền",
            "Giảm % trên Hóa đơn khi đủ tiền"
        });
        
        textTenCT = new JTextField();
        textDieuKien = new JTextField();
        
        textNgayBatDau = new JTextField(); 
        textNgayKetThuc = new JTextField(); 
        
        pForm.add(new JLabel("Mã khuyến mãi:")); 
        pForm.add(textMaKhuyenMai);
        pForm.add(new JLabel("Loại CT:"));
        pForm.add(cmbLoaiCT);
        pForm.add(new JLabel("Tên CT:"));
        pForm.add(textTenCT);
        pForm.add(new JLabel("Điều kiện:")); 
        pForm.add(textDieuKien);
        
        pForm.add(new JLabel("Ngày bắt đầu (dd/MM/yyyy):"));
        pForm.add(textNgayBatDau); 
        pForm.add(new JLabel("Ngày kết thúc (dd/MM/yyyy):")); 
        pForm.add(textNgayKetThuc);

        pFormVaNut.add(pForm, BorderLayout.NORTH);

        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        btnThemDS = new JButton("Thêm");
        btnThemDS.setBackground(new Color(0, 150, 0)); 
        btnThemDS.setForeground(Color.WHITE);
        
        btnXoaDS = new JButton("Xóa");
        btnXoaDS.setBackground(Color.RED);
        btnXoaDS.setForeground(Color.WHITE);
        
        btnSuaDS = new JButton("Sửa");
        btnSuaDS.setBackground(new Color(0, 100, 200)); 
        btnSuaDS.setForeground(Color.WHITE);
        
        btnTaoMoiDS = new JButton("Tạo mới");
        btnTaoMoiDS.setBackground(new Color(30, 144, 255)); 
        btnTaoMoiDS.setForeground(Color.WHITE);

        pButtons.add(btnThemDS);
        pButtons.add(btnXoaDS);
        pButtons.add(btnSuaDS);
        pButtons.add(btnTaoMoiDS);

        pFormVaNut.add(pButtons, BorderLayout.CENTER);
        pDanhSach.add(pFormVaNut, BorderLayout.NORTH);

        String[] columns = {"Mã KM", "Loại CT", "Tên CT", "Điều kiện", "Ngày BĐ", "Ngày KT"};
        modelDSKM = new DefaultTableModel(columns, 0);

        tableDSKhuyenMai = new JTable(modelDSKM);
        tableDSKhuyenMai.setPreferredScrollableViewportSize(new Dimension(0, 300));
        JScrollPane scrollPaneDSKM = new JScrollPane(tableDSKhuyenMai);

        pDanhSach.add(scrollPaneDSKM, BorderLayout.CENTER);

        return pDanhSach;
    }

    private JPanel createChiTietPanel() {
        JPanel pChiTiet = new JPanel(new BorderLayout(5, 10));
        pChiTiet.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        JPanel pFormVaNut = new JPanel(new BorderLayout(5, 10));

        TitledBorder titleBorderCT = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY), "CHI TIẾT SẢN PHẨM ÁP DỤNG",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 24), Color.BLUE);

        JPanel pForm = new JPanel(new GridLayout(4, 2, 10, 8));
        pForm.setBorder(titleBorderCT);

        textMaSanPham = new JTextField();
        textMaSanPham.setPreferredSize(new Dimension(150, 30));
        textMaKMChiTiet = new JTextField();
        
        textTiLeKM = new JTextField();
        textNgayMua = new JTextField();
        
        pForm.add(new JLabel("Mã Sản Phẩm:"));
        pForm.add(textMaSanPham);

        pForm.add(new JLabel("Mã Khuyến Mãi:"));
        pForm.add(textMaKMChiTiet);

        pForm.add(new JLabel("Tỉ Lệ KM (%):"));
        pForm.add(textTiLeKM);

        pForm.add(new JLabel("Ngày Mua (dd/MM/yyyy):"));
        pForm.add(textNgayMua);


        pFormVaNut.add(pForm, BorderLayout.NORTH);

        // --- PANEL CHỨA CÁC NÚT THAO TÁC CHÍNH ---
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnThemCT = new JButton("Thêm");
        btnThemCT.setBackground(new Color(0, 150, 0)); 
        btnThemCT.setForeground(Color.WHITE);
    
        
        btnXoaCT = new JButton("Xóa");
        btnXoaCT.setBackground(Color.RED);
        btnXoaCT.setForeground(Color.WHITE);
   
        
        btnSuaCT = new JButton("Sửa");
        btnSuaCT.setBackground(new Color(0, 100, 200)); 
        btnSuaCT.setForeground(Color.WHITE);
     
        btnLuudulieuCT = new JButton("Lưu dữ liệu");
        btnLuudulieuCT.setBackground(new Color(30, 144, 255));
        btnLuudulieuCT.setForeground(Color.WHITE);
      
        


        pButtons.add(btnThemCT);
        pButtons.add(btnXoaCT);
        pButtons.add(btnSuaCT);
        pButtons.add(btnLuudulieuCT);
     
        
        // --- PANEL TÌM KIẾM ---
        JPanel pTimKiem = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        textTimKiemCT = new JTextField(8); 
        textTimKiemCT.setPreferredSize(new Dimension(150, 30));
        btnTimKiemCT = new JButton("Tìm Kiếm");
        btnTimKiemCT.setBackground(new Color(255, 165, 0)); 
        btnTimKiemCT.setForeground(Color.WHITE);
        // Bỏ setFont
        
        // Thêm các thành phần tìm kiếm vào pTimKiem
        pTimKiem.add(new JLabel("Nhập mã sản phẩm:"));
        pTimKiem.add(textTimKiemCT);
        pTimKiem.add(btnTimKiemCT);
        
       
        JPanel pNutChucNangTong = new JPanel(new BorderLayout());
        pNutChucNangTong.add(pTimKiem, BorderLayout.WEST);
        pNutChucNangTong.add(pButtons, BorderLayout.EAST);


        pFormVaNut.add(pNutChucNangTong, BorderLayout.CENTER);

        pChiTiet.add(pFormVaNut, BorderLayout.NORTH);

        String[] columnsCT = {"Mã Sản Phẩm", "Mã Khuyến Mãi", "Tỉ Lệ KM (%)", "Ngày Mua"};
        modelChiTietKM = new DefaultTableModel(columnsCT, 0);

        tableChiTietKM = new JTable(modelChiTietKM);
        JScrollPane scrollPaneCTKM = new JScrollPane(tableChiTietKM);

        pChiTiet.add(scrollPaneCTKM, BorderLayout.CENTER);

        return pChiTiet;
    }

    public static void main(String[] args) {
        new TAB_KhuyenMa().setVisible(true);
    }
}
