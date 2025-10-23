package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Date;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;


public class TAB_KhachHang extends JFrame {
	private JTextField txtTenKH;
	private JTextField txtSDT;
	private DefaultTableModel modelNV;
	private JTable tableNV;
	private JButton btnThem;
	private JTextField txtTim;
	private JButton btnSua;
	private JRadioButton radNu;
	private JRadioButton radNam;
	private JButton btnTim;
	private JButton btnXoa;
	private JTextField txtDiemTichLuy;
	private JTextField txtMaKH;

	public TAB_KhachHang() {
		setTitle("Quản lý khách hàng");
		setSize(1200, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		
		//pCenter
		JPanel pCenter = new JPanel(new BorderLayout());
		
			//pLeft: Nhập thông tin KH
		// pLeft: Nhập thông tin KH
		JPanel pLeft = new JPanel();
		Font font = new Font("Arial", Font.BOLD, 20);
		TitledBorder border_left = BorderFactory.createTitledBorder("Thông tin khách hàng");
		border_left.setTitleColor(Color.BLUE);
		border_left.setTitleFont(font);
		pLeft.setBorder(border_left);

		// Tạo các Box
		Box b = Box.createVerticalBox();
		Box b1 = Box.createHorizontalBox();
		Box b2 = Box.createHorizontalBox();
		Box b3 = Box.createHorizontalBox();
		Box b4 = Box.createHorizontalBox();
		Box b5 = Box.createHorizontalBox();
		Box b6 = Box.createHorizontalBox();
		Box b7 = Box.createHorizontalBox();

		// Các thành phần
		txtMaKH = new JTextField();
		txtMaKH.setEditable(false);
		txtMaKH.setFocusable(false);
		txtTenKH = new JTextField();
		radNam = new JRadioButton("Nam", true);
		radNu = new JRadioButton("Nữ");
		ButtonGroup groupPhai = new ButtonGroup();
		groupPhai.add(radNam);
		groupPhai.add(radNu);
		txtSDT = new JTextField();
		txtDiemTichLuy = new JTextField();
		txtDiemTichLuy.setEditable(false);
		txtDiemTichLuy.setFocusable(false);

		// Spinner chọn ngày
		JDateChooser dateChooser = new JDateChooser();
		dateChooser.setDateFormatString("dd/MM/yyyy");
		dateChooser.setDate(new Date());
		dateChooser.setPreferredSize(new Dimension(150, 25));

		// Nút
		btnThem = new JButton("Thêm");
		btnSua = new JButton("Sửa thông tin");
		btnXoa = new JButton("Xóa");

		// Làm cho 3 nút có cùng kích thước
		Dimension sizeBtn = new Dimension(130, 30);
		btnThem.setPreferredSize(sizeBtn);
		btnSua.setPreferredSize(sizeBtn);
		btnXoa.setPreferredSize(sizeBtn);

		// Kích thước text field
		Dimension sizeTxt = new Dimension(180, 25);
		txtMaKH.setPreferredSize(sizeTxt);
		txtTenKH.setPreferredSize(sizeTxt);
		txtSDT.setPreferredSize(sizeTxt);
		txtDiemTichLuy.setPreferredSize(sizeTxt);

		// Bổ sung độ giãn cho căn đều
		int labelWidth = 120;
		
		// Hàn 1: Mã 
		JLabel lblMa = new JLabel("Mã khách hàng: ");
		lblMa.setPreferredSize(new Dimension(labelWidth, 25));
		b1.add(lblMa);
		b1.add(txtMaKH);
		
		// Hàng 1: Họ tên
		JLabel lblTen = new JLabel("Họ tên khách hàng: ");
		lblTen.setPreferredSize(new Dimension(labelWidth, 25));
		b2.add(lblTen);
		b2.add(txtTenKH);

		// Hàng 2: Giới tính
		JLabel lblGioiTinh = new JLabel("Giới tính: ");
		lblGioiTinh.setPreferredSize(new Dimension(labelWidth, 25));
		b3.add(lblGioiTinh);
		Box boxPhai = Box.createHorizontalBox();
		boxPhai.add(radNam);
		boxPhai.add(Box.createHorizontalStrut(10));
		boxPhai.add(radNu);
		b3.add(boxPhai);
		b3.add(Box.createHorizontalGlue());


		// Hàng 3: SĐT
		JLabel lblSDT = new JLabel("Số điện thoại: ");
		lblSDT.setPreferredSize(new Dimension(labelWidth, 25));
		b4.add(lblSDT);
		b4.add(txtSDT);

		// Hàng 4: Ngày tạo
		JLabel lblNgayTao = new JLabel("Ngày tạo: ");
		lblNgayTao.setPreferredSize(new Dimension(labelWidth, 25));
		b5.add(lblNgayTao);
		b5.add(dateChooser);
		
		// Hàng 5: Điểm tích lũy
		JLabel lblDiemTichLuy = new JLabel("Điểm tích lũy: ");
		lblDiemTichLuy.setPreferredSize(new Dimension(labelWidth, 25));
		b6.add(lblDiemTichLuy);
		b6.add(txtDiemTichLuy);
		

		// Các nút — căn giữa, đều nhau
		b7.add(Box.createHorizontalGlue());
		b7.add(btnThem);
		b7.add(Box.createHorizontalStrut(10));
		b7.add(btnSua);
		b7.add(Box.createHorizontalStrut(10));
		b7.add(btnXoa);
		b7.add(Box.createHorizontalGlue());

		// Thêm khoảng cách giữa các dòng
		b.add(b1);
		b.add(Box.createVerticalStrut(8));
		b.add(b2);
		b.add(Box.createVerticalStrut(8));
		b.add(b3);
		b.add(Box.createVerticalStrut(8));
		b.add(b4);
		b.add(Box.createVerticalStrut(12));
		b.add(b5);
		b.add(Box.createVerticalStrut(12));
		b.add(b6);
		b.add(Box.createVerticalStrut(12));
		b.add(b7);

		pLeft.add(b, BorderLayout.NORTH);
		
			//pRight: Table
		// Panel bên phải
		JPanel pRight = new JPanel(new BorderLayout());
		TitledBorder border_right = BorderFactory.createTitledBorder("Danh sách khách hàng");
		border_right.setTitleColor(Color.BLUE);
		border_right.setTitleFont(font);
		pRight.setBorder(border_right);

		// Tạo panel tìm kiếm (lblTimKiem + txtTim)
		JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		JLabel lblTimKiem = new JLabel("Tìm kiếm khách hàng:");
		lblTimKiem.setPreferredSize(new Dimension(150, 25));
		JTextField txtTim = new JTextField(20); 
		txtTim.setPreferredSize(sizeTxt);
		btnTim = new JButton("Tìm");

		pSearch.add(lblTimKiem);
		pSearch.add(txtTim);
		pSearch.add(btnTim);

		// Tạo bảng
		String[] header = {"Mã khách hàng", "Họ tên", "Số điện thoại", "Điểm tích lũy", "Ngày tạo"};
		DefaultTableModel modelNV = new DefaultTableModel(header, 0);
		JTable tableNV = new JTable(modelNV);
		tableNV.getTableHeader().setBorder(BorderFactory.createRaisedBevelBorder());
		tableNV.getTableHeader().setBackground(Color.LIGHT_GRAY);
		tableNV.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

		JScrollPane paneNV = new JScrollPane(tableNV);

		// Thêm vào pRight
		pRight.add(pSearch, BorderLayout.NORTH);
		pRight.add(paneNV, BorderLayout.CENTER);

		// Gắn vào giao diện chính
		pCenter.add(pLeft, BorderLayout.WEST);
		pCenter.add(pRight, BorderLayout.CENTER);
		add(pCenter, BorderLayout.CENTER);

		
		//PSouth
		JPanel pSouth = new JPanel();
		
		add(pSouth, BorderLayout.SOUTH);
	}
	
	public static void main(String[] args) {
		new TAB_KhachHang().setVisible(true);
	}
}
