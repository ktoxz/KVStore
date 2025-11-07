package com.entity;

public class SanPham {
	private String maSP;
	private String tenSP;
	private double giaSP;
	private String moTaSP;
	private String hinhAnhSP;
	private boolean tinhTrangSP;
	private String loaiSP;

	// --- Constructor đầy đủ ---
	public SanPham(String maSP, String tenSP, double giaSP, String moTaSP, String hinhAnhSP, boolean tinhTrangSP, String loaiSP) {
		this.maSP = maSP;
		this.tenSP = tenSP;
		this.giaSP = giaSP;
		this.moTaSP = moTaSP;
		this.hinhAnhSP = hinhAnhSP;
		this.tinhTrangSP = tinhTrangSP;
		this.loaiSP = loaiSP;
	}

	// --- Constructor rút gọn ---
	public SanPham(String maSP, String tenSP, double giaSP) {
		this(maSP, tenSP, giaSP, "", "", true, "");
	}

	public SanPham(String maSP, String tenSP, double giaSP, boolean tinhTrangSP) {
		this(maSP, tenSP, giaSP, "", "", tinhTrangSP, "");
	}

	// --- Getter / Setter ---
	public String getMaSP() {
		return maSP;
	}

	public void setMaSP(String maSP) {
		this.maSP = maSP;
	}

	public String getTenSP() {
		return tenSP;
	}

	public void setTenSP(String tenSP) {
		this.tenSP = tenSP;
	}

	public double getGiaSP() {
		return giaSP;
	}

	public void setGiaSP(double giaSP) {
		this.giaSP = giaSP;
	}

	public String getMoTaSP() {
		return moTaSP;
	}

	public void setMoTaSP(String moTaSP) {
		this.moTaSP = moTaSP;
	}

	public String getHinhAnhSP() {
		return hinhAnhSP;
	}

	public void setHinhAnhSP(String hinhAnhSP) {
		this.hinhAnhSP = hinhAnhSP;
	}

	public boolean isTinhTrangSP() {
		return tinhTrangSP;
	}

	public void setTinhTrangSP(boolean tinhTrangSP) {
		this.tinhTrangSP = tinhTrangSP;
	}

	public String getLoaiSP() {
		return loaiSP;
	}

	public void setLoaiSP(String loaiSP) {
		this.loaiSP = loaiSP;
	}

	@Override
	public String toString() {
		return String.format("%s - %s - %.0fđ (%s)", maSP, tenSP, giaSP, loaiSP);
	}
}
