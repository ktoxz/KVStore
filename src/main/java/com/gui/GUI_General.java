package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI_General extends JPanel {
    private JPanel contentPanel;
    private JPanel currentTabPanel;

    public GUI_General() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ===== HEADER PANEL (1/5 chiều cao) =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 122, 255));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Tên ứng dụng
        JLabel lblAppName = new JLabel("KVStore - Hệ thống quản lý cửa hàng");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblAppName.setForeground(Color.WHITE);
        headerPanel.add(lblAppName, BorderLayout.WEST);

        // Nút đăng xuất
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(new Color(0, 122, 255));
        btnLogout.setFocusPainted(false);
        btnLogout.setPreferredSize(new Dimension(120, 40));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
        headerPanel.add(btnLogout, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ===== SIDEBAR PANEL (1/5 chiều rộng) =====
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(245, 248, 255));
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        // Thêm các tab button vào sidebar
        addTabButtons(sidebarPanel);

        add(sidebarPanel, BorderLayout.WEST);

        // ===== CONTENT PANEL (phần còn lại) =====
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // Mặc định hiển thị Dashboard
        showTab(createDashboardTab());

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addTabButtons(JPanel sidebarPanel) {
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tab Dashboard
        JButton btnDashboard = createTabButton("Dashboard", "🏠");
        btnDashboard.addActionListener(e -> showTab(createDashboardTab()));
        sidebarPanel.add(btnDashboard);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Bán hàng
        JButton btnBanHang = createTabButton("Bán hàng", "🛒");
        btnBanHang.addActionListener(e -> showTab(new TAB_BanHang()));
        sidebarPanel.add(btnBanHang);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Sản phẩm
        JButton btnSanPham = createTabButton("Sản phẩm", "📦");
        btnSanPham.addActionListener(e -> showTab(new TAB_SanPham()));
        sidebarPanel.add(btnSanPham);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Khách hàng
        JButton btnKhachHang = createTabButton("Khách hàng", "👥");
        btnKhachHang.addActionListener(e -> showTab(new TAB_KhachHang()));
        sidebarPanel.add(btnKhachHang);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Khuyến mãi
        JButton btnKhuyenMai = createTabButton("Khuyến mãi", "🎁");
        btnKhuyenMai.addActionListener(e -> showTab(new TAB_KhuyenMai()));
        sidebarPanel.add(btnKhuyenMai);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab Hướng dẫn
        JButton btnHuongDan = createTabButton("Hướng dẫn", "📖");
        btnHuongDan.addActionListener(e -> showTab(new TAB_HuongDan()));
        sidebarPanel.add(btnHuongDan);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Bạn có thể thêm các tab khác tại đây
        // Tab Hướng dẫn
        JButton btnNhanVien = createTabButton("Nhân Viên", "📖");
        btnHuongDan.addActionListener(e -> showTab(new TAB_NhanVien()));
        sidebarPanel.add(btnNhanVien);

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

    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);

        JLabel lblWelcome = new JLabel("Chào mừng đến với hệ thống quản lý KVStore", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(new Color(50, 50, 50));
        panel.add(lblWelcome, BorderLayout.CENTER);

        return panel;
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
                loginFrame.setLocationRelativeTo(null);
                loginFrame.setResizable(false);
                loginFrame.setVisible(true);
            });
        }
    }
}
