package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.Desktop;

import com.dao.DAO_SanPham;
import com.entity.SanPham;
import com.enums.LoaiSP;
import com.service.TabStyler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	JTextField txtSearch, txtMa, txtTen, txtGia, txtPathAnh;
	JComboBox<LoaiSP> cboLoai;
	JTextArea txtMoTa;
	JCheckBox chkActive;
	JButton btnThem, btnLuu, btnXoa, btnXoaTrang, btnTim, btnChonAnh, btnXoaAnh,
    	btnFileMau, btnNhapExcel, btnXuatExcel;
	JTable tbl;
	DefaultTableModel mdl;
	JLabel lblPreview;
	
	// Bộ lọc dưới thanh tìm kiếm
	private JComboBox<LoaiSP> cboLocLoai;
	private JComboBox<String> cboLocTrangThai;
	private JTextField txtGiaMin;
	private JTextField txtGiaMax;
	private JButton btnResetBoLoc;
	
	// Paging (SQL-based)
	int pageSize = 10, currentPage = 1, totalPages = 1, totalRows = 0;
	String currentKeyword = "";
	JPanel pnlPaging;
	JLabel lbPageInfo;

	
	// Sizes
	static final int THUMB_W = 58, THUMB_H = 58, PREVIEW_W = 380, PREVIEW_H = 240, FORM_FIELD_W = 240, BTN_H = 32;
	
	// link dir
	private static final String IMG_DIR = "src/main/resources/sp_image";
	

	public TAB_SanPham() {
		
		dao = new DAO_SanPham();
		
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(10, 10, 10, 10));

                JPanel northWrapper = new JPanel(new BorderLayout());
                northWrapper.setOpaque(false);
                northWrapper.add(TabStyler.createHeader("QUẢN LÝ SẢN PHẨM"), BorderLayout.NORTH);
                northWrapper.add(buildNorthSearch(), BorderLayout.CENTER);
                add(northWrapper, BorderLayout.NORTH);
		
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
		
		currentKeyword = "";
		currentPage = 1;
		reloadTable();
		LoadCboLoaiSP();
		bindEvents();
		setFormModeNew();
		
                TabStyler.applyContentFont(this);

        }
	
	private void LoadCboLoaiSP() {
	    DefaultComboBoxModel<LoaiSP> m = new DefaultComboBoxModel<>();
	    for (LoaiSP loai : LoaiSP.values()) {
	        m.addElement(loai);
	    }
	    cboLoai.setModel(m);
	    cboLoai.setSelectedIndex(-1);
	}

	
	// tải bảng và dữ liệu
	//	maSP, tenSP, giaSP, moTaSP, hinhAnhSP, tinhTrangSP, loaiSP
	private void loadTable(List<SanPham> ds) {
	    mdl.setRowCount(0);
	    for (SanPham sp : ds) {
	    	
	    	ImageIcon icon = scaledOrPlaceholder(sp.getHinhAnhSP(), THUMB_W, THUMB_H);
	    	String giaStr = formatGia(sp.getGiaSP()); // đổi sang String cho khớp getColumnClass
	    	LoaiSP loaiEnum = LoaiSP.fromAny(sp.getLoaiSP());
	    	Object loaiHienThi = loaiEnum != null ? loaiEnum.toString() : sp.getLoaiSP();
	    	mdl.addRow(new Object[]{
	    	    icon,                  // 0: Ảnh (ImageIcon)
	    	    sp.getMaSP(),          // 1: Mã
	    	    sp.getTenSP(),         // 2: Tên
	    	    sp.getMoTaSP(),        // 3: Mô tả
	    	    giaStr,                // 4: Giá (String)
	    	    loaiHienThi,           // 5: Loại
	    	    sp.isTinhTrangSP() ,   // 6: Hoạt động (Boolean)
	    	});
	    }
	}
	
	//tải trang hiên tại (phân trang bằng SQL, không dùng dsAll)
	// tải trang hiên tại (phân trang bằng SQL, không dùng dsAll)
	private void reloadTable() {
	    // Đọc giá trị bộ lọc
	    String loaiFilter = null;
	    Boolean trangThaiFilter = null;
	    Double giaMin = null;
	    Double giaMax = null;

	    // Lọc theo loại
	    if (cboLocLoai != null) {
	        LoaiSP loaiEnumLoc = (LoaiSP) cboLocLoai.getSelectedItem();
	        if (loaiEnumLoc != null) {
	            loaiFilter = loaiEnumLoc.toDbValue();
	        }
	    }

	    // Lọc theo trạng thái
	    if (cboLocTrangThai != null) {
	        Object v = cboLocTrangThai.getSelectedItem();
	        if (v instanceof String) {
	            String s = (String) v;
	            if ("Còn bán".equals(s)) {
	                trangThaiFilter = Boolean.TRUE;
	            } else if ("Ngừng bán".equals(s)) {
	                trangThaiFilter = Boolean.FALSE;
	            }
	            // "Tất cả" => null (không lọc)
	        }
	    }

	    // Lọc theo giá tối thiểu
	    if (txtGiaMin != null) {
	        String s = txtGiaMin.getText().trim();
	        if (!s.isEmpty()) {
	            double v = parseGia(s);
	            if (v < 0) {
	                JOptionPane.showMessageDialog(this, "Giá tối thiểu không hợp lệ!");
	                txtGiaMin.requestFocus();
	                return;
	            }
	            giaMin = v;
	        }
	    }

	    // Lọc theo giá tối đa
	    if (txtGiaMax != null) {
	        String s = txtGiaMax.getText().trim();
	        if (!s.isEmpty()) {
	            double v = parseGia(s);
	            if (v < 0) {
	                JOptionPane.showMessageDialog(this, "Giá tối đa không hợp lệ!");
	                txtGiaMax.requestFocus();
	                return;
	            }
	            giaMax = v;
	        }
	    }

	    // Gọi DAO với đủ tham số
	    totalRows = dao.countSanPham(currentKeyword, loaiFilter, trangThaiFilter, giaMin, giaMax);

	    totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));
	    if (currentPage < 1) currentPage = 1;
	    if (currentPage > totalPages) currentPage = totalPages;

	    List<SanPham> dsPage = dao.getSanPhamPage(
	            currentKeyword, loaiFilter, trangThaiFilter, giaMin, giaMax,
	            currentPage, pageSize
	    );
	    loadTable(dsPage);
	    rebuildPaging(totalRows);
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
	    return dao.getNextMaSanPham();
	}
	
	// build các tiểu cấu trúc
	// ví dụ gọi: setBorder(createTitleBorder("QUẢN LÝ SẢN PHẨM", new Color(0,102,204), 22f, 2));
        private static Border createTitleBorder(String title, Color titleColor, float fontSizePt, int lineThickness) {
            Font base = TabStyler.SECTION_FONT;

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
		
//		Panel dòng 1: tìm kiếm
                JPanel p = new JPanel(new BorderLayout(8, 0));
                p.setOpaque(false);
		txtSearch = new JTextField();
		btnTim = new JButton("Tìm");
		
		styleButton(btnTim, CLR_PRIMARY, CLR_TEXT_LIGHT);
		p.add(new JLabel("Tìm kiếm:"), BorderLayout.WEST);
		p.add(txtSearch, BorderLayout.CENTER);
		p.add(btnTim, BorderLayout.EAST);
		
		// === Khởi tạo combobox lọc loại sản phẩm ===
		cboLocLoai = new JComboBox<>();
		cboLocLoai.addItem(null); // null = Tất cả loại
		for (LoaiSP loai : LoaiSP.values()) {
		    cboLocLoai.addItem(loai);
		}
		// Renderer để hiện chữ "Tất cả loại" khi item = null
		cboLocLoai.setRenderer(new DefaultListCellRenderer() {
		    @Override
		    public Component getListCellRendererComponent(
		            JList<?> list, Object value, int index,
		            boolean isSelected, boolean cellHasFocus) {
		        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        if (value == null) {
		            setText("Tất cả loại");
		        }
		        return this;
		    }
		});

		// === Combobox lọc trạng thái kinh doanh ===
		cboLocTrangThai = new JComboBox<>(new String[] {
		    "Tất cả", "Còn bán", "Ngừng bán"
		});

		// === Ô nhập khoảng giá ===
		txtGiaMin = new JTextField(6);
		setupGiaAutoFormat(txtGiaMin);

		txtGiaMax = new JTextField(6);
		setupGiaAutoFormat(txtGiaMax);

		// === Nút xóa bộ lọc ===
		btnResetBoLoc = new JButton("Xóa bộ lọc");
		
		// Panel dòng 2: các bộ lọc
		JPanel pnlDongBoLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		pnlDongBoLoc.add(new JLabel("Loại:"));
		pnlDongBoLoc.add(cboLocLoai);

		pnlDongBoLoc.add(new JLabel("Trạng thái:"));
		pnlDongBoLoc.add(cboLocTrangThai);

		pnlDongBoLoc.add(new JLabel("Giá:"));
		pnlDongBoLoc.add(txtGiaMin);
		pnlDongBoLoc.add(new JLabel("-"));
		pnlDongBoLoc.add(txtGiaMax);

		pnlDongBoLoc.add(btnResetBoLoc);
		
		// Panel tổng: xếp dọc 2 dòng trên
		JPanel pnlTimKiemVaBoLoc = new JPanel();
		pnlTimKiemVaBoLoc.setLayout(new BoxLayout(pnlTimKiemVaBoLoc, BoxLayout.Y_AXIS));
		pnlTimKiemVaBoLoc.add(p);
		pnlTimKiemVaBoLoc.add(Box.createVerticalStrut(4)); // khoảng cách nhỏ
		pnlTimKiemVaBoLoc.add(pnlDongBoLoc);
		
		return pnlTimKiemVaBoLoc;
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
		
		tbl.getTableHeader().setReorderingAllowed(false);
		tbl.getTableHeader().setResizingAllowed(false);
		
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

		// thanh nút Excel (bên trong viền, trên bảng, canh trái)
		JPanel pnlExcelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		pnlExcelButtons.setBorder(new EmptyBorder(0, 0, 4, 0)); // tạo khoảng cách với bảng

		btnFileMau = new JButton("CSV mẫu");
		styleButton(btnFileMau, CLR_MUTED, CLR_TEXT_LIGHT);

		btnNhapExcel = new JButton("Nhập CSV");
		styleButton(btnNhapExcel, CLR_PRIMARY, CLR_TEXT_LIGHT);

		btnXuatExcel = new JButton("Xuất CSV");
		styleButton(btnXuatExcel, CLR_PRIMARY.darker(), CLR_TEXT_LIGHT);

		pnlExcelButtons.add(btnFileMau);
		pnlExcelButtons.add(btnNhapExcel);
		pnlExcelButtons.add(btnXuatExcel);

		wrap.add(pnlExcelButtons, BorderLayout.NORTH);
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
		setupGiaAutoFormat(txtGia);
		addRowFixed(p, gbc, "Giá (₫):", txtGia);

		// --- Loại
		cboLoai = new JComboBox<>();
		addRowFixed(p, gbc, "Loại SP:", cboLoai);

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

	private void addRowFixed(JPanel p, GridBagConstraints gbc, String label, JComponent field) {
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
	
	private void selectLoai(String loaiStr) {
	    if (loaiStr == null || loaiStr.isBlank()) { 
	        cboLoai.setSelectedIndex(-1); 
	        return; 
	    }
	    LoaiSP loai = LoaiSP.fromAny(loaiStr);
	    if (loai == null) {
	        cboLoai.setSelectedIndex(-1);
	    } else {
	        cboLoai.setSelectedItem(loai);
	    }
	}

	
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

	private double parseGia(String text) {
	    if (text == null) return -1;
	    // bỏ hết ký tự không phải số, kể cả dấu phẩy / khoảng trắng
	    String s = text.replaceAll("[^0-9]", "");
	    if (s.isEmpty()) return -1;

	    try {
	        return Double.parseDouble(s);
	    } catch (NumberFormatException ex) {
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
	    btnFileMau.addActionListener(this);
	    btnXuatExcel.addActionListener(this);
	    btnNhapExcel.addActionListener(this);

	    // === Bộ lọc dưới thanh tìm kiếm ===
	    if (btnResetBoLoc != null) {
	        btnResetBoLoc.addActionListener(this);
	    }
	    if (cboLocLoai != null) {
	        cboLocLoai.addActionListener(this);
	    }
	    if (cboLocTrangThai != null) {
	        cboLocTrangThai.addActionListener(this);
	    }
	    if (txtGiaMin != null) {
	        txtGiaMin.addActionListener(this);   // Enter trong ô giá min
	    }
	    if (txtGiaMax != null) {
	        txtGiaMax.addActionListener(this);   // Enter trong ô giá max
	    }
	}


	private void setFormModeNew() {
		txtMa.setText("");
		txtTen.setText("");
		txtGia.setText("");
		cboLoai.setSelectedIndex(-1);
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
		selectLoai(sp.getLoaiSP());
		txtMoTa.setText(sp.getMoTaSP());
		chkActive.setSelected(sp.isTinhTrangSP());
		txtPathAnh.setText(sp.getHinhAnhSP() == null ? "" : sp.getHinhAnhSP());
		lblPreview.setIcon(scaledOrPlaceholder(sp.getHinhAnhSP(), PREVIEW_W, PREVIEW_H));
		
	}

	// Chọn hàng
	private void selectRowById(String id) {
	    if (id == null || id.isBlank()) return;

	    // Lấy index của mã trong danh sách đã lọc hiện tại (SQL)
	    int idx = dao.getIndexById(currentKeyword, id.trim());
	    if (idx < 0) {
	        tbl.clearSelection();
	        return;
	    }

	    // Tính trang và nạp lại đúng trang
	    int targetPage = idx / pageSize + 1;
	    currentPage = targetPage;
	    reloadTable();

	    // chọn dòng trong trang
	    int rowInPage = idx % pageSize;
	    int viewRow = (tbl.getRowSorter() == null)
	            ? rowInPage
	            : tbl.convertRowIndexToView(rowInPage);

	    if (viewRow >= 0 && viewRow < tbl.getRowCount()) {
	        tbl.getSelectionModel().setSelectionInterval(viewRow, viewRow);
	        tbl.scrollRectToVisible(tbl.getCellRect(viewRow, 0, true));
	        tbl.requestFocusInWindow();
	    } else {
	        tbl.clearSelection();
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
			
			//tên
			String ten = txtTen.getText().trim();
			if (ten.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Tên sản phẩm không được rỗng!");
				txtTen.requestFocus();
				return;
			}
			
			//giá
			double gia = parseGia(txtGia.getText().trim());
			if (gia < 0) {
				JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
				txtGia.requestFocus();
				return;
			}
			
			//mã
			String ma = nextIdFromDB();
			
			//mô tả
			String moTa = txtMoTa.getText().trim();
			
			//loại
			LoaiSP loaiEnum = (LoaiSP) cboLoai.getSelectedItem();
			if (loaiEnum == null) {
			    JOptionPane.showMessageDialog(this, "Chưa chọn loại sản phẩm!");
			    cboLoai.requestFocus();
			    return;
			}
			String loai = loaiEnum.toDbValue();
			
			// trạng thái
			boolean active = chkActive.isSelected();
			
			//path ảnh
			String path = txtPathAnh.getText().trim();
			if (path.isEmpty())
				path = null;
			
			SanPham sp = new SanPham(ma, ten, gia, moTa, path, active, loai);
			
			dao.insertSanPham(sp);

			// Sau khi thêm: bỏ lọc để chắc chắn thấy sản phẩm mới
			currentKeyword = "";
			txtSearch.setText("");
			selectRowById(ma);
			fillForm(sp);
			
			return;
		}

		if (o.equals(btnLuu)) { // lưu cập nhật theo mã
			
			//mã
			String ma = txtMa.getText().trim();
			
			//tên
			String ten = txtTen.getText().trim();
			if (ten.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Tên sản phẩm không được rỗng!");
				txtTen.requestFocus();
				return;
			}
			
			//giá
			double gia = parseGia(txtGia.getText().trim());
			if (gia < 0) {
				JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
				txtGia.requestFocus();
				return;
			}
			//mô tả
			String moTa = txtMoTa.getText().trim();
			
			//loại
			LoaiSP loaiEnum = (LoaiSP) cboLoai.getSelectedItem();
			if (loaiEnum == null) {
			    JOptionPane.showMessageDialog(this, "Chưa chọn loại sản phẩm!");
			    cboLoai.requestFocus();
			    return;
			}
			String loai = loaiEnum.toDbValue();
			
			//trạng thái
			boolean active = chkActive.isSelected();
			
			//path ảnh
			String path = txtPathAnh.getText().trim();
			
			if (path.isEmpty()) {
				path = null;
			}
			
			if (ma.isEmpty()) {
				ma = nextIdFromDB();
			}
			
			SanPham sp = new SanPham(ma, ten, gia, moTa, path, active, loai);
			if (!dao.updateSanPham(sp)) {
				JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thất bại!");
				return;
			}
			// Sau khi sửa: bỏ lọc, nhảy về sản phẩm vừa sửa
			currentKeyword = "";
			txtSearch.setText("");
			selectRowById(ma);
			JOptionPane.showMessageDialog(this, "Cập nhật thành công!");

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
			
			if ((isXoa == JOptionPane.NO_OPTION) || (isXoa == JOptionPane.CLOSED_OPTION))
				return;
			
			if (!dao.deleteSanPham(ma)) {
			    JOptionPane.showMessageDialog(this, "Xoá thất bại!");
			    return;
			}
			
			// Sau khi xoá: reload lại theo filter hiện tại
			reloadTable();
			setFormModeNew();
			JOptionPane.showMessageDialog(this, "Đã xoá thành công!");
			return;
		}
		
		if (o.equals(btnResetBoLoc)) {
		    // Reset toàn bộ bộ lọc
		    if (cboLocLoai != null) {
		        cboLocLoai.setSelectedIndex(0); // null = "Tất cả loại"
		    }
		    if (cboLocTrangThai != null) {
		        cboLocTrangThai.setSelectedIndex(0); // "Tất cả"
		    }
		    if (txtGiaMin != null) {
		        txtGiaMin.setText("");
		    }
		    if (txtGiaMax != null) {
		        txtGiaMax.setText("");
		    }

		    currentPage = 1;
		    reloadTable();
		    return;
		}

		// Khi thay đổi lựa chọn lọc / Enter trong ô giá => áp dụng bộ lọc luôn
		if (o.equals(cboLocLoai) || o.equals(cboLocTrangThai)
		        || o.equals(txtGiaMin) || o.equals(txtGiaMax)) {
		    currentPage = 1;
		    reloadTable();
		    return;
		}


		if (o.equals(btnTim) || o.equals(txtSearch)) {
			String kw = txtSearch.getText().trim();
			currentKeyword = kw;
			currentPage = 1;
			reloadTable();
			return;
		}

		if (o.equals(btnChonAnh)) {
			JFileChooser fc = new JFileChooser(new File(IMG_DIR));
		    fc.setDialogTitle("Chọn hình ảnh");
		    int ret = fc.showOpenDialog(this);
		    if (ret == JFileChooser.APPROVE_OPTION) {
		        File f = fc.getSelectedFile();

		        String fileName = f.getName();      // ✅ chỉ lưu tên file
		        txtPathAnh.setText(fileName);
		        lblPreview.setIcon(scaledOrPlaceholder(fileName, PREVIEW_W, PREVIEW_H));
		    }
		    return;
		}

		if (o.equals(btnXoaAnh)) {
			txtPathAnh.setText("");
			lblPreview.setIcon(scaledOrPlaceholder(null, PREVIEW_W, PREVIEW_H));
		}
		
		if (o.equals(btnFileMau)) {
            exportTemplateCsv();
            return;
        }
		
		if (o.equals(btnXuatExcel)) {
		    exportCurrentFilterToCsv();
		    return;
		}
		
		if (o.equals(btnNhapExcel)) {
            importFromCsv();
            return;
        }
	}
	
    // ====== Nhập sản phẩm từ CSV (mở bằng Excel) ======
    private void importFromCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file CSV để nhập sản phẩm");
        fc.setFileFilter(new FileNameExtensionFilter("File CSV (*.csv)", "csv"));

        int ret = fc.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fc.getSelectedFile();
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this, "File không tồn tại!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int added = 0;
        int skipped = 0;
        Set<String> tenSeen = new HashSet<>();  // tránh trùng ngay trong 1 lần import

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // bỏ qua dòng header đầu tiên
                if (first) {
                    first = false;
                    continue;
                }

                java.util.List<String> cols = parseCsvLine(line);
                if (cols.size() < 7) {
                    skipped++;
                    continue;
                }

                // Cột trong file: 0: MaSP (BỎ QUA), 1: TenSP, 2: Gia, 3: MoTa, 4: LoaiSP, 5: TrangThai, 6: HinhAnh

                String ten = cols.get(1).trim();
                if (ten.isEmpty()) {
                    skipped++;
                    continue;
                }

                String tenKey = ten.toLowerCase();

                // trùng trong chính file import
                if (tenSeen.contains(tenKey)) {
                    skipped++;
                    continue;
                }

                // trùng trong DB
                if (dao.existsTenSanPham(ten)) {
                    skipped++;
                    continue;
                }

                // giá
                double gia;
                try {
                    gia = Double.parseDouble(cols.get(2).trim());
                } catch (Exception ex) {
                    skipped++;
                    continue;
                }

                String moTa = cols.get(3).trim();
                String loaiRaw = cols.get(4).trim();
                if (loaiRaw.isEmpty()) {
                    skipped++;
                    continue;
                }

                LoaiSP loaiEnum = LoaiSP.fromAny(loaiRaw);
                if (loaiEnum == null) {
                    skipped++;
                    continue;
                }
                String loai = loaiEnum.toDbValue();

                String ttRaw = cols.get(5).trim();
                boolean tinhTrang = "1".equals(ttRaw) || "true".equalsIgnoreCase(ttRaw);

                String hinh = cols.get(6).trim();
                if (hinh.isEmpty()) {
                    hinh = null;
                }

                // KHÔNG dùng mã trong file, luôn sinh mã mới
                String ma = nextIdFromDB();

                SanPham sp = new SanPham(ma, ten, gia, moTa, hinh, tinhTrang, loai);
                if (dao.insertSanPham(sp)) {
                    added++;
                    tenSeen.add(tenKey);
                } else {
                    skipped++;
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi đọc file CSV:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Đã thêm " + added + " sản phẩm mới.\n" +
                "Bỏ qua " + skipped + " dòng (trùng tên hoặc dữ liệu không hợp lệ).",
                "Kết quả nhập",
                JOptionPane.INFORMATION_MESSAGE);

        if (added > 0) {
            currentPage = 1;
            reloadTable();   // reload lại danh sách theo bộ lọc hiện tại
        }
    }
	
    // Parse 1 dòng CSV -> list cột, có hỗ trợ dấu ngoặc kép và dấu phẩy trong text
    private java.util.List<String> parseCsvLine(String line) {
        java.util.List<String> cols = new ArrayList<>();
        if (line == null) return cols;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // xử lý "" => "
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        cols.add(sb.toString());
        return cols;
    }
    
    private void setupGiaAutoFormat(JTextField txt) {
        txt.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String raw = txt.getText();
                // bỏ hết ký tự không phải số
                String digits = raw.replaceAll("[^0-9]", "");
                if (digits.isEmpty()) {
                    txt.setText("");
                    return;
                }

                java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###", symbols);
                df.setGroupingUsed(true);
                df.setMaximumFractionDigits(0);

                try {
                    long value = Long.parseLong(digits);
                    String formatted = df.format(value);
                    if (!formatted.equals(raw)) {
                        txt.setText(formatted);
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }
        });
    }



	
    // ====== Xuất CSV theo bộ lọc hiện tại ======
    private void exportCurrentFilterToCsv() {
        // đọc lại bộ lọc giống reloadTable()
        String loaiFilter = null;
        Boolean trangThaiFilter = null;
        Double giaMin = null;
        Double giaMax = null;

        if (cboLocLoai != null) {
            LoaiSP loaiEnumLoc = (LoaiSP) cboLocLoai.getSelectedItem();
            if (loaiEnumLoc != null) {
                loaiFilter = loaiEnumLoc.toDbValue();
            }
        }

        if (cboLocTrangThai != null) {
            Object v = cboLocTrangThai.getSelectedItem();
            if (v instanceof String) {
                String s = (String) v;
                if ("Còn bán".equals(s)) {
                    trangThaiFilter = Boolean.TRUE;
                } else if ("Ngừng bán".equals(s)) {
                    trangThaiFilter = Boolean.FALSE;
                }
                // "Tất cả" => null
            }
        }

        if (txtGiaMin != null) {
            String s = txtGiaMin.getText().trim();
            if (!s.isEmpty()) {
                double v = parseGia(s);
                if (v < 0) {
                    JOptionPane.showMessageDialog(this, "Giá tối thiểu không hợp lệ!");
                    txtGiaMin.requestFocus();
                    return;
                }
                giaMin = v;
            }
        }

        if (txtGiaMax != null) {
            String s = txtGiaMax.getText().trim();
            if (!s.isEmpty()) {
                double v = parseGia(s);
                if (v < 0) {
                    JOptionPane.showMessageDialog(this, "Giá tối đa không hợp lệ!");
                    txtGiaMax.requestFocus();
                    return;
                }
                giaMax = v;
            }
        }

        // chọn nơi lưu file
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu danh sách sản phẩm (CSV)");
        fc.setSelectedFile(new File("DanhSachSanPham.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("File CSV (*.csv)", "csv"));

        int ret = fc.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fc.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            file = new File(path + ".csv");
        }

        if (file.exists()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "File đã tồn tại, bạn có muốn ghi đè không?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // gọi DAO export
        int count = dao.exportSanPhamToCsv(
                file,
                currentKeyword,
                loaiFilter,
                trangThaiFilter,
                giaMin,
                giaMax
        );

        if (count >= 0) {
            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + count + " sản phẩm ra file:\n" + file.getName(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            // mở luôn file vừa xuất
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(file);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Không thể mở file vừa xuất: " + ex.getMessage());
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi khi xuất dữ liệu!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

	
    // ====== File mẫu (CSV) cho nhập sản phẩm ======
    private void exportTemplateCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file mẫu nhập sản phẩm");
        fc.setSelectedFile(new File("MauNhapSanPham.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("File CSV (*.csv)", "csv"));

        int ret = fc.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fc.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            file = new File(path + ".csv");
        }

        if (file.exists()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "File đã tồn tại, bạn có muốn ghi đè không?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {

            out.println("MaSP,TenSP,Gia,MoTa,LoaiSP,TrangThai,HinhAnh");
            out.println("SP001,Gạo ST25,18000,Gạo thơm dẻo đặc sản Sóc Trăng,DoAn,1,gao_st25.jpg");
            out.println("SP002,Mì Hảo Hảo tôm chua cay,4500,Mì ăn liền vị tôm chua cay,DoAn,1,mihaohao.jpg");
            out.println("SP003,Bánh Oreo 137g,21000,Bánh quy kem socola,DoAn,1,oreo137.jpg");
            out.println("SP004,Phô mai Con Bò Cười 8 miếng,44000,Phô mai béo ngậy, bổ sung canxi,DoAn,1,phomai_conboc.jpg");
            out.println("SP005,Cà phê G7 3in1 hộp 18 gói,56000,Cà phê hòa tan đậm đà hương vị Việt,DoAn,1,cafe_g7_3in1.jpg");

            JOptionPane.showMessageDialog(this,
                    "Đã tạo file mẫu: " + file.getName(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            
            // tự động mở file mẫu sau khi tạo xong
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(file);
                    }
                }
            } catch (IOException ex2) {
                // không cần báo lỗi ầm ĩ, log nhẹ là được
                System.err.println("Không thể mở file mẫu: " + ex2.getMessage());
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi ghi file mẫu:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
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
	// ====== Image utils ======
	private ImageIcon scaledOrPlaceholder(String path, int w, int h) {
	    Image img = null;
	    if (path != null && !path.isEmpty()) {

	        // 1) Thử coi path là đường dẫn tuyệt đối (khi chọn ảnh bằng JFileChooser)
	        File f = new File(path);

	        // 2) Nếu không tồn tại thì coi path là tên file tương đối, ghép với IMG_DIR (giống TAB_KhuyenMai)
	        if (!f.exists() || !f.isFile()) {
	            f = new File(IMG_DIR, path);
	        }

	        if (f.exists() && f.isFile()) {
	            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
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
