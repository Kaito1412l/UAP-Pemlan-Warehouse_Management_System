package wms.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

public class LoginApp extends JFrame {

    private static final Map<String, String> USERS = Map.of(
            "admin", "ADMIN",
            "inbound1", "INBOUND",
            "inventory1", "INVENTORY",
            "outbound1", "OUTBOUND"
    );

    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color HOVER_COLOR = new Color(100, 149, 237);
    private final Color BG_COLOR = new Color(236, 240, 241);

    public LoginApp() {
        setTitle("Warehouse Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new GridBagLayout());
        initUI();
    }

    private void initUI() {

        JPanel cardPanel = new RoundedPanel(25, Color.WHITE);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        cardPanel.setPreferredSize(new Dimension(400, 450));

        JLabel titleLabel = new JLabel("Welcome");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("Warehouse Management System");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitleLabel.setForeground(Color.GRAY);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(100, 100, 100));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(userLabel, BorderLayout.WEST);
        labelPanel.setMaximumSize(new Dimension(300, 20));

        RoundedTextField userField = new RoundedTextField(15);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userField.setMaximumSize(new Dimension(300, 40));

        ModernButton loginButton = new ModernButton("LOGIN");
        loginButton.setMaximumSize(new Dimension(300, 45));

        JLabel msgLabel = new JLabel(" ");
        msgLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        msgLabel.setForeground(new Color(231, 76, 60));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(subTitleLabel);
        cardPanel.add(Box.createVerticalStrut(40));

        cardPanel.add(labelPanel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(userField);
        cardPanel.add(Box.createVerticalStrut(30));

        cardPanel.add(loginButton);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(msgLabel);

        add(cardPanel);

        this.getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String role = USERS.get(username);

            if (role == null) {
                msgLabel.setText("Username tidak ditemukan!");
                shakeComponent(cardPanel);
            } else {
                msgLabel.setText("Login sukses...");
                new MainFrame(role, username).setVisible(true);
                this.dispose();
            }
        });
    }

    private void shakeComponent(JComponent component) {
        Point original = component.getLocation();
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    SwingUtilities.invokeLater(() -> component.setLocation(original.x + 5, original.y));
                    Thread.sleep(20);
                    SwingUtilities.invokeLater(() -> component.setLocation(original.x - 5, original.y));
                    Thread.sleep(20);
                }
                SwingUtilities.invokeLater(() -> component.setLocation(original));
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> new LoginApp().setVisible(true));
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, radius, radius);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, radius, radius);
        }
    }

    static class RoundedTextField extends JTextField {
        private Shape shape;
        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 15, 5, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(245, 245, 245));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }

        @Override
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
            return shape.contains(x, y);
        }
    }

    class ModernButton extends JButton {
        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(HOVER_COLOR);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(PRIMARY_COLOR);
                    repaint();
                }
            });
            setBackground(PRIMARY_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            super.paintComponent(g);
        }
    }
}