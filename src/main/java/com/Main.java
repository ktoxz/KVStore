package com;

import com.connectDB.ConnectDB;
import com.dao.DAO_SanPham;
import com.dao.DAO_CT_KhuyenMai;
import com.entity.SanPham;
import com.entity.CT_KhuyenMai;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.gui.GUI_Login;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectDB.getInstance().connect();
            System.out.println("✅ Kết nối CSDL thành công!");
        } catch (SQLException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Failed to connect to database: " + ex.getMessage(),
                    "DB Connection Error",
                    JOptionPane.ERROR_MESSAGE));
            ex.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ConnectDB.getInstance().disconnect();
                System.out.println("🔌 Đã ngắt kết nối CSDL.");
            } catch (Exception ignored) {}
        }));

        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception ignored) {}

        try {
            DAO_SanPham daoSP = new DAO_SanPham();
            DAO_CT_KhuyenMai daoKM = new DAO_CT_KhuyenMai();

            List<SanPham> sps = daoSP.getAllSanPham();

            System.out.println("=== 🛒 Danh sách sản phẩm có khuyến mãi ===");
            for (SanPham sp : sps) {
                try {
                    List<CT_KhuyenMai> kms = daoKM.findBySanPham(sp.getMaSP());
                    if (!kms.isEmpty()) {
                        System.out.println("➡️ " + sp.getMaSP() + " - " + sp.getTenSP());
                        for (CT_KhuyenMai km : kms) {
                            System.out.printf("   • KM: %-25s | Loại: %-20s | Giá trị: %.2f%n",
                                    km.getKhuyenMai().getTenKM(),
                                    km.getLoaiKM(),
                                    km.getGiaTri());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("⚠️ Lỗi khi truy vấn KM cho SP " + sp.getMaSP() + ": " + e.getMessage());
                }
            }
            System.out.println("==========================================");

        } catch (Exception e) {
            System.err.println("⚠️ Lỗi debug khuyến mãi: " + e.getMessage());
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KVStore - Đăng nhập");
            GUI_Login loginPanel = new GUI_Login();
            loginPanel.setParentFrame(frame);
            frame.setContentPane(loginPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1080, 600);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
