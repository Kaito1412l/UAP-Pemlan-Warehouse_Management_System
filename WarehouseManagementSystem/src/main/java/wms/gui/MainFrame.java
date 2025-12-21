package wms.gui;

import wms.core.model.Location;
import wms.core.model.StockBatch;
import wms.core.repository.ExcelRepository;
import wms.core.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final String ADMIN = "ADMIN";
    private static final String INBOUND = "INBOUND";
    private static final String INVENTORY = "INVENTORY";
    private static final String OUTBOUND = "OUTBOUND";

    private final String role;
    private final String username;

    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color HOVER_COLOR = new Color(100, 149, 237);
    private final Color BG_COLOR = new Color(236, 240, 241);

    private final AdminService adminService = new AdminService();
    private final InboundService inboundService = new InboundService();
    private final InventoryService inventoryService = new InventoryService();
    private final OutboundService outboundService = new OutboundService();

    public MainFrame(String role, String username) {
        this.role = role;
        this.username = username;
        setTitle("WMS - " + username + " (" + role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        if (ADMIN.equals(role)) {
            tabbedPane.addTab("Dashboard", createDashboardPanel());
            tabbedPane.addTab("Lokasi", createLocationPanel());
            tabbedPane.addTab("Stok", createStockPanel());
        } else if (INBOUND.equals(role)) {
            tabbedPane.addTab("Inbound", createInboundPanel());
        } else if (INVENTORY.equals(role)) {
            tabbedPane.addTab("Relokasi", createRelocatePanel());
        } else if (OUTBOUND.equals(role)) {
            tabbedPane.addTab("Outbound", createOutboundPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_COLOR);
        JButton logoutBtn = new ModernButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginApp().setVisible(true);
            dispose();
        });
        footer.add(logoutBtn);
        add(footer, BorderLayout.SOUTH);
    }

    // ========== PANEL DASHBOARD (ADMIN) ==========
    private JPanel createDashboardPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JButton btnViewLocations = new ModernButton("Lihat Lokasi");
        btnViewLocations.addActionListener(e -> showMessageList("Lokasi", adminService.getLocations()
                .stream().map(Location::toBarcode).collect(Collectors.toList())));
        panel.add(btnViewLocations);
        panel.add(Box.createVerticalStrut(10));

        JButton btnViewStock = new ModernButton("Lihat Stok");
        btnViewStock.addActionListener(e -> showStockTable());
        panel.add(btnViewStock);

        return panel;
    }

    // ========== PANEL MANAJEMEN LOKASI (ADMIN) ==========
    private JPanel createLocationPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Manajemen Lokasi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        // Tambah Lokasi
        JTextField addField = new RoundedTextField(20);
        addField.setMaximumSize(new Dimension(300, 40));
        ModernButton addBtn = new ModernButton("Tambah Lokasi");
        addBtn.addActionListener(e -> {
            try {
                Location loc = Location.fromBarcode(addField.getText().trim());
                adminService.addLocation(loc);
                JOptionPane.showMessageDialog(this, "Lokasi berhasil ditambahkan.");
                addField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Tambah Lokasi (format: AREA-LINE-RACK-LEVEL-POS)"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(addField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addBtn);
        panel.add(Box.createVerticalStrut(20));

        // Edit Lokasi
        JTextField oldField = new RoundedTextField(20);
        JTextField newField = new RoundedTextField(20);
        oldField.setMaximumSize(new Dimension(300, 40));
        newField.setMaximumSize(new Dimension(300, 40));
        ModernButton editBtn = new ModernButton("Update Lokasi");
        editBtn.addActionListener(e -> {
            try {
                Location oldLoc = Location.fromBarcode(oldField.getText().trim());
                Location newLoc = Location.fromBarcode(newField.getText().trim());
                adminService.updateLocation(oldLoc, newLoc);
                JOptionPane.showMessageDialog(this, "Lokasi berhasil diperbarui.");
                oldField.setText("");
                newField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Edit Lokasi"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JLabel("Lama:"));
        panel.add(oldField);
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JLabel("Baru:"));
        panel.add(newField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(editBtn);
        panel.add(Box.createVerticalStrut(20));

        // Hapus Lokasi
        JTextField deleteField = new RoundedTextField(20);
        deleteField.setMaximumSize(new Dimension(300, 40));
        ModernButton deleteBtn = new ModernButton("Hapus Lokasi");
        deleteBtn.addActionListener(e -> {
            try {
                Location loc = Location.fromBarcode(deleteField.getText().trim());
                adminService.deleteLocation(loc);
                JOptionPane.showMessageDialog(this, "Lokasi berhasil dihapus.");
                deleteField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Hapus Lokasi"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(deleteField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(deleteBtn);

        return panel;
    }

    // ========== PANEL STOK (ADMIN) ==========
    private JPanel createStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"UPC", "SKU", "BatchDate", "Qty", "Location"}, 0
        );
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);

        // Load data
        List<StockBatch> stock = ExcelRepository.get().getAllStock();
        for (StockBatch b : stock) {
            model.addRow(new Object[]{
                    b.upc, b.sku, b.batchDate.toString(), b.quantity, b.location.toBarcode()
            });
        }

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ========== PANEL INBOUND ==========
    private JPanel createInboundPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Terima Barang (Inbound)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JTextField upcField = new RoundedTextField(20);
        JTextField manuField = new RoundedTextField(20);
        JTextField catField = new RoundedTextField(20);
        JTextField qtyField = new RoundedTextField(20);
        JTextField locField = new RoundedTextField(20);

        ModernButton submitBtn = new ModernButton("Proses Inbound");
        submitBtn.addActionListener(e -> {
            try {
                String upc = upcField.getText().trim();
                String manu = manuField.getText().trim();
                String cat = catField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                Location loc = Location.fromBarcode(locField.getText().trim());

                inboundService.receive(upc, manu, cat, qty, loc);
                JOptionPane.showMessageDialog(this, "Inbound berhasil.");
                upcField.setText("");
                manuField.setText("");
                catField.setText("");
                qtyField.setText("");
                locField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addLabeledField(panel, "UPC", upcField);
        addLabeledField(panel, "Manufacturer", manuField);
        addLabeledField(panel, "Category", catField);
        addLabeledField(panel, "Qty", qtyField);
        addLabeledField(panel, "Location (P-area)", locField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(submitBtn);

        return panel;
    }

    // ========== PANEL RELOKASI ==========
    private JPanel createRelocatePanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Relokasi Stok");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JTextField upcField = new RoundedTextField(20);
        JTextField skuField = new RoundedTextField(20);
        JTextField fromField = new RoundedTextField(20);
        JTextField toField = new RoundedTextField(20);
        JTextField qtyField = new RoundedTextField(20);

        ModernButton submitBtn = new ModernButton("Proses Relokasi");
        submitBtn.addActionListener(e -> {
            try {
                String upc = upcField.getText().trim();
                String sku = skuField.getText().trim();
                Location from = Location.fromBarcode(fromField.getText().trim());
                Location to = Location.fromBarcode(toField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());

                inventoryService.relocate(upc, sku, from, to, qty);
                JOptionPane.showMessageDialog(this, "Relokasi berhasil.");
                upcField.setText("");
                skuField.setText("");
                fromField.setText("");
                toField.setText("");
                qtyField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addLabeledField(panel, "UPC", upcField);
        addLabeledField(panel, "SKU", skuField);
        addLabeledField(panel, "From Location", fromField);
        addLabeledField(panel, "To Location", toField);
        addLabeledField(panel, "Qty", qtyField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(submitBtn);

        return panel;
    }

    // ========== PANEL OUTBOUND ==========
    private JPanel createOutboundPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Ambil Barang (Outbound)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JTextField upcField = new RoundedTextField(20);
        JTextField locField = new RoundedTextField(20);
        JTextField qtyField = new RoundedTextField(20);

        ModernButton submitBtn = new ModernButton("Proses Outbound");
        submitBtn.addActionListener(e -> {
            try {
                String upc = upcField.getText().trim();
                Location loc = Location.fromBarcode(locField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());

                outboundService.pick(upc, loc, qty);
                JOptionPane.showMessageDialog(this, "Outbound berhasil.");
                upcField.setText("");
                locField.setText("");
                qtyField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addLabeledField(panel, "UPC", upcField);
        addLabeledField(panel, "Location (R-area)", locField);
        addLabeledField(panel, "Qty", qtyField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(submitBtn);

        return panel;
    }

    private void addLabeledField(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(Box.createVerticalStrut(5));
        field.setMaximumSize(new Dimension(350, 40));
        panel.add(field);
        panel.add(Box.createVerticalStrut(15));
    }

    private void showMessageList(String title, java.util.List<String> items) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        for (String item : items) {
            area.append(item + "\n");
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStockTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"UPC", "SKU", "BatchDate", "Qty", "Location"}, 0
        );
        JTable table = new JTable(model);
        List<StockBatch> stock = ExcelRepository.get().getAllStock();
        for (StockBatch b : stock) {
            model.addRow(new Object[]{
                    b.upc, b.sku, b.batchDate.toString(), b.quantity, b.location.toBarcode()
            });
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Daftar Stok", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== KOMPONEN CUSTOM ==========
    class RoundedPanel extends JPanel {
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
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }

    static class RoundedTextField extends JTextField {
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
            setBackground(PRIMARY_COLOR);

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