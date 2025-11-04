package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUI_Login extends JPanel {
    private JFrame parentFrame;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblForgot;

    public GUI_Login() {
        setLayout(new BorderLayout());

        // ===== LEFT PANEL (Image) =====
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(245, 248, 255)); // nhẹ, không trắng gắt
        leftPanel.setPreferredSize(new Dimension(520, 600));

        ImageIcon icon = new ImageIcon("src/main/resources/login_img.png");
        Image scaled = icon.getImage().getScaledInstance(420, 420, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
        leftPanel.add(imgLabel);

        // ===== RIGHT PANEL (Login Form) =====
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(10, 0, 10, 0);

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel lblUser = new JLabel("Username");
        txtUsername = new JTextField(22);

        JLabel lblPass = new JLabel("Password");
        txtPassword = new JPasswordField(22);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setBackground(new Color(0, 122, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(140, 38));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));



        lblForgot = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        lblForgot.setForeground(new Color(0, 122, 255));
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Thêm sự kiện Enter cho password field
        txtPassword.addActionListener(e -> btnLogin.doClick());

        // Layout the components
        gc.gridy = 0;
        rightPanel.add(lblTitle, gc);
        gc.gridy++;
        rightPanel.add(lblUser, gc);
        gc.gridy++;
        rightPanel.add(txtUsername, gc);
        gc.gridy++;
        rightPanel.add(lblPass, gc);
        gc.gridy++;
        rightPanel.add(txtPassword, gc);
        gc.gridy++;

        JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowButtons.setBackground(Color.WHITE);
        rowButtons.add(btnLogin);
        rowButtons.add(Box.createHorizontalStrut(15));
        rowButtons.add(lblForgot);

        rightPanel.add(rowButtons, gc);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        // Thêm ActionListener cho nút đăng nhập
        btnLogin.addActionListener(e -> handleLogin());

    }

    /**
     * Phương thức để set JFrame cha - cần gọi từ Main
     */
    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    /**
     * Phương thức xử lý đăng nhập - hiển thị thông báo và mở GUI_General
     */
    private void handleLogin() {

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin đăng nhập!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiển thị thông báo đăng nhập thành công
        JOptionPane.showMessageDialog(this,
                "Đăng nhập thành công!\nChào mừng " + username,
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);

        // Đóng cửa sổ login và mở GUI_General
        openMainWindow();
    }

    /**
     * Mở cửa sổ chính GUI_General và đóng cửa sổ login
     */
    private void openMainWindow() {
        // Tìm JFrame cha nếu chưa được set
        if (parentFrame == null) {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                parentFrame = (JFrame) window;
            }
        }

        // Tạo và hiển thị GUI_General
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame("KVStore - Quản lý cửa hàng");
            mainFrame.setContentPane(new GUI_General());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Toàn màn hình
            mainFrame.setResizable(false); // Không resize được
            mainFrame.setVisible(true);

            // Đóng cửa sổ login
            if (parentFrame != null) {
                parentFrame.dispose();
            }
        });
    }

    /**
     * Login
     */

}
