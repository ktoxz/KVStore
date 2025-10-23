// java
package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 Updated GUI_Login as a JPanel to be compatible with Main which does:
 frame.setContentPane(new GUI_Login());
*/
public class GUI_Login extends JPanel {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton btnLogin;
    private final JButton btnCancel;

    public GUI_Login() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(16);
        form.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(16);
        form.add(passwordField, gbc);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");
        buttons.add(btnCancel);
        buttons.add(btnLogin);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // Actions
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLogin();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // Ensure default button is set after this panel is added to a root pane
        SwingUtilities.invokeLater(() -> {
            JRootPane rp = SwingUtilities.getRootPane(GUI_Login.this);
            if (rp != null) {
                rp.setDefaultButton(btnLogin);
            }
        });
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean authOk = "admin".equals(username) && java.util.Arrays.equals(password, "admin".toCharArray());

        // Clear password array for security
        java.util.Arrays.fill(password, '\0');

        if (authOk) {
            JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            openMainWindow(username);
            // Close the top-level window that contains this panel (the frame created in Main)
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void onCancel() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.dispose();
        }
    }

    private void openMainWindow(String username) {
        JFrame main = new JFrame("KVStore - Main");
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setSize(480, 320);
        main.setLocationRelativeTo(null);
        JLabel welcome = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        main.add(welcome);
        main.setVisible(true);
    }
}
