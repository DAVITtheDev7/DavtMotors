package dav.project;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import dav.project.models.Car;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class adminHome extends JFrame {

    private JPanel panel;
    private JTabbedPane tabbedPane1;
    private JButton btnAddCar;
    private JButton btnLogout;
    private JTable tbCars;

    private DefaultTableModel tableModel;

    public adminHome() {
        setTitle("Admin Dashboard - DavtMotors");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(panel);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Setup table
        setupTable();

        // Load data
        loadCars();

        // Logout Logic
        btnLogout.addActionListener(e -> {
            new auth().setVisible(true);
            this.dispose();
        });

        // Add Car Logic
        btnAddCar.addActionListener(e -> showAddCarDialog());

        setVisible(true);
    }

    private void showAddCarDialog() {
        JDialog dialog = new JDialog(this, "Add New Car", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField tfBrand = new JTextField();
        JTextField tfModel = new JTextField();
        JTextField tfYear = new JTextField();
        JTextField tfColor = new JTextField();
        JTextField tfMileage = new JTextField();
        JTextField tfPrice = new JTextField();
        JTextField tfImageUrl = new JTextField();

        formPanel.add(new JLabel("Brand:")); formPanel.add(tfBrand);
        formPanel.add(new JLabel("Model:")); formPanel.add(tfModel);
        formPanel.add(new JLabel("Year:"));  formPanel.add(tfYear);
        formPanel.add(new JLabel("Color:")); formPanel.add(tfColor);
        formPanel.add(new JLabel("Mileage:")); formPanel.add(tfMileage);
        formPanel.add(new JLabel("Price ($):")); formPanel.add(tfPrice);
        formPanel.add(new JLabel("Image URL:")); formPanel.add(tfImageUrl);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton btnSave = new JButton("Save Car");
        dialog.add(btnSave, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            try {
                String brand = tfBrand.getText().trim();
                String model = tfModel.getText().trim();
                if (brand.isEmpty() || model.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Brand and Model are required!");
                    return;
                }
                int year = Integer.parseInt(tfYear.getText().trim());
                double price = Double.parseDouble(tfPrice.getText().trim());
                double mileage = Double.parseDouble(tfMileage.getText().trim());
                String imgUrl = tfImageUrl.getText().trim();

                Map<String, Object> carData = new HashMap<>();
                carData.put("brand", brand);
                carData.put("model", model);
                carData.put("year", year);
                carData.put("color", tfColor.getText().trim());
                carData.put("mileage", mileage);
                carData.put("price", price);
                carData.put("imageUrl", imgUrl);

                btnSave.setEnabled(false);

                new Thread(() -> {
                    try {
                        Firestore db = FirestoreClient.getFirestore();
                        db.collection("cars").add(carData).get();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(dialog, "Saved successfully!");
                            dialog.dispose();
                            loadCars();
                        });
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }).start();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });
        dialog.setVisible(true);
    }

    private void setupTable() {
        String[] columnNames = {"Image", "ID", "Brand", "Model", "Year", "Color", "Mileage", "Price", "Action"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Icon.class;
                return Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        tbCars.setModel(tableModel);
        tbCars.setRowHeight(60);

        // Hide ID Column
        tbCars.getColumnModel().getColumn(1).setMinWidth(0);
        tbCars.getColumnModel().getColumn(1).setMaxWidth(0);
        tbCars.getColumnModel().getColumn(1).setWidth(0);

        tbCars.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    private void loadCars() {
        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
                List<QueryDocumentSnapshot> documents = db.collection("cars").get().get().getDocuments();

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (QueryDocumentSnapshot doc : documents) {
                        Car car = doc.toObject(Car.class);
                        car.setId(doc.getId());

                        ImageIcon imageIcon = null;
                        try {
                            if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
                                URL url = new URL(car.getImageUrl());
                                Image image = ImageIO.read(url);
                                Image scaled = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                                imageIcon = new ImageIcon(scaled);
                            }
                        } catch (Exception e) {}

                        tableModel.addRow(new Object[]{
                                imageIcon,
                                car.getId(),
                                car.getBrand(),
                                car.getModel(),
                                car.getYear(),
                                car.getColor(),
                                car.getMileage(),
                                "$" + car.getPrice(),
                                "Delete"
                        });
                    }
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }



    // CUSTOM EDITOR
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "Delete" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // DELETE ACTION
                performDelete(currentRow);
            }
            isPushed = false;
            return label;
        }

        private void performDelete(int row) {
            String docId = (String) tbCars.getValueAt(row, 1);


            new Thread(() -> {
                try {
                    Firestore db = FirestoreClient.getFirestore();
                    db.collection("cars").document(docId).delete();
                    SwingUtilities.invokeLater(() -> {
                        ((DefaultTableModel) tbCars.getModel()).removeRow(row);
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }).start();

        }
    }
}