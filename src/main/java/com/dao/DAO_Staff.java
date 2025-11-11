package com.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.connectDB.ConnectDB;

public class DAO_Staff {
    /**
     * Lấy mã nhân viên đầu tiên trong database
     * @return Mã nhân viên hoặc null nếu không có
     */
    public String getFirstMaNV() {
        String maNV = null;
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getCon();
            String sql = "SELECT TOP 1 maNV FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                maNV = rs.getString(1);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maNV;
    }
}
