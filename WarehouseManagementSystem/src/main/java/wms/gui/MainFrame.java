package wms.gui;

import wms.core.service.*;
import wms.core.repository.ExcelRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private final AdminService adminService = new AdminService();
    private final InboundService inboundService = new InboundService();
    private final InventoryService inventoryService = new InventoryService();
    private final OutboundService outboundService = new OutboundService();

    private final String currentRole;
    private final String currentUsername;

    private final Color BG_COLOR = new Color(236, 240, 241);
    private final Color ADMIN_COLOR = new Color(70, 130, 180);
    private final Color OPERATOR_COLOR = new Color(39, 174, 96);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);

    public MainFrame(String role, String username) {
        this.currentRole = role;
        this.currentUsername = username;

        setTitle("Warehouse Management System - " + role);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        initUI();
    }

    private void initUI() {
        if ("ADMIN".equals(currentRole)) {
            add(createAdminPanel());
        } else {
            add(createOperatorPanel());
        }
    }

    private void loadLocationsToTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<String> locations = ExcelRepository.get().locations();

        for (String loc : locations) {
            model.addRow(new String[]{loc});
        }
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ADMIN_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("User: " + currentUsername);
        userLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        userLabel.setForeground(new Color(220, 220, 220));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Daftar Lokasi Gudang"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        styleTable(table, ADMIN_COLOR);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadLocationsToTable(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setBackground(BG_COLOR);

        ModernButton addLocBtn = new ModernButton("Tambah Lokasi", new Color(243, 156, 18)); // Orange
        ModernButton logoutBtn = new ModernButton("Logout", DANGER_COLOR);

        btnPanel.add(addLocBtn);
        btnPanel.add(logoutBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        addLocBtn.addActionListener(e -> {
            String loc = JOptionPane.showInputDialog(this, "Masukkan kode lokasi baru:");
            if (loc != null && !loc.trim().isEmpty()) {
                try {
                    adminService.addLocation(loc.trim());
                    loadLocationsToTable(model);
                    JOptionPane.showMessageDialog(this, "Lokasi berhasil ditambahkan.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        logoutBtn.addActionListener(e -> logout());

        return panel;
    }

    private JPanel createOperatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(OPERATOR_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Operator Menu: " + currentRole);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("User: " + currentUsername);
        userLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        userLabel.setForeground(new Color(220, 220, 220));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // --- Content ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createLineBorder(new Color(230,230,230), 1));

        JLabel welcomeIcon = new JLabel("<html><div style='text-align: center;'><h2>Selamat Bekerja, " + currentUsername + "</h2>" +
                "<p>Silakan pilih tugas operasional di bawah ini.</p></div></html>");
        welcomeIcon.setFont(MAIN_FONT);
        centerPanel.add(welcomeIcon);

        panel.add(centerPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(BG_COLOR);

        ModernButton actionBtn = new ModernButton("Jalankan Tugas", OPERATOR_COLOR);
        actionBtn.setPreferredSize(new Dimension(200, 50));

        ModernButton logoutBtn = new ModernButton("Logout", DANGER_COLOR);
        logoutBtn.setPreferredSize(new Dimension(150, 50));

        btnPanel.add(actionBtn);
        btnPanel.add(logoutBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        actionBtn.addActionListener(e -> performOperatorTask());
        logoutBtn.addActionListener(e -> logout());

        return panel;
    }

    private void performOperatorTask() {
        switch (currentRole) {
            case "INBOUND" -> inboundDialog();
            case "INVENTORY" -> relocateDialog();
            case "OUTBOUND" -> outboundDialog();
            default -> JOptionPane.showMessageDialog(this, "Role tidak dikenali.");
        }
    }

    private void inboundDialog() {
        RoundedTextField upcField = new RoundedTextField(20);
        RoundedTextField manuField = new RoundedTextField(20);
        RoundedTextField catField = new RoundedTextField(20);
        RoundedTextField qtyField = new RoundedTextField(20);
        RoundedTextField locField = new RoundedTextField(20);

        Object[] message = {
                "UPC:", upcField,
                "Manufacturer:", manuField,
                "Category:", catField,
                "Qty:", qtyField,
                "Location:", locField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Inbound Barang", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                inboundService.receive(upcField.getText(), manuField.getText(), catField.getText(),
                        Integer.parseInt(qtyField.getText()), locField.getText());
                JOptionPane.showMessageDialog(this, "Inbound berhasil.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void relocateDialog() {
        RoundedTextField upcField = new RoundedTextField(20);
        RoundedTextField skuField = new RoundedTextField(20);
        RoundedTextField fromField = new RoundedTextField(20);
        RoundedTextField toField = new RoundedTextField(20);
        RoundedTextField qtyField = new RoundedTextField(20);

        Object[] message = {
                "UPC:", upcField,
                "SKU:", skuField,
                "From Location:", fromField,
                "To Location:", toField,
                "Qty:", qtyField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Relocate Barang", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                inventoryService.relocate(upcField.getText(), skuField.getText(), fromField.getText(),
                        toField.getText(), Integer.parseInt(qtyField.getText()));
                JOptionPane.showMessageDialog(this, "Relocate berhasil.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void outboundDialog() {
        RoundedTextField upcField = new RoundedTextField(20);
        RoundedTextField locField = new RoundedTextField(20);
        RoundedTextField qtyField = new RoundedTextField(20);

        Object[] message = {
                "UPC:", upcField,
                "Location:", locField,
                "Qty:", qtyField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Outbound Barang", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                outboundService.pick(upcField.getText(), locField.getText(), Integer.parseInt(qtyField.getText()));
                JOptionPane.showMessageDialog(this, "Outbound berhasil.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void logout() {
        new LoginApp().setVisible(true);
        this.dispose();
    }

    private void styleTable(JTable table, Color headerColor) {
        table.setFont(MAIN_FONT);
        table.setRowHeight(35);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Styling
                l.setBackground(headerColor);
                l.setForeground(Color.WHITE);
                l.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                l.setFont(new Font("Segoe UI", Font.BOLD, 14));

                l.setIcon(null);
                if (table.getRowSorter() != null) {
                    List<? extends RowSorter.SortKey> keys = table.getRowSorter().getSortKeys();
                    if (keys != null && !keys.isEmpty()) {
                        RowSorter.SortKey key = keys.get(0);
                        if (key.getColumn() == column) {
                            if (key.getSortOrder() == SortOrder.ASCENDING) {
                                l.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));
                            } else if (key.getSortOrder() == SortOrder.DESCENDING) {
                                l.setIcon(UIManager.getIcon("Table.descendingSortIcon"));
                            }
                        }
                    }
                }

                return l;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int i=0; i<table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    static class RoundedTextField extends JTextField {
        private Shape shape;
        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            super.paintComponent(g);
        }
        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(189, 195, 199));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }
        @Override
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
            return shape.contains(x, y);
        }
    }

    static class ModernButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;

        public ModernButton(String text, Color color) {
            super(text);
            this.baseColor = color;
            this.hoverColor = color.brighter();

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 45));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverColor);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(baseColor);
                    repaint();
                }
            });
            setBackground(baseColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }
}