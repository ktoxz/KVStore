package com.gui;

import com.entity.NhanVien;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI_General extends JPanel {
    private static final Logger log = LogManager.getLogger(GUI_General.class);
    private NhanVien nhanVien;
    private JPanel contentPanel;
    private JPanel currentTabPanel;
    private TAB_KhuyenMai tabKhuyenMai;
    private TAB_HuongDan tabHuongDan;
    private TAB_KhachHang tabKhachHang;
    private TAB_BanHang tabBanHang;
    private TAB_NhanVien tabNhanVien;
    private TAB_SanPham tabSanPham;
    private TAB_ManHinhChinh tabManHinhChinh;

    public GUI_General(NhanVien nhanVien) {
        if(nhanVien == null) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: Nhân viên không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.nhanVien = nhanVien;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabKhuyenMai = new TAB_KhuyenMai();
        tabHuongDan = new TAB_HuongDan();
        tabKhachHang = new TAB_KhachHang();
        tabBanHang = new TAB_BanHang(nhanVien);
        tabNhanVien = new TAB_NhanVien();
        tabSanPham = new TAB_SanPham();
        tabManHinhChinh = new TAB_ManHinhChinh();
        tabManHinhChinh.setNhanVien(nhanVien);

        // ================= HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 122, 255));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // ===== App name =====
        JLabel lblAppName = new JLabel("KVStore - Hệ thống quản lý cửa hàng");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblAppName.setForeground(Color.WHITE);
        headerPanel.add(lblAppName, BorderLayout.WEST);

        // ================= HEADER RIGHT (Xin chào + Đăng xuất) =================
        JPanel headerRight = new JPanel();
        headerRight.setOpaque(false);
        headerRight.setLayout(new BoxLayout(headerRight, BoxLayout.X_AXIS));

        // Lời chào
        String ten = nhanVien != null ? nhanVien.getTenNV() : "";
        String chucVu = (nhanVien != null && nhanVien.getChucVu() != null) ? nhanVien.getChucVu().toString() : "";

        JLabel lblHello = new JLabel("Xin chào, " + ten + (chucVu.isEmpty() ? "" : " (" + chucVu + ")"));
        lblHello.setForeground(Color.WHITE);
        lblHello.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Nút đăng xuất
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(new Color(0, 122, 255));
        btnLogout.setFocusPainted(false);
        btnLogout.setPreferredSize(new Dimension(120, 40));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        // Thêm vào headerRight
        headerRight.add(lblHello);
        headerRight.add(Box.createHorizontalStrut(15));  // khoảng cách giữa text & nút
        headerRight.add(btnLogout);

        // Gắn vào header phải
        headerPanel.add(headerRight, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ================= SIDEBAR PANEL =================
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(245, 248, 255));
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        addTabButtons(sidebarPanel);
        add(sidebarPanel, BorderLayout.WEST);

        // ================= CONTENT PANEL =================
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // Mặc định hiển thị màn hình chính
        showTab(tabManHinhChinh);

        add(contentPanel, BorderLayout.CENTER);
    }


    private void addTabButtons(JPanel sidebarPanel) {
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tab Màn hình chính (thay thế Dashboard)
        JButton btnDashboard = createTabButton("Màn hình chính", "🏠");
        btnDashboard.addActionListener(e -> showTab(tabManHinhChinh));
        sidebarPanel.add(btnDashboard);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Bán hàng
        JButton btnBanHang = createTabButton("Bán hàng", "🛒");
        btnBanHang.addActionListener(e -> showTab(tabBanHang));
        sidebarPanel.add(btnBanHang);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Sản phẩm
        JButton btnSanPham = createTabButton("Sản phẩm", "📦");
        btnSanPham.addActionListener(e -> showTab(tabSanPham));
        sidebarPanel.add(btnSanPham);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Khách hàng
        JButton btnKhachHang = createTabButton("Khách hàng", "👥");
        btnKhachHang.addActionListener(e -> showTab(tabKhachHang));
        sidebarPanel.add(btnKhachHang);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));


        // Tab Nhân Viên
        JButton btnNhanVien = createTabButton("Nhân Viên", "📖");
        btnNhanVien.addActionListener(e -> showTab(tabNhanVien));
        sidebarPanel.add(btnNhanVien);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Khuyến mãi
        JButton btnKhuyenMai = createTabButton("Khuyến mãi", "🎁");
        btnKhuyenMai.addActionListener(e -> showTab(tabKhuyenMai));
        sidebarPanel.add(btnKhuyenMai);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Hướng dẫn
        JButton btnHuongDan = createTabButton("Hướng dẫn", "📖");
        btnHuongDan.addActionListener(e -> showTab(tabHuongDan));
        sidebarPanel.add(btnHuongDan);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Thống kê
        JButton btnThongKe = createTabButton("Thống kê", "📖");
        btnThongKe.addActionListener(e -> showTab(new TAB_ThongKe()));
        sidebarPanel.add(btnThongKe);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        sidebarPanel.add(Box.createVerticalGlue());
    }

    private JButton createTabButton(String text, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(250, 50));
        button.setPreferredSize(new Dimension(250, 50));
        button.setBackground(new Color(245, 248, 255));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 240, 255));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(245, 248, 255));
            }
        });

        return button;
    }

    private void showTab(JPanel tabPanel) {
        // Xóa tab hiện tại
        if (currentTabPanel != null) {
            contentPanel.remove(currentTabPanel);
        }

        if(tabPanel instanceof TAB_ManHinhChinh) {
            ((TAB_ManHinhChinh) tabPanel).setNhanVien(nhanVien);
        } else if (tabPanel instanceof TAB_BanHang) {
            ((TAB_BanHang) tabPanel).setNhanVien(nhanVien);
        }

        // Hiển thị tab mới
        currentTabPanel = tabPanel;
        contentPanel.add(currentTabPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Phương thức để thêm tab tùy chỉnh từ bên ngoài
    public void addCustomTab(String title, String icon, JPanel panel) {
        // Tìm sidebar panel
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel checkPanel = (JPanel) comp;
                if (checkPanel.getLayout() instanceof BoxLayout) {
                    JButton btnNewTab = createTabButton(title, icon);
                    btnNewTab.addActionListener(e -> showTab(panel));
                    checkPanel.add(btnNewTab);
                    checkPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                    checkPanel.revalidate();
                    checkPanel.repaint();
                    break;
                }
            }
        }
    }


    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Đóng cửa sổ hiện tại
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                window.dispose();
            }

            // Mở lại màn hình đăng nhập
            SwingUtilities.invokeLater(() -> {
                JFrame loginFrame = new JFrame("KVStore - Đăng nhập");
                com.gui.GUI_Login loginPanel = new com.gui.GUI_Login();
                loginPanel.setParentFrame(loginFrame);
                loginFrame.setContentPane(loginPanel);
                loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                loginFrame.setSize(1080, 600);
                loginFrame.pack();
                loginFrame.setLocationRelativeTo(null);
                loginFrame.setResizable(false);
                loginFrame.setVisible(true);
            });
        }
    }
}
