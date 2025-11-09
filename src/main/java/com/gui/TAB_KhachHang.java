package com.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.connectDB.ConnectDB;
import com.dao.DAO_KhachHang;
import com.entity.KhachHang;
import com.toedter.calendar.JDateChooser;

public class TAB_KhachHang extends JPanel implements ActionListener, MouseListener{

    private JTextField txtMaKH, txtTenKH, txtSDT, txtDiemTichLuy, txtTim;
    private JRadioButton radNam, radNu;
    private JDateChooser dateChooser;
    private JButton btnThem, btnSua, btnTim;
    private JTable tableKH;
    private DefaultTableModel modelKH;
    
    private DAO_KhachHang kh_dao;

    public TAB_KhachHang() {
    	// Khởi tạo kết nối đến CSDL
    	try {
			ConnectDB.getInstance().connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	kh_dao = new DAO_KhachHang();
    	
    	setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 90, 200));

        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeader.setBackground(Color.WHITE);
        pHeader.add(lblTitle);
        add(pHeader, BorderLayout.NORTH);

        // ===== Center Panel =====
        JPanel pCenter = new JPanel(new BorderLayout());

        // ===== LEFT PANEL (Form nhập) =====
        JPanel pLeft = new JPanel();
        Font font = new Font("Arial", Font.BOLD, 18);

        TitledBorder border_left = BorderFactory.createTitledBorder("Thông tin khách hàng");
        border_left.setTitleColor(Color.BLUE);
        border_left.setTitleFont(font);
        pLeft.setBorder(border_left);

        Box box = Box.createVerticalBox();

        int labelWidth = 120;
        Dimension sizeTxt = new Dimension(180, 25);
        Dimension sizeBtn = new Dimension(130, 30);

        // Mã KH
        Box b1 = Box.createHorizontalBox();
        JLabel lblMa = new JLabel("Mã khách hàng:");
        lblMa.setPreferredSize(new Dimension(labelWidth, 25));
        txtMaKH = new JTextField();
        txtMaKH.setPreferredSize(sizeTxt);
        txtMaKH.setEditable(false);
        b1.add(lblMa); b1.add(txtMaKH);

        // Tên KH
        Box b2 = Box.createHorizontalBox();
        JLabel lblTen = new JLabel("Họ tên:");
        lblTen.setPreferredSize(new Dimension(labelWidth, 25));
        txtTenKH = new JTextField();
        txtTenKH.setPreferredSize(sizeTxt);
        b2.add(lblTen); b2.add(txtTenKH);

        // Giới tính
        Box b3 = Box.createHorizontalBox();
        JLabel lblGT = new JLabel("Giới tính:");
        lblGT.setPreferredSize(new Dimension(labelWidth, 25));

        radNam = new JRadioButton("Nam", true); // 0-> Nam (false)
        radNu = new JRadioButton("Nữ"); // 1 -> Nữ 9 (true)
        ButtonGroup groupGT = new ButtonGroup();
        groupGT.add(radNam); groupGT.add(radNu);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setPreferredSize(new Dimension(180, 25)); // Giống kích thước txt
        genderPanel.add(radNam);
        genderPanel.add(radNu);

        b3.add(lblGT);
        b3.add(genderPanel);

        // SĐT
        Box b4 = Box.createHorizontalBox();
        JLabel lblSDT = new JLabel("SĐT:");
        lblSDT.setPreferredSize(new Dimension(labelWidth, 25));
        txtSDT = new JTextField();
        txtSDT.setPreferredSize(sizeTxt);
        b4.add(lblSDT); b4.add(txtSDT);

        // Ngày tạo
        Box b5 = Box.createHorizontalBox();
        JLabel lblNgay = new JLabel("Ngày tạo:");
        lblNgay.setPreferredSize(new Dimension(labelWidth, 25));
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setDate(new Date());
        dateChooser.setPreferredSize(sizeTxt);
        b5.add(lblNgay); b5.add(dateChooser);


        // Điểm tích lũy
        Box b6 = Box.createHorizontalBox();
        JLabel lblDiem = new JLabel("Điểm tích lũy:");
        lblDiem.setPreferredSize(new Dimension(labelWidth, 25));
        txtDiemTichLuy = new JTextField();
        txtDiemTichLuy.setPreferredSize(sizeTxt);
        txtDiemTichLuy.setEditable(false);
        b6.add(lblDiem); b6.add(txtDiemTichLuy);

        // Buttons
        Color textColor = Color.WHITE; 
        btnThem = new JButton("Thêm");
        btnThem.setBackground(Color.BLUE);
        btnThem.setForeground(textColor);
        btnThem.setPreferredSize(sizeBtn);
        
