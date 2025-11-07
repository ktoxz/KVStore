package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.entity.SanPham;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.Font;
import java.awt.Color;


public class TAB_SanPham extends JPanel implements ActionListener, MouseListener {
	
	interface LoaiSPRepo {
		String nextId();

		void save(SanPham sp);

		void update(SanPham sp);

		void deleteById(String ma);

		Optional<SanPham> findById(String ma);

		List<SanPham> findAll();

		List<SanPham> searchByKeyword(String kw);
	}

	static class InMemoryLoaiSPRepo implements LoaiSPRepo {
		private final Map<String, SanPham> db = new LinkedHashMap<>();
		private long seq = 0;

		public synchronized String nextId() {
			return String.format("LSP%04d", ++seq);
		}

		public synchronized void save(SanPham sp) {
			db.put(sp.getMa(), sp);
		}

		public synchronized void update(SanPham sp) {
			db.put(sp.getMa(), sp);
		}

		public synchronized void deleteById(String ma) {
			db.remove(ma);
		}

		public synchronized Optional<SanPham> findById(String ma) {
			return Optional.ofNullable(db.get(ma));
		}

		public synchronized List<SanPham> findAll() {
			return new ArrayList<>(db.values());
		}

		public synchronized List<SanPham> searchByKeyword(String kw) {
			String k = kw == null ? "" : kw.trim().toLowerCase();
			if (k.isEmpty())
				return findAll();
			return db
					.values().stream().filter(sp -> sp.getMa().toLowerCase().contains(k)
							|| sp.getTen().toLowerCase().contains(k) || sp.getMoTa().toLowerCase().contains(k))
					.collect(Collectors.toList());
		}
	}
	
	final LoaiSPRepo repo;

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
	JTextArea txtMoTa;
	JCheckBox chkActive;
	JButton btnThem, btnLuu, btnXoa, btnXoaTrang, btnTim, btnChonAnh, btnXoaAnh;
	JTable tbl;
	DefaultTableModel mdl;
	JLabel lblPreview;

	// Sizes
	static final int THUMB_W = 56, THUMB_H = 56, PREVIEW_W = 300, PREVIEW_H = 220, FORM_FIELD_W = 240, BTN_H = 32;

	public TAB_SanPham() {
		this(new InMemoryLoaiSPRepo(), Collections.emptyList());
	}

