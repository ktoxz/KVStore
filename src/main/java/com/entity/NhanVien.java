package com.entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNv;
    private String tenNv;
    private boolean gioiTinh;
    private String email;
    private String sdt;
    private LocalDate ngayTaoTaiKhoan;

    public NhanVien(String maNv, String tenNv, boolean gioiTinh, String email, String sdt, LocalDate ngayTaoTaiKhoan) {
        this.maNv = maNv;
        this.tenNv = tenNv;
        this.gioiTinh = gioiTinh;
        this.email = email;
        this.sdt = sdt;
        this.ngayTaoTaiKhoan = ngayTaoTaiKhoan;
    }

    public NhanVien(String maNv){
        this.maNv = maNv;
    }

    public String getMaNv() {
        return maNv;
    }

    public String getTenNv() {
        return tenNv;
    }

    public void setTenNv(String tenNv) {
        this.tenNv = tenNv;
    }

    public boolean isGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public LocalDate getNgayTaoTaiKhoan() {
        return ngayTaoTaiKhoan;
    }

    public void setNgayTaoTaiKhoan(LocalDate ngayTaoTaiKhoan) {
        this.ngayTaoTaiKhoan = ngayTaoTaiKhoan;
    }
}
