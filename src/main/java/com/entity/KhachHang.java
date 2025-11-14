package com.entity;

import java.time.LocalDate;
import java.util.Objects;

public class KhachHang {
	private String maKH;
	private String tenKH;
	private boolean gioiTinh;
	private String sdt;
	private LocalDate ngayTao;
	private int diemTichLuy;
	public KhachHang() {
		
	}

	public KhachHang(String maKH) {
		this.maKH = maKH;
	}

	public KhachHang(String maKH, String tenKH, boolean gioiTinh, String sdt, LocalDate ngayTao, int diemTichLuy) {
		this.maKH = maKH;
		this.tenKH = tenKH;
		this.gioiTinh = gioiTinh;
		this.sdt = sdt;
		this.ngayTao = ngayTao;
		this.diemTichLuy = diemTichLuy;
	}
	public String getTenKH() {
		return tenKH;
	}
	public void setTenKH(String tenKH) {
		this.tenKH = tenKH;
	}
	public boolean isGioiTinh() {
		return gioiTinh;
	}
	public void setGioiTinh(boolean gioiTinh) {
		this.gioiTinh = gioiTinh;
	}
	public String getSdt() {
		return sdt;
	}
	public void setSdt(String sdt) {
		this.sdt = sdt;
	}
	public LocalDate getNgayTao() {
		return ngayTao;
	}
	public void setNgayTao(LocalDate ngayTao) {
		this.ngayTao = ngayTao;
	}
	public int getDiemTichLuy() {
		return diemTichLuy;
	}
	public void setDiemTichLuy(int diemTichLuy) {
		this.diemTichLuy = diemTichLuy;
	}
	public String getMaKH() {
		return maKH;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(sdt);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhachHang other = (KhachHang) obj;
		return Objects.equals(sdt, other.sdt);
	}
	@Override
	public String toString() {
		return "KhachHang [maKH=" + maKH + ", tenKH=" + tenKH + ", gioiTinh=" + gioiTinh + ", sdt=" + sdt + ", ngayTao="
				+ ngayTao + ", diemTichLuy=" + diemTichLuy + "]";
	}

}
