package com.entity;

import java.time.LocalDate;

public class NhanVien {
	private String maNV;
	private String tenNV;
	private boolean gioiTinh;
	private String email;
	private String sdt;
	private LocalDate ngayTaoTaiKhoan;
	private String chucVu;	

	public NhanVien() {
		super();
	}

	public NhanVien(String maNV,
			String tenNV, 
			boolean gioiTinh, 
			String email, 
			String sdt, 
			LocalDate ngayTaoTaiKhoan,
			String chucVu) {
		this.maNV = maNV;
		this.tenNV = tenNV;
		this.gioiTinh = gioiTinh;
		this.email = email;
		this.sdt = sdt;
		this.ngayTaoTaiKhoan = ngayTaoTaiKhoan;
		this.chucVu = chucVu;
	}
	
	public String getMaNV() { return maNV; }
	public void setMaNV(String maNV) { this.maNV = maNV; }
	public String getTenNV() { return tenNV; }
	public void setTenNV(String tenNV) { this.tenNV = tenNV; }
	public boolean isGioiTinh() { return gioiTinh; }
	public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getSdt() { return sdt; }
	public void setSdt(String sdt) { this.sdt = sdt; }
	public LocalDate getNgayTaoTaiKhoan() { return ngayTaoTaiKhoan; }
	public void setNgayTaoTaiKhoan(LocalDate ngayTaoTaiKhoan) { this.ngayTaoTaiKhoan = ngayTaoTaiKhoan; }
	public String getChucVu() { return chucVu; }
	public void setChucVu(String chucVu) { this.chucVu = chucVu; }
}
