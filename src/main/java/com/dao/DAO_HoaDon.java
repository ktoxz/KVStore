package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import com.connectDB.ConnectDB;
import com.entity.HoaDon;
import com.entity.CT_HoaDon;

public class DAO_HoaDon {

    /**
     * Phát sinh mã hóa đơn tự động theo định dạng HDyyyyMMddxxxxx
     * @return Mã hóa đơn mới
     */
    public String phatSinhMaHoaDon() {
        String maHD = "";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getCon();

            // Lấy ngày hiện tại
            LocalDate now = LocalDate.now();
            String datePrefix = String.format("HD%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());

            // Đếm số hóa đơn trong ngày
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE maHoaDon LIKE '" + datePrefix + "%'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            count++; // Tăng lên 1 cho hóa đơn mới

            // Tạo mã hóa đơn: HDyyyyMMdd + số thứ tự 5 chữ số
            maHD = datePrefix + String.format("%05d", count);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maHD;
    }

    /**
     * Thêm hóa đơn mới vào database (bao gồm cả chi tiết hóa đơn)
     * @param hoaDon Đối tượng hóa đơn cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean themHoaDon(HoaDon hoaDon) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        PreparedStatement stmtHD = null;
        PreparedStatement stmtCT = null;

        try {
            // Bắt đầu transaction
            con.setAutoCommit(false);

            // Thêm hóa đơn
            String sqlHD = "INSERT INTO HoaDon (maHoaDon, ngayGiaoDich, tienKhach, thue, maKH, maNV) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
            stmtHD = con.prepareStatement(sqlHD);
            stmtHD.setString(1, hoaDon.getMaHoaDon());
            stmtHD.setDate(2, java.sql.Date.valueOf(hoaDon.getNgayGiaoDich()));
            stmtHD.setDouble(3, hoaDon.getTienKhach());
            stmtHD.setDouble(4, hoaDon.getThue());
            stmtHD.setString(5, hoaDon.getKhachHang().getMaKH());
            stmtHD.setString(6, hoaDon.getNhanVien().getMaNv());

            int n = stmtHD.executeUpdate();

            if (n > 0) {
                // Thêm chi tiết hóa đơn
                String sqlCT = "INSERT INTO CT_HoaDon (maHoaDon, maSP, soLuong) VALUES (?, ?, ?)";
                stmtCT = con.prepareStatement(sqlCT);

                for (CT_HoaDon ct : hoaDon.getChiTietList()) {
                    stmtCT.setString(1, hoaDon.getMaHoaDon());
                    stmtCT.setString(2, ct.getSanPham().getMaSP());
                    stmtCT.setInt(3, ct.getSoLuong());
                    stmtCT.addBatch();
                }

                stmtCT.executeBatch();

                // Commit transaction
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (stmtHD != null) stmtHD.close();
                if (stmtCT != null) stmtCT.close();
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lấy số lần xuất hóa đơn của một khách hàng
     * @param maKH Mã khách hàng
     * @return Số lần xuất hóa đơn
     */
    public int getSoLanXuatHoaDon(String maKH) {
        int soLan = 0;
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getCon();
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE maKH = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maKH);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                soLan = rs.getInt(1);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return soLan;
    }
}


