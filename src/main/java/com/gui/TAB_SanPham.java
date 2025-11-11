package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.dao.DAO_SanPham;
import com.entity.SanPham;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;


public class TAB_SanPham extends JPanel implements ActionListener, MouseListener {
	
	DAO_SanPham dao;

	// Theme
	static final Color
			CLR_PRIMARY = new Color(33, 150, 243),
			CLR_WARNING = new Color(255, 193, 7),
			CLR_DANGER = new Color(244, 67, 54), 
			CLR_MUTED = new Color(158, 158, 158), 
			CLR_TEXT_LIGHT = Color.WHITE,
			CLR_TEXT_DARK = Color.BLACK;

	// UI
	JTextField txtSearch, txtMa, txtTen, txtGia, txtPathAnh, txtLoai;
	JTextArea txtMoTa;
	JCheckBox chkActive;
	JButton btnThem, btnLuu, btnXoa, btnXoaTrang, btnTim, btnChonAnh, btnXoaAnh;
	JTable tbl;
	DefaultTableModel mdl;
	JLabel lblPreview;
	
	// Paging (SQL-based)
	int pageSize = 10, currentPage = 1, totalPages = 1;
	JPanel pnlPaging;
	JLabel lbPageInfo;

	
	// Sizes
	static final int THUMB_W = 61, THUMB_H = 61, PREVIEW_W = 400, PREVIEW_H = 240, FORM_FIELD_W = 240, BTN_H = 32;
	
	// Img Dir
	private static final String IMG_DIR = "src/main/resources/sp_image"; 
	
	//data
	List<SanPham> dsAll = new ArrayList<>();


	public TAB_SanPham() {
		
		dao = new DAO_SanPham();
		
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(buildNorthSearch(), BorderLayout.NORTH);
		
		JComponent cenTab = buildCenterTable();
		JComponent eastForm = buildEastForm();
		
		cenTab.setBorder( createTitleBorder( "Danh sách sản phẩm", new Color(30,144,255), 20f, 1 ) );
		eastForm.setBorder( createTitleBorder( "Thông tin sản phẩm", new Color(30,144,255), 20f, 1 ) );

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cenTab, eastForm);
		split.setResizeWeight(0.70);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(false);
		split.setDividerLocation( 0.70 );
		split.setEnabled(false);      // khoá kéo bằng chuột
		split.setDividerSize(0);      // ẩn/tháo “tay nắm” kéo
		split.setFocusable(false);    // tránh điều khiển bằng phím
	
		
		add(split, BorderLayout.CENTER);
		
		setTable(dao.getAllSanPham());
		bindEvents();
		setFormModeNew();
		
