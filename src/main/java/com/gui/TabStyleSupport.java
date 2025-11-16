package com.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

/**
 * Reusable styling helpers so every management tab can align its header, fonts,
 * and section borders without duplicating boilerplate.
 */
public interface TabStyleSupport {
    String HEADER_PROPERTY = "tabHeader";

    Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 26);
    Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 18);
    Font CONTENT_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    Color HEADER_COLOR = new Color(0, 90, 200);

    default JPanel createHeader(String title) {
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(HEADER_COLOR);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        header.add(lblTitle);
        header.putClientProperty(HEADER_PROPERTY, Boolean.TRUE);
        return header;
    }

    default void applyContentFont(Component component) {
        if (component == null) {
            return;
        }
        if (component instanceof JComponent jComponent) {
            Object flag = jComponent.getClientProperty(HEADER_PROPERTY);
            if (Boolean.TRUE.equals(flag)) {
                return;
            }
        }
        Font font = component.getFont();
        if (font == null || font instanceof FontUIResource) {
            component.setFont(CONTENT_FONT);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyContentFont(child);
            }
        }
    }

    default void markCustomFont(JComponent component) {
        if (component != null) {
            component.putClientProperty(HEADER_PROPERTY, Boolean.TRUE);
        }
    }

    default TitledBorder createSectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(SECTION_FONT);
        border.setTitleColor(HEADER_COLOR);
        return border;
    }

    default void applySectionTitleFont(TitledBorder border) {
        if (border != null) {
            border.setTitleFont(SECTION_FONT);
            border.setTitleColor(HEADER_COLOR);
        }
    }
}
