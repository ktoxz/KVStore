package com.entity;

public class CT_HoaDon {
    private String maHoaDon;
    private String maSP;
    private int soLuong;

    // Thông tin bổ sung (không lưu trong DB nhưng dùng để hiển thị)
    private String tenSP;
    private double giaSP;

    public CT_HoaDon() {
    }

    public CT_HoaDon(String maHoaDon, String maSP, int soLuong) {
        this.maHoaDon = maHoaDon;
        this.maSP = maSP;
        this.soLuong = soLuong;
    }

    public CT_HoaDon(String maHoaDon, String maSP, int soLuong, String tenSP, double giaSP) {
        this.maHoaDon = maHoaDon;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.tenSP = tenSP;
        this.giaSP = giaSP;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
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

    public double getThanhTien() {
        return soLuong * giaSP;
    }

    @Override
    public String toString() {
        return "CT_HoaDon [maHoaDon=" + maHoaDon + ", maSP=" + maSP + ", soLuong=" + soLuong + "]";
    }
}