		setBorder( createTitleBorder("QUẢN LÝ SẢN PHẨM", new Color(0,102,204), 22f, 0) );

	}
	
	private void setTable(List<SanPham> ds) {
		currentPage = 1;
		dsAll = ds;
		reloadTable();
	}
	
	// tải bảng và dữ liệu
	//	maSP, tenSP, giaSP, moTaSP, hinhAnhSP, tinhTrangSP, loaiSP
	private void loadTable(List<SanPham> ds) {
	    mdl.setRowCount(0);
	    for (SanPham sp : ds) {
	    	
//	    	Path p = Paths.get(sp.getHinhAnhSP());
//	    	if (!p.isAbsolute()) p = Paths.get(IMG_DIR).resolve(p);
	    	
	        ImageIcon icon = scaledOrPlaceholder(sp.getHinhAnhSP(), THUMB_W, THUMB_H);
	        String giaStr = formatGia(sp.getGiaSP()); // đổi sang String cho khớp getColumnClass
	        mdl.addRow(new Object[]{
	            icon,                  // 0: Ảnh (ImageIcon)
	            sp.getMaSP(),          // 1: Mã
	            sp.getTenSP(),         // 2: Tên
	            sp.getMoTaSP(),        // 3: Mô tả
	            giaStr,                // 4: Giá (String)
	            sp.getLoaiSP(),        // 5: Loại
	            sp.isTinhTrangSP() ,   // 6: Hoạt động (Boolean)
	        });
	    }
	}
	
	//tải trang hiên tại
	private void reloadTable() {
	    if (dsAll == null) dsAll = dao.getAllSanPham();

	    totalPages = Math.max(1, (int) Math.ceil(dsAll.size() / (double) pageSize));
	    if (currentPage < 1) currentPage = 1;
	    if (currentPage > totalPages) currentPage = totalPages;

	    int from = (currentPage - 1) * pageSize;
	    int to   = Math.min(from + pageSize, dsAll.size());
	    if (from < 0) from = 0;
	    if (to < from) to = from;

	    loadTable(dsAll.subList(from, to));
	    rebuildPaging(dsAll.size());
	}


	private void rebuildPaging(int total) {
	    if (pnlPaging == null) return;
	    pnlPaging.removeAll();

	    int window = 7;
	    int start = Math.max(1, currentPage - window/2);
	    int end   = Math.min(totalPages, start + window - 1);
	    start = Math.max(1, end - window + 1);

	    pnlPaging.add(pageBtn("<<", 1, currentPage > 1, false));
	    pnlPaging.add(pageBtn("<", currentPage - 1, currentPage > 1, false));
	    for (int p = start; p <= end; p++) {
	        pnlPaging.add(pageBtn(String.valueOf(p), p, true, p == currentPage));
	    }
	    pnlPaging.add(pageBtn(">", currentPage + 1, currentPage < totalPages, false));
	    pnlPaging.add(pageBtn(">>", totalPages, currentPage < totalPages, false));

	    lbPageInfo.setText("Trang " + currentPage + "/" + totalPages + " • " + total + " mục");
	    lbPageInfo.setBorder(new EmptyBorder(0, 12, 0, 0));
	    pnlPaging.add(lbPageInfo);

	    pnlPaging.revalidate();
	    pnlPaging.repaint();
	}

	private JButton pageBtn(String text, int target, boolean enabled, boolean active) {
	    JButton b = new JButton(text);
	    b.setEnabled(enabled);
	    b.setPreferredSize(new Dimension(42, BTN_H));
	    if (active) {
	        b.setBackground(CLR_PRIMARY);
	        b.setForeground(CLR_TEXT_LIGHT);
	    }
	    b.addActionListener(e -> { currentPage = target; reloadTable(); });
	    return b;
	}

	
	private String nextIdFromDB() {
	    int next = 1;
	    for (SanPham sp : dao.getAllSanPham()) {
	        String id = sp.getMaSP();
	        if (id != null && id.matches("SP\\d+")) {
	            int n = Integer.parseInt(id.replaceAll("\\D+", ""));
	            if (n >= next) next = n + 1;
	        }
	    }
	    return String.format("SP%03d", next);
	}

	
	
	// build các tiểu cấu trúc
	// ví dụ gọi: setBorder(createTitleBorder("QUẢN LÝ SẢN PHẨM", new Color(0,102,204), 22f, 2));
	private static Border createTitleBorder(String title, Color titleColor, float fontSizePt, int lineThickness) {
	    Font base = UIManager.getFont("Label.font");
	    if (base == null) base = new Font("SansSerif", Font.PLAIN, 14);

	    return new CompoundBorder(
	        new EmptyBorder(8, 8, 8, 8), // mép ngoài
	        new CompoundBorder(
	            new TitledBorder(
	                new LineBorder(new Color(200, 200, 200), lineThickness, true), // độ dày viền
	                title,
	                TitledBorder.LEFT, TitledBorder.TOP,
	                base.deriveFont(Font.BOLD, fontSizePt), // cỡ chữ
	                titleColor                               // màu chữ
	            ),
	            new EmptyBorder(10, 12, 12, 12) // padding nội dung
	        )
	    );
	}


	private JComponent buildNorthSearch() {
		
		JPanel p = new JPanel(new BorderLayout(8, 0));
		txtSearch = new JTextField();
		btnTim = new JButton("Tìm");
		
		styleButton(btnTim, CLR_PRIMARY, CLR_TEXT_LIGHT);
		p.add(new JLabel("Tìm kiếm:"), BorderLayout.WEST);
		p.add(txtSearch, BorderLayout.CENTER);
		p.add(btnTim, BorderLayout.EAST);
		
		return p;
	}

	private JComponent buildCenterTable() {
		String[] cols = { "Ảnh", "Mã", "Tên sản phẩm", "Mô tả", "Giá", "Loại", "Hoạt động" };
		mdl = new DefaultTableModel(cols, 0) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int r, int c) {
				return false;
			}

			public Class<?> getColumnClass(int c) {
				if (c == 0)
					return ImageIcon.class; // Ảnh
				if (c == 6)
					return Boolean.class; // Hoạt động
				return String.class; // các cột còn lại
			}
		};
		

		
		tbl = new JTable(mdl);
		tbl.setRowHeight(Math.max(THUMB_H + 8, 28));
		
	    TableColumn c6 = tbl.getColumnModel().getColumn(6);
	    c6.setCellRenderer(new DefaultTableCellRenderer() {
	      /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		  @Override public Component getTableCellRendererComponent(
	          JTable t,Object v,boolean sel,boolean focus,int r,int c) {
	        JLabel L = (JLabel) super.getTableCellRendererComponent(t, "", sel, focus, r, c);
	        boolean on = Boolean.TRUE.equals(v);
	        L.setText(on ? "Đang hoạt động" : "Ngừng");
	        if (!sel) {
	          L.setBackground(on ? new Color(220,248,231) : new Color(255,235,238));
	          L.setForeground(on ? new Color(24,121,78)   : new Color(183,28,28));
	        }
	        L.setHorizontalAlignment(SwingConstants.CENTER);
	        L.setOpaque(true);
	        return L;
	      }
	    });
		
		tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl.addMouseListener(this);
		JScrollPane sp = new JScrollPane(tbl);
		sp.setPreferredSize(new Dimension(700, 500));
		TableColumnModel tcm = tbl.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(THUMB_W + 16);
		tcm.getColumn(0).setMaxWidth(THUMB_W + 24);
		
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.add(sp, BorderLayout.CENTER);

		pnlPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
		pnlPaging.setBorder(new EmptyBorder(6, 0, 0, 0));
		lbPageInfo = new JLabel();
		wrap.add(pnlPaging, BorderLayout.SOUTH);

		return wrap;

	}

	private JComponent buildEastForm() {
		JPanel p = new JPanel(new GridBagLayout());
		p.setPreferredSize(new Dimension(520, 540));
		p.setMaximumSize(p.getPreferredSize());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		// --- Mã
		txtMa = new JTextField();
		txtMa.setEditable(false);
		addRowFixed(p, gbc, "Mã:", txtMa);

		// --- Tên sản phẩm
		txtTen = new JTextField();
		addRowFixed(p, gbc, "Tên sản phẩm:", txtTen);

		// --- Giá
		txtGia = new JTextField();
		addRowFixed(p, gbc, "Giá (₫):", txtGia);

		// --- Loại
		txtLoai = new JTextField();
		addRowFixed(p, gbc, "Loại SP:", txtLoai);

		// --- Mô tả
		txtMoTa = new JTextArea(6, 20);
		txtMoTa.setLineWrap(true);
		txtMoTa.setWrapStyleWord(true);
		JLabel lbMoTa = new JLabel("Mô tả:");
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		p.add(lbMoTa, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		p.add(new JScrollPane(txtMoTa), gbc);

		// --- Check active
		chkActive = new JCheckBox("Đang hoạt động", true);
		gbc.gridy++;
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		p.add(chkActive, gbc);

		// --- Hình ảnh label
		JLabel lbImg = new JLabel("Hình ảnh:");
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		p.add(lbImg, gbc);

		// --- Preview
		lblPreview = new JLabel(scaledOrPlaceholder(null, PREVIEW_W, PREVIEW_H));
			
		lblPreview.setPreferredSize(new Dimension(0, PREVIEW_H)); // rộng linh hoạt
		lblPreview.setMinimumSize(new Dimension(200, PREVIEW_H)); // tránh quá nhỏ
		
		lblPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(lblPreview, gbc);

		// --- Hàng link + nút 
		txtPathAnh = new JTextField();
		txtPathAnh.setEditable(false);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 2.0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(txtPathAnh, gbc);
		btnChonAnh = new JButton("Chọn ảnh...");
		styleButton(btnChonAnh, CLR_PRIMARY, CLR_TEXT_LIGHT);
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(btnChonAnh, gbc);
		btnXoaAnh = new JButton("Bỏ ảnh");
		styleButton(btnXoaAnh, CLR_MUTED, CLR_TEXT_LIGHT);
		gbc.gridx = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(btnXoaAnh, gbc);

		// --- Hàng nút: co giãn đều
		btnThem = new JButton("Thêm");
		styleButton(btnThem, CLR_PRIMARY, CLR_TEXT_LIGHT);
		btnLuu = new JButton("Lưu");
		styleButton(btnLuu, CLR_PRIMARY.darker(), CLR_TEXT_LIGHT);
		btnXoaTrang = new JButton("Xoá trắng");
		styleButton(btnXoaTrang, CLR_WARNING, CLR_TEXT_DARK);
		btnXoa = new JButton("Xoá");
		styleButton(btnXoa, CLR_DANGER, CLR_TEXT_LIGHT);

		gbc.gridy++;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		setButtonRowHeight(btnThem);
		p.add(btnThem, gbc);
		gbc.gridx = 1;
		setButtonRowHeight(btnLuu);
		p.add(btnLuu, gbc);
		gbc.gridx = 2;
		setButtonRowHeight(btnXoaTrang);
		p.add(btnXoaTrang, gbc);
		gbc.gridx = 3;
		setButtonRowHeight(btnXoa);
		p.add(btnXoa, gbc);

		return p;
	}

	private void addRowFixed(JPanel p, GridBagConstraints gbc, String label, JTextField field) {
		JLabel lb = new JLabel(label);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		p.add(lb, gbc);

		JPanel wrap = new JPanel();
		wrap.setLayout(new BoxLayout(wrap, BoxLayout.X_AXIS));
		wrap.add(field);
		int h = field.getPreferredSize().height;
		wrap.setPreferredSize(new Dimension(0, h));
		wrap.setMinimumSize(new Dimension(0, h));
		wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));

		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(wrap, gbc);
	}

	private void setButtonRowHeight(AbstractButton b) {
		Dimension d = b.getPreferredSize();
		if (d.height < BTN_H)
			d.height = BTN_H;
		b.setPreferredSize(d);
	}

	// ====== Helpers ======
	private void styleButton(JButton b, Color bg, Color fg) {
		b.setBackground(bg);
		b.setForeground(fg);
		b.setOpaque(true);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
	}

	private String formatGia(double v) {
		if (v <= 0)
			return "0";
		java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		return nf.format(v);
	}

	private double parseGia(String s) {
		if (s == null || s.trim().isEmpty())
			return 0.0;
		String norm = s.replace(".", "").replace(",", "").trim();
		try {
			return Double.parseDouble(norm);
		} catch (Exception ex) {
			return -1;
		}
	}


	private void bindEvents() {
		btnThem.addActionListener(this);
		btnLuu.addActionListener(this);
		btnXoaTrang.addActionListener(this);
		btnXoa.addActionListener(this);
		btnTim.addActionListener(this);
		txtSearch.addActionListener(this);
		btnChonAnh.addActionListener(this);
		btnXoaAnh.addActionListener(this);
	}

	private void setFormModeNew() {
		txtMa.setText("");
		txtTen.setText("");
		txtGia.setText("");
		txtLoai.setText("");
		txtMoTa.setText("");
		chkActive.setSelected(true);
		txtPathAnh.setText("");
		lblPreview.setIcon(scaledOrPlaceholder(null, PREVIEW_W, PREVIEW_H));
		tbl.clearSelection();
		txtTen.requestFocus();
	}

	private void fillForm(SanPham sp) {
		txtMa.setText(sp.getMaSP());
		txtTen.setText(sp.getTenSP());
		txtGia.setText(formatGia(sp.getGiaSP()));
		txtLoai.setText(sp.getLoaiSP());
		txtMoTa.setText(sp.getMoTaSP());
		chkActive.setSelected(sp.isTinhTrangSP());
		txtPathAnh.setText(sp.getHinhAnhSP() == null ? "" : sp.getHinhAnhSP());
		lblPreview.setIcon(scaledOrPlaceholder(sp.getHinhAnhSP(), PREVIEW_W, PREVIEW_H));
	}

	private void selectRowById(String id) {
		for (int i = 0; i < mdl.getRowCount(); i++) {
			if (String.valueOf(mdl.getValueAt(i, 1)).equals(id)) {
				tbl.setRowSelectionInterval(i, i);
				tbl.scrollRectToVisible(tbl.getCellRect(i, 0, true));
				break;
			}
		}
	}

	// ====== Actions ======
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o.equals(btnXoaTrang)) {
			setFormModeNew();
			return;
		}

		if (o.equals(btnThem)) { 
			String ten = txtTen.getText().trim();
			if (ten.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Tên sản phẩm không được rỗng!");
				txtTen.requestFocus();
				return;
			}
			double gia = parseGia(txtGia.getText().trim());
			if (gia < 0) {
				JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
				txtGia.requestFocus();
				return;
			}
			String ma = nextIdFromDB();
			String moTa = txtMoTa.getText().trim();
			String loai = txtLoai.getText().trim();
			boolean active = chkActive.isSelected();
			String path = txtPathAnh.getText().trim();
			if (path.isEmpty())
				path = null;
			
			SanPham sp = new SanPham(ma, ten, gia, moTa, path, active, loai);
			
			dao.insertSanPham(sp);
			dsAll.add(sp);
			
			reloadTable();
			selectRowById(ma);
			return;
		}

		if (o.equals(btnLuu)) { // lưu cập nhật theo mã
			String ma = txtMa.getText().trim();
			String ten = txtTen.getText().trim();
			if (ten.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Tên sản phẩm không được rỗng!");
				txtTen.requestFocus();
				return;
			}
			double gia = parseGia(txtGia.getText().trim());
			if (gia < 0) {
				JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
				txtGia.requestFocus();
				return;
			}
			String moTa = txtMoTa.getText().trim();
			String loai = txtLoai.getText().trim();
			boolean active = chkActive.isSelected();
			String path = txtPathAnh.getText().trim();
			
			if (path.isEmpty()) {
				path = null;
			}
			
			if (ma.isEmpty()) {
				ma = nextIdFromDB();
			}
			
			SanPham sp = new SanPham(ma, ten, gia, moTa, path, active, loai);
			dao.deleteSanPham(ma);
			dao.insertSanPham(sp);

			if (dsAll != null) {
				for (int i = 0; i < dsAll.size(); i++) {
			        if (ma.equals(String.valueOf(dsAll.get(i).getMaSP()))) {
			            dsAll.set(i, sp);
			            break;
			        }
			    }
			}
			
			
			reloadTable();
			selectRowById(ma);
			return;
		}

		if (o.equals(btnXoa)) {
			int row = tbl.getSelectedRow();
			if (row == -1) {
				JOptionPane.showMessageDialog(this, "Chọn dòng để xoá");
				return;
			}
			String ma = String.valueOf(mdl.getValueAt(row, 1));
			
			int isXoa = JOptionPane.showConfirmDialog(this, "Bạn thật sự muốn xoá không?", "Xác nhận",
					JOptionPane.YES_NO_OPTION);
			
			if (isXoa == JOptionPane.NO_OPTION)
				return;
			
			if (!dao.deleteSanPham(ma)) {
			    JOptionPane.showMessageDialog(this, "Xoá thất bại!");
			    return;
			}
			
			if (dsAll != null) {
			    dsAll.removeIf(sp -> ma.equals(String.valueOf(sp.getMaSP())));
			}
			
			reloadTable();
			setFormModeNew();
			JOptionPane.showMessageDialog(this, "Đã xoá thành công!");
			return;
		}

		if (o.equals(btnTim) || o.equals(txtSearch)) {
			String kw = txtSearch.getText().trim();
			
			if(kw.isEmpty()) {
				setTable(dao.getAllSanPham());
				return;
			}
				
			List<SanPham> dataLs = dao.getAllSanPham();
			List<SanPham> ans = new ArrayList<SanPham>(); 
			
			boolean ok;
			
			for(SanPham x : dataLs) {
				String [] strLs = {
						x.getMaSP(), 
						x.getTenSP(), 
						x.getMoTaSP(), 
						Double.toString(x.getGiaSP()), 
						x.isTinhTrangSP() ? "Đang hoạt động" : "Ngừng",
						x.getLoaiSP(),
				}; 
				
				ok = false;
				
				for(String subS : strLs) {
					if(subS.toLowerCase().contains(kw.toLowerCase())) {
						ok = true;
						break;
					}
				}
				
				if(ok) {
					ans.add(x);
				}
			}
			
			setTable(ans);
			return;
		}

		if (o.equals(btnChonAnh)) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Chọn hình ảnh");
			int ret = fc.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				txtPathAnh.setText(f.getAbsolutePath());
				lblPreview.setIcon(scaledOrPlaceholder(f.getAbsolutePath(), PREVIEW_W, PREVIEW_H));
			}
			return;
		}
		if (o.equals(btnXoaAnh)) {
			txtPathAnh.setText("");
			lblPreview.setIcon(scaledOrPlaceholder(null, PREVIEW_W, PREVIEW_H));
		}
	}

	// ====== Mouse ======
	public void mouseClicked(MouseEvent e) {
		if (e.getSource().equals(tbl)) {
			int r = tbl.getSelectedRow();
			if (r >= 0) {
				String ma = String.valueOf(mdl.getValueAt(r, 1));
				
				SanPham sp = dao.findById(ma);
				if (sp != null) fillForm(sp);
				
			}
			if (e.getClickCount() == 2 && !e.isConsumed()) {
				e.consume();
				txtTen.requestFocus();
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	// ====== Image utils ======
	private ImageIcon scaledOrPlaceholder(String path, int w, int h) {
		Image img = null;
		if (path != null && !path.isEmpty()) {
			File f = new File(path);
			if (f.exists() && f.isFile()) {
				ImageIcon raw = new ImageIcon(path);
				img = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
			}
		}
		if (img == null)
			img = placeholderImage(w, h, "NO IMAGE");
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
			g.drawLine(0, 0, w, h);
			g.drawLine(0, h, w, 0);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setFont(new Font("SansSerif", Font.BOLD, 14));
			FontMetrics fm = g.getFontMetrics();
			int tw = fm.stringWidth(text), th = fm.getAscent();
			g.setColor(new Color(100, 100, 100));
			g.drawString(text, Math.max(6, (w - tw) / 2), Math.max(th + 2, (h + th) / 2 - 4));
		} finally {
			g.dispose();
		}
		return bi;
	}

	// ====== Demo main ======
//	public static void main(String[] args) {
//		SwingUtilities.invokeLater(() -> {
//			JFrame f = new JFrame("QL Loại SP - Grid Right + Giá");
//			TAB_SanPham p = new TAB_SanPham();
//			p.reloadTable();
//			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			f.setContentPane(p);
//			f.setSize(1140, 660);
//			f.setLocationRelativeTo(null);
//			f.setVisible(true);
//		});
//	}
}