        btnSua = new JButton("Sửa");
        btnSua.setBackground(Color.ORANGE);
        btnSua.setForeground(textColor);
        btnSua.setPreferredSize(sizeBtn);

        Box bBtns = Box.createHorizontalBox();
        bBtns.add(btnThem);
        bBtns.add(Box.createHorizontalStrut(10));
        bBtns.add(btnSua);

        // Add to vertical box
        box.add(b1); box.add(Box.createVerticalStrut(8));
        box.add(b2); box.add(Box.createVerticalStrut(8));
        box.add(b3); box.add(Box.createVerticalStrut(8));
        box.add(b4); box.add(Box.createVerticalStrut(8));
        box.add(b5); box.add(Box.createVerticalStrut(8));
        box.add(b6); box.add(Box.createVerticalStrut(12));
        box.add(bBtns);

        pLeft.add(box, BorderLayout.NORTH);

        // ===== RIGHT PANEL (Table + search) =====
        JPanel pRight = new JPanel(new BorderLayout());
        TitledBorder border_right = BorderFactory.createTitledBorder("Danh sách khách hàng");
        border_right.setTitleColor(Color.BLUE);
        border_right.setTitleFont(font);
        pRight.setBorder(border_right);

        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblTim = new JLabel("Tìm khách bằng số điện thoại:");
        lblTim.setPreferredSize(new Dimension(180, 25));
        txtTim = new JTextField(20);
        btnTim = new JButton("Tìm");

        pSearch.add(lblTim);
        pSearch.add(txtTim);
        pSearch.add(btnTim);

        String[] header = {"Mã KH", "Tên KH", "Giới tính", "SĐT", "Ngày tạo", "Điểm tích lũy"};
        modelKH = new DefaultTableModel(header, 0);
        tableKH = new JTable(modelKH); // lon ten
        JScrollPane pane = new JScrollPane(tableKH);

        pRight.add(pSearch, BorderLayout.NORTH);
        pRight.add(pane, BorderLayout.CENTER);

        // Add to center
        pCenter.add(pLeft, BorderLayout.WEST);
        pCenter.add(pRight, BorderLayout.CENTER);

        add(pCenter, BorderLayout.CENTER);
        
        // Đọc dữ liệu
        DocDuLieuVaoDatabase();
        
