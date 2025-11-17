package com.entity;

public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private SanPham sanPham;
    private int soLuong;
    private KhuyenMai khuyenMai; // nullable
    private double thanhTien;    // đã gồm giảm giá

    public ChiTietHoaDon(HoaDon hoaDon, SanPham sanPham, int soLuong) {
        this.hoaDon = hoaDon;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.thanhTien = sanPham.getGiaSP() * soLuong; // giá gốc
    }

    public ChiTietHoaDon(HoaDon hoaDon, SanPham sanPham, int soLuong, KhuyenMai km, double thanhTien) {
        this.hoaDon = hoaDon;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.khuyenMai = km;
        this.thanhTien = thanhTien;
    }

    public HoaDon getHoaDon() { return hoaDon; }
    public SanPham getSanPham() { return sanPham; }
    public int getSoLuong() { return soLuong; }
    public double getThanhTien() { return thanhTien; }
    public KhuyenMai getKhuyenMai() { return khuyenMai; }

    public void setKhuyenMai(KhuyenMai km) { this.khuyenMai = km; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
