package com.entity;

public class SanPham {
	private String ma, ten, moTa, hinhAnh;
	private boolean hoatDong;
	private double gia;

	public SanPham(String ma, String ten, String moTa, boolean hoatDong) {
		this(ma, ten, moTa, hoatDong, null, 0.0);
	}

	public SanPham(String ma, String ten, String moTa, boolean hoatDong, String hinhAnh) {
		this(ma, ten, moTa, hoatDong, hinhAnh, 0.0);
	}

	public SanPham(String ma, String ten, String moTa, boolean hoatDong, String hinhAnh, double gia) {
		this.ma = ma;
		this.ten = ten;
		this.moTa = moTa;
		this.hoatDong = hoatDong;
		this.hinhAnh = hinhAnh;
		this.gia = gia;
	}

	public String getMa() {
		return ma;
	}

	public String getTen() {
		return ten;
	}

	public String getMoTa() {
		return moTa;
	}

	public boolean isHoatDong() {
		return hoatDong;
	}

	public String getHinhAnh() {
		return hinhAnh;
	}

	public double getGia() {
		return gia;
	}

	public void setTen(String v) {
		ten = v;
	}

	public void setMoTa(String v) {
		moTa = v;
	}

	public void setHoatDong(boolean v) {
		hoatDong = v;
	}

	public void setHinhAnh(String v) {
		hinhAnh = v;
	}

	public void setGia(double v) {
		gia = v;
	}
}