        // Sự kiện
        btnThem.addActionListener(this);
        btnSua.addActionListener(this);
        btnTim.addActionListener(this);
        tableKH.addMouseListener(this);
    }
    
    public void DocDuLieuVaoDatabase() {
    	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    	List<KhachHang> list = kh_dao.getAllKhachHang();
		for (KhachHang khachHang : list) {
			modelKH.addRow(new Object[] {khachHang.getMaKH(),
											khachHang.getTenKH(),
											khachHang.isGioiTinh()?"Nữ":"Nam",
											khachHang.getSdt(),
											khachHang.getNgayTao().format(fmt),
											khachHang.getDiemTichLuy()});
		}
	}
    
    // Kiểm tra dữ liệu nhập
    public boolean ValidDate() {
		String tenKH = txtTenKH.getText().trim();
		String sdt = txtSDT.getText().trim();
		LocalDate ngayTao = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		
		//Kiểm tra tên KH
    	if(tenKH.length()>0) {
    		if (!tenKH.matches("([\\p{Lu}][\\p{Ll}]+)( [\\p{Lu}][\\p{Ll}]+)*")) {
    			JOptionPane.showMessageDialog(this, "Tên khách hàng chưa nhập đúng định dạng!");
    		    txtTenKH.requestFocus();
    		    return false;
    		}
    	}else {
    		JOptionPane.showMessageDialog(this, "Phải nhập tên khách hàng!");
    		txtTenKH.requestFocus();
    		return false;
    	}
    	
    	if (tenKH.length() > 100) {
    	    JOptionPane.showMessageDialog(this, "Tên khách hàng không được vượt quá 100 ký tự!");
    	    txtTenKH.requestFocus();
    	    return false;
    	}
    	
    	// Kiểm tra số điện thoại
    	if(sdt.length()>0) {
    		if(!sdt.matches("[0-9]{10}")) {
    			JOptionPane.showMessageDialog(this, "Số điện thoại không đủ số");
    		    txtTenKH.requestFocus();
    		    return false;
    		}
    	}else {
    		JOptionPane.showMessageDialog(this, "Phải nhập số điện thoại!");
    		txtTenKH.requestFocus();
    		return false;
    	}
    	
    	// Kiểm tra ngày đăng ký
        if (ngayTao.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày tạo không được lớn hơn ngày hiện tại!");
            return false;
        }
		
    	return true;
	}
    
    // Lấy dữ liệu từ form
    public KhachHang revertKHFromTextfields() {
        String maKH = txtMaKH.getText().trim();
        String tenKH = txtTenKH.getText().trim();
        boolean gioiTinh = radNu.isSelected(); 
        String sdt = txtSDT.getText().trim();

        // Lấy ngày từ JDateChooser
        Date date = dateChooser.getDate();
        LocalDate ngayTaoTK = null;
        if (date != null) {
            ngayTaoTK = date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
        }

        int diemTichLuy = 0;
    	return new KhachHang(maKH, tenKH, gioiTinh, sdt, ngayTaoTK, diemTichLuy);
	}
    
    @Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o.equals(btnThem)) {
			if(ValidDate()) {
				// Phát sinh mã Khách Hàng
				String maKh = kh_dao.phatSinhMaKH();
				txtMaKH.setText(maKh);
				
				KhachHang kh = revertKHFromTextfields();
				
				kh.setDiemTichLuy(0);
				
				if (kh_dao.themKH(kh)) {
                    modelKH.addRow(new Object[] {
                        kh.getMaKH(),
                        kh.getTenKH(),
                        kh.isGioiTinh() ? "Nữ" : "Nam",  // đổi boolean -> text
                        kh.getSdt(),
                        kh.getNgayTao(),
                        kh.getDiemTichLuy()
                    });
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                }
			}
		}
		
		// Sửa thông tin
		if(o.equals(btnSua)) {
			if(ValidDate()) {
				KhachHang kh = revertKHFromTextfields();
				
				if(kh_dao.capNhatKH(kh)) {
					JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!");
					
					//Cập nhật lại dữ liệu trên JTable
					int row = tableKH.getSelectedRow();
					if(row >= 0) {
						modelKH.setValueAt(kh.getTenKH(), row,1);
						modelKH.setValueAt(kh.isGioiTinh() ? "Nữ" : "Nam", row, 2);
	                    modelKH.setValueAt(kh.getDiemTichLuy(), row, 5);
					}
				}else {
					JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
				}
			}
		}
		
		if(o.equals(btnTim)) {
			String timSDT = txtTim.getText().trim();
			if(timSDT.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại cần tìm!");
				return;
			}
			
			KhachHang kh = kh_dao.timKiemKH(timSDT);
			
			if(kh == null) {
				JOptionPane.showMessageDialog(this, "Khách hàng chưa được tạo!");
				return;
			}
			
			// ✅ 1. Chọn hàng tương ứng trong bảng (nếu có)
		    int rowCount = modelKH.getRowCount();
		    boolean found = false;
		    for (int i = 0; i < rowCount; i++) {
		        String sdtTable = modelKH.getValueAt(i, 3).toString();
		        if (sdtTable.equalsIgnoreCase(timSDT)) {
		            tableKH.setRowSelectionInterval(i, i); // chọn hàng đó
		            tableKH.scrollRectToVisible(tableKH.getCellRect(i, 0, true)); // cuộn tới hàng
		            found = true;
		            break;
		        }
		    }
		    
		    txtMaKH.setText(kh.getMaKH());
		    txtTenKH.setText(kh.getTenKH());
		    if(kh.isGioiTinh()) {
		    	radNu.setSelected(true);
		    }else {
		    	radNam.setSelected(true);
		    }
		    txtSDT.setText(kh.getSdt());
			txtDiemTichLuy.setText(String.valueOf(kh.getDiemTichLuy()));
			dateChooser.setDate(java.sql.Date.valueOf(kh.getNgayTao()));
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = tableKH.getSelectedRow();
		txtMaKH.setText(modelKH.getValueAt(row, 0).toString());
		txtTenKH.setText(modelKH.getValueAt(row, 1).toString());
		String gioiTinh = modelKH.getValueAt(row, 2).toString();
	    if ("Nam".equalsIgnoreCase(gioiTinh)) {
	        radNam.setSelected(true);
	    } else if ("Nữ".equalsIgnoreCase(gioiTinh)) {
	        radNu.setSelected(true);
	    }
		txtSDT.setText(modelKH.getValueAt(row, 3).toString());
		String ngayTaoStr = modelKH.getValueAt(row, 4).toString();
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	        Date ngayTao = sdf.parse(ngayTaoStr);
	        dateChooser.setDate(ngayTao); 
	    } catch (ParseException ex) {
	        ex.printStackTrace();
	        dateChooser.setDate(new Date());
	    }
		txtDiemTichLuy.setText(modelKH.getValueAt(row, 5).toString());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
    
    
}
