package wms.gui;

import wms.core.model.Location;
import wms.core.model.StockBatch;
import wms.core.repository.ExcelRepository;
import wms.core.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

public class MainFrame extends JFrame {

    private static final String ADMIN = "ADMIN";
    private static final String INBOUND = "INBOUND";
    private static final String INVENTORY = "INVENTORY";
    private static final String OUTBOUND = "OUTBOUND";

    private final String role;
    private final String username;

    private final AdminService adminService = new AdminService();
    private final InboundService inboundService = new InboundService();
    private final InventoryService inventoryService = new InventoryService();
    private final OutboundService outboundService = new OutboundService();

    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color HOVER_COLOR = new Color(100, 149, 237);
    private final Color BG_COLOR = new Color(236, 240, 241);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public MainFrame(String role, String username) {
        this.role = role;
        this.username = username;

        setTitle("WMS - " + username + " (" + role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        if (ADMIN.equals(role)) {
            tabbedPane.addTab("Dashboard", createDashboardPanel());
            tabbedPane.addTab("Manajemen Lokasi", createLocationManagementPanel());
        } else if (INBOUND.equals(role)) {
            tabbedPane.addTab("Inbound Task", createInboundPanel());
        } else if (INVENTORY.equals(role)) {
            tabbedPane.addTab("Relokasi Task", createRelocatePanel());
        } else if (OUTBOUND.equals(role)) {
            tabbedPane.addTab("Outbound Task", createOutboundPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_COLOR);
        JButton logoutBtn = new ModernButton("Logout", new Color(231, 76, 60));
        logoutBtn.setFont(new Font("Segoe UI",Font.PLAIN, 14));
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(e -> {
            new LoginApp().setVisible(true);
            dispose();
        });
        footer.add(logoutBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private boolean validateInput(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Kolom '" + fieldName + "' tidak boleh kosong!",
                    "Peringatan Validasi",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Klik tombol header tabel untuk menyortir data");
        subTitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subTitle.setForeground(Color.GRAY);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(title);
        headerPanel.add(subTitle);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setOpaque(false);

        JPanel locPanel = new JPanel(new BorderLayout(0, 10));
        locPanel.setOpaque(false);
        JLabel locTitle = new JLabel("Daftar Lokasi");
        locTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        locPanel.add(locTitle, BorderLayout.NORTH);

        DefaultTableModel locModel = new DefaultTableModel(new String[]{"Kode Lokasi"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return String.class; }
        };
        JTable locTable = new JTable(locModel);
        locTable.setAutoCreateRowSorter(true);
        styleTable(locTable, PRIMARY_COLOR);

        List<Location> locations = adminService.getLocations();
        for (Location l : locations) {
            locModel.addRow(new String[]{l.toBarcode()});
        }
        locPanel.add(new JScrollPane(locTable), BorderLayout.CENTER);

        JPanel stockPanel = new JPanel(new BorderLayout(0, 10));
        stockPanel.setOpaque(false);
        JLabel stockTitle = new JLabel("Data Stok");
        stockTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        stockPanel.add(stockTitle, BorderLayout.NORTH);

        DefaultTableModel stockModel = new DefaultTableModel(new String[]{"UPC", "SKU", "Qty", "Lokasi"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if(columnIndex == 2) return Integer.class;
                return String.class;
            }
        };
        JTable stockTable = new JTable(stockModel);
        stockTable.setAutoCreateRowSorter(true);
        styleTable(stockTable, new Color(39, 174, 96));

        List<StockBatch> stocks = ExcelRepository.get().getAllStock();
        for (StockBatch b : stocks) {
            stockModel.addRow(new Object[]{
                    b.upc, b.sku, b.quantity, b.location.toBarcode()
            });
        }
        stockPanel.add(new JScrollPane(stockTable), BorderLayout.CENTER);

        tablesPanel.add(locPanel);
        tablesPanel.add(stockPanel);
        panel.add(tablesPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        ModernButton refreshBtn = new ModernButton("Refresh Data", Color.GRAY);
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.addActionListener(e -> {
            locModel.setRowCount(0);
            adminService.getLocations().forEach(l -> locModel.addRow(new String[]{l.toBarcode()}));
            stockModel.setRowCount(0);
            ExcelRepository.get().getAllStock().forEach(b -> stockModel.addRow(new Object[]{
                    b.upc, b.sku, b.quantity, b.location.toBarcode()
            }));
        });
        footerPanel.add(refreshBtn);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLocationManagementPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel addLbl = new JLabel("Tambah Lokasi Baru");
        addLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(addLbl, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        JTextField addField = new RoundedTextField(15);
        panel.add(addField, gbc);

        gbc.gridx = 1;
        ModernButton addBtn = new ModernButton("Simpan", new Color(39, 174, 96));
        addBtn.setPreferredSize(new Dimension(100, 40));
        addBtn.addActionListener(e -> {

            if (!validateInput(addField.getText(), "Lokasi Baru")) return;

            try {
                adminService.addLocation(Location.fromBarcode(addField.getText().trim()));
                JOptionPane.showMessageDialog(this, "Lokasi berhasil ditambahkan.");
                addField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(addBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);

        gbc.gridy = 3;
        JLabel editLbl = new JLabel("Edit Lokasi");
        editLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(editLbl, gbc);

        gbc.gridy = 4; gbc.gridwidth = 1;
        JTextField oldField = new RoundedTextField(15);
        oldField.setToolTipText("Kode Lama");
        panel.add(oldField, gbc);

        gbc.gridx = 1;
        JTextField newField = new RoundedTextField(15);
        newField.setToolTipText("Kode Baru");
        panel.add(newField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        ModernButton editBtn = new ModernButton("Update", new Color(243, 156, 18));
        editBtn.addActionListener(e -> {

            if (!validateInput(oldField.getText(), "Lokasi Lama")) return;
            if (!validateInput(newField.getText(), "Lokasi Baru")) return;

            try {
                adminService.updateLocation(
                        Location.fromBarcode(oldField.getText().trim()),
                        Location.fromBarcode(newField.getText().trim())
                );
                JOptionPane.showMessageDialog(this, "Lokasi diperbarui.");
                oldField.setText(""); newField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(editBtn, gbc);

        gbc.gridy = 6;
        panel.add(new JSeparator(), gbc);

        gbc.gridy = 7;
        JLabel delLbl = new JLabel("Hapus Lokasi");
        delLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(delLbl, gbc);

        gbc.gridy = 8; gbc.gridwidth = 1;
        JTextField delField = new RoundedTextField(15);
        panel.add(delField, gbc);

        gbc.gridx = 1;
        ModernButton delBtn = new ModernButton("Hapus", new Color(192, 57, 43));
        delBtn.setPreferredSize(new Dimension(100, 40));
        delBtn.addActionListener(e -> {

            if (!validateInput(delField.getText(), "Lokasi yang akan dihapus")) return;

            try {
                adminService.deleteLocation(Location.fromBarcode(delField.getText().trim()));
                JOptionPane.showMessageDialog(this, "Lokasi dihapus.");
                delField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(delBtn, gbc);

        return panel;
    }

    private JPanel createInboundPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField upc = new RoundedTextField(20);
        JTextField manu = new RoundedTextField(20);
        JTextField cat = new RoundedTextField(20);
        JTextField qty = new RoundedTextField(20);
        JTextField loc = new RoundedTextField(20);

        addLabelAndField(panel, "UPC Code:", upc);
        addLabelAndField(panel, "Manufacturer:", manu);
        addLabelAndField(panel, "Category:", cat);
        addLabelAndField(panel, "Quantity:", qty);
        addLabelAndField(panel, "Location (P-Area):", loc);

        panel.add(Box.createVerticalStrut(20));
        ModernButton btn = new ModernButton("Terima Barang (Inbound)", PRIMARY_COLOR);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {

            if (!validateInput(upc.getText(), "UPC Code")) return;
            if (!validateInput(manu.getText(), "Manufacturer")) return;
            if (!validateInput(cat.getText(), "Category")) return;
            if (!validateInput(qty.getText(), "Quantity")) return;
            if (!validateInput(loc.getText(), "Location")) return;

            try {
                inboundService.receive(upc.getText(), manu.getText(), cat.getText(),
                        Integer.parseInt(qty.getText()), Location.fromBarcode(loc.getText()));
                JOptionPane.showMessageDialog(this, "Inbound Berhasil.");
                upc.setText(""); manu.setText(""); cat.setText(""); qty.setText(""); loc.setText("");
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Quantity harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btn);
        return panel;
    }

    private JPanel createRelocatePanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField upc = new RoundedTextField(20);
        JTextField sku = new RoundedTextField(20);
        JTextField from = new RoundedTextField(20);
        JTextField to = new RoundedTextField(20);
        JTextField qty = new RoundedTextField(20);

        addLabelAndField(panel, "UPC Code:", upc);
        addLabelAndField(panel, "SKU:", sku);
        addLabelAndField(panel, "From Location:", from);
        addLabelAndField(panel, "To Location:", to);
        addLabelAndField(panel, "Quantity:", qty);

        panel.add(Box.createVerticalStrut(20));
        ModernButton btn = new ModernButton("Pindahkan Barang (Relocate)", PRIMARY_COLOR);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {

            if (!validateInput(upc.getText(), "UPC Code")) return;
            if (!validateInput(sku.getText(), "SKU")) return;
            if (!validateInput(from.getText(), "From Location")) return;
            if (!validateInput(to.getText(), "To Location")) return;
            if (!validateInput(qty.getText(), "Quantity")) return;

            try {
                inventoryService.relocate(upc.getText(), sku.getText(),
                        Location.fromBarcode(from.getText()),
                        Location.fromBarcode(to.getText()),
                        Integer.parseInt(qty.getText()));
                JOptionPane.showMessageDialog(this, "Relokasi Berhasil.");
                upc.setText(""); sku.setText(""); from.setText(""); to.setText(""); qty.setText("");
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Quantity harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btn);
        return panel;
    }

    private JPanel createOutboundPanel() {
        JPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField upc = new RoundedTextField(20);
        JTextField loc = new RoundedTextField(20);
        JTextField qty = new RoundedTextField(20);

        addLabelAndField(panel, "UPC Code:", upc);
        addLabelAndField(panel, "Location (R-Area):", loc);
        addLabelAndField(panel, "Quantity:", qty);

        panel.add(Box.createVerticalStrut(20));
        ModernButton btn = new ModernButton("Keluarkan Barang (Outbound)", PRIMARY_COLOR);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            if (!validateInput(upc.getText(), "UPC Code")) return;
            if (!validateInput(loc.getText(), "Location")) return;
            if (!validateInput(qty.getText(), "Quantity")) return;

            try {
                outboundService.pick(upc.getText(), Location.fromBarcode(loc.getText()),
                        Integer.parseInt(qty.getText()));
                JOptionPane.showMessageDialog(this, "Outbound Berhasil.");
                upc.setText(""); loc.setText(""); qty.setText("");
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Quantity harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btn);
        return panel;
    }

    private void addLabelAndField(JPanel p, String text, JTextField field) {
        JLabel l = new JLabel(text);
        l.setFont(MAIN_FONT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));

        field.setMaximumSize(new Dimension(500, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(15));
    }

    private void styleTable(JTable table, Color headerColor) {
        table.setFont(MAIN_FONT);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        header.setDefaultRenderer((t, value, isSelected, hasFocus, row, column) -> {
            String text = value.toString();

            String icon = " ▲";

            if (t.getRowSorter() != null) {
                java.util.List<? extends RowSorter.SortKey> keys = t.getRowSorter().getSortKeys();
                if (keys != null && !keys.isEmpty()) {
                    for (RowSorter.SortKey key : keys) {
                        if (key.getColumn() == column) {
                            if (key.getSortOrder() == SortOrder.ASCENDING) {
                                icon = " ▲";
                            } else if (key.getSortOrder() == SortOrder.DESCENDING) {
                                icon = " ▼";
                            }
                            break;
                        }
                    }
                }
            }

            JButton btn = new JButton(text + icon);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBackground(headerColor);
            btn.setForeground(Color.WHITE);

            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            return btn;
        });
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bgColor;
        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
        }
    }

    static class RoundedTextField extends JTextField {
        public RoundedTextField(int cols) {
            super(cols);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 245, 245));
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            super.paintComponent(g);
        }
        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(189, 195, 199));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
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
            setPreferredSize(new Dimension(200, 45));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { setBackground(hoverColor); repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { setBackground(baseColor); repaint(); }
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

            FontMetrics fm = g2.getFontMetrics();
            Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
            int x = (getWidth() - stringBounds.width) / 2;
            int y = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, y);
        }
    }
}