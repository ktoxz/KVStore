package com.entity;

import java.sql.Date;

public class KhuyenMai {
    private int maKM;
    private String tenKM;
    private String moTaKM;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    
    // (CSDL mới không có loaiKM trong bảng KhuyenMai)

    // Constructor rỗng
    public KhuyenMai() {
    }

    // Constructor đầy đủ (dùng cho SELECT và UPDATE)
    public KhuyenMai(int maKM, String tenKM, String moTaKM, Date ngayBatDau, Date ngayKetThuc) {
        this.maKM = maKM;
        this.tenKM = tenKM;
        this.moTaKM = moTaKM;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    // Constructor cho INSERT (không cần maKM vì nó là IDENTITY)
    // ✅ Đây là hàm mà code GUI của bạn đang cố gắng gọi
    public KhuyenMai(String tenKM, String moTaKM, Date ngayBatDau, Date ngayKetThuc) {
        this.tenKM = tenKM;
        this.moTaKM = moTaKM;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public KhuyenMai(int maKM) {
    }

    // --- Getters and Setters ---
    
    public int getMaKM() {
        return maKM;
    }

    public void setMaKM(int maKM) {
        this.maKM = maKM;
    }

    public String getTenKM() {
        return tenKM;
    }

    public void setTenKM(String tenKM) {
        this.tenKM = tenKM;
    }

    public String getMoTaKM() {
        return moTaKM;
    }

    public void setMoTaKM(String moTaKM) {
        this.moTaKM = moTaKM;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
}