	public TAB_SanPham(LoaiSPRepo repo, List<SanPham> initialData) {
		
		this.repo = repo == null ? new InMemoryLoaiSPRepo() : repo;
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(buildNorthSearch(), BorderLayout.NORTH);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCenterTable(), buildEastForm());
		split.setResizeWeight(0.68);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);
		split.setDividerLocation( 700 );
		
		final double MIN_PCT = 0.35, MAX_PCT = 0.70;
		
		split.addComponentListener(new java.awt.event.ComponentAdapter() {
		  @Override public void componentResized(java.awt.event.ComponentEvent e) {
		    int span = split.getWidth() - split.getDividerSize();
		    if (span <= 0) return;
		    java.awt.Component left  = split.getLeftComponent();
		    java.awt.Component right = split.getRightComponent();
		    left.setMinimumSize(new java.awt.Dimension((int)Math.round(span * MIN_PCT), 1));
		    right.setMinimumSize(new java.awt.Dimension((int)Math.round(span * (1 - MAX_PCT)), 1));
		  }
		});
		
		add(split, BorderLayout.CENTER);

		if (initialData != null) {
			for (SanPham sp : initialData) {
				String ma = (sp.getMa() == null || sp.getMa().isEmpty()) ? this.repo.nextId() : sp.getMa();
				this.repo
						.save(new SanPham(ma, sp.getTen(), sp.getMoTa(), sp.isHoatDong(), sp.getHinhAnh(), sp.getGia()));
			}
		}
		
		
		reloadTable();
		bindEvents();
		setFormModeNew();
		
		setBorder(
				new CompoundBorder(
						new EmptyBorder(8, 8, 8, 8),
						new CompoundBorder(				
							    new TitledBorder(
							        new LineBorder(new Color(200,200,200), 0),    
							        "QUẢN LÝ SẢN PHẨM",                           
							        TitledBorder.LEFT, TitledBorder.TOP,          // canh trái, trên
							        getFont().deriveFont(Font.BOLD, 22f),         // chữ đậm như hình
							        new Color(0,102,204)                          // xanh dương
							    ),
							    new EmptyBorder(10, 12, 12, 12)                   // padding nội dung
						)
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
		String[] cols = { "Ảnh", "Mã", "Tên loại", "Mô tả", "Giá", "Hoạt động" };
		mdl = new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}

			public Class<?> getColumnClass(int c) {
				if (c == 0)
					return ImageIcon.class; // Ảnh
				if (c == 5)
					return Boolean.class; // Hoạt động
				return String.class; // các cột còn lại
			}
		};
		
		tbl = new JTable(mdl);
		tbl.setRowHeight(Math.max(THUMB_H + 8, 28));
		tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl.addMouseListener(this);
		JScrollPane sp = new JScrollPane(tbl);
		sp.setPreferredSize(new Dimension(700, 500));
		TableColumnModel tcm = tbl.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(THUMB_W + 16);
		tcm.getColumn(0).setMaxWidth(THUMB_W + 24);
		return sp;
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

		// --- Tên loại
		txtTen = new JTextField();
		addRowFixed(p, gbc, "Tên loại:", txtTen);

		// --- Giá
		txtGia = new JTextField();
		addRowFixed(p, gbc, "Giá (₫):", txtGia);

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
		gbc.gridwidth = 4;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(lbImg, gbc);

		// --- Preview
		lblPreview = new JLabel(scaledOrPlaceholder(null, PREVIEW_W, PREVIEW_H));
		fixSize(lblPreview, PREVIEW_W, PREVIEW_H);
		lblPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
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

	private void fixSize(JComponent c, int w, int h) {
		Dimension d = new Dimension(w, h);
		c.setPreferredSize(d);
		c.setMinimumSize(d);
		c.setMaximumSize(d);
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

	// ====== Data binding ======
	private void reloadTable() {
		fillTable(repo.findAll());
	}

	private void fillTable(List<SanPham> data) {
		mdl.setRowCount(0);
		for (SanPham sp : data) {
			ImageIcon icon = scaledOrPlaceholder(sp.getHinhAnh(), THUMB_W, THUMB_H);
			String giaStr = formatGia(sp.getGia());
			mdl.addRow(new Object[] { icon, sp.getMa(), sp.getTen(), sp.getMoTa(), giaStr, sp.isHoatDong() });
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
		txtMoTa.setText("");
		chkActive.setSelected(true);
		txtPathAnh.setText("");
		lblPreview.setIcon(scaledOrPlaceholder(null, PREVIEW_W, PREVIEW_H));
		tbl.clearSelection();
		txtTen.requestFocus();
	}

	private void fillForm(SanPham sp) {
		txtMa.setText(sp.getMa());
		txtTen.setText(sp.getTen());
		txtGia.setText(formatGia(sp.getGia()));
		txtMoTa.setText(sp.getMoTa());
		chkActive.setSelected(sp.isHoatDong());
		txtPathAnh.setText(sp.getHinhAnh() == null ? "" : sp.getHinhAnh());
		lblPreview.setIcon(scaledOrPlaceholder(sp.getHinhAnh(), PREVIEW_W, PREVIEW_H));
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
				JOptionPane.showMessageDialog(this, "Tên loại không được rỗng!");
				txtTen.requestFocus();
				return;
			}
			double gia = parseGia(txtGia.getText().trim());
			if (gia < 0) {
				JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
				txtGia.requestFocus();
				return;
			}
			String ma = repo.nextId();
			String moTa = txtMoTa.getText().trim();
			boolean active = chkActive.isSelected();
			String path = txtPathAnh.getText().trim();
			if (path.isEmpty())
				path = null;
			repo.save(new SanPham(ma, ten, moTa, active, path, gia));
			reloadTable();
			selectRowById(ma);
			return;
		}

		if (o.equals(btnLuu)) { // lưu/cập nhật theo mã
			String ma = txtMa.getText().trim();
			String ten = txtTen.getText().trim();
			if (ten.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Tên loại không được rỗng!");
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
			boolean active = chkActive.isSelected();
			String path = txtPathAnh.getText().trim();
			if (path.isEmpty())
				path = null;
			if (ma.isEmpty()) {
				ma = repo.nextId();
				repo.save(new SanPham(ma, ten, moTa, active, path, gia));
			} else
				repo.update(new SanPham(ma, ten, moTa, active, path, gia));
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
			repo.deleteById(ma);
			reloadTable();
			setFormModeNew();
			JOptionPane.showMessageDialog(this, "Đã xoá thành công!");
			return;
		}

		if (o.equals(btnTim) || o.equals(txtSearch)) {
			String kw = txtSearch.getText();
			fillTable(repo.searchByKeyword(kw));
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
				repo.findById(ma).ifPresent(this::fillForm);
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
//			p.repo.save(new SanPham(p.repo.nextId(), "Đồ uống 2", "Các loại nước giải khát", true, null, 15000));
//			p.repo.save(new SanPham(p.repo.nextId(), "Bánh kẹo", "Đồ ngọt các loại", true, null, 12000));
//			p.reloadTable();
//			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			f.setContentPane(p);
//			f.setSize(1140, 660);
//			f.setLocationRelativeTo(null);
//			f.setVisible(true);
//		});
//	}
}
