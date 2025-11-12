package com.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;

public class DAO_Staff {
    /**
     * Lấy mã nhân viên đầu tiên trong database
     * @return Mã nhân viên hoặc null nếu không có
     */
    public NhanVien getFirstNV() {
        String maNV = null;
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getCon();
            String sql = "SELECT TOP 1 * FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                maNV = rs.getString(1);
                String tenNV = rs.getString(2);
                boolean gioiTinh = rs.getBoolean(4);
                String email = rs.getString(5);
                String sdt = rs.getString(6);
                LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
                NhanVien nv = new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan);
                return nv;
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
