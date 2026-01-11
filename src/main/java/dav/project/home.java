package dav.project;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import dav.project.models.Car;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class home extends JFrame {

    private JPanel panel;
    private JTabbedPane tpHome;
    private JLabel lblGreeting;
    private JButton btnLogout;
    private JTable tbCars;
    private JTable tbMyCars;

    private DefaultTableModel tableModel;
    private DefaultTableModel myCarsTableModel;
    private final String currentUserEmail;

    public home(String userEmail) {
        this.currentUserEmail = userEmail;

        setTitle("DavtMotors");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        lblGreeting.setText("Loading profile...");
        setupTable();
        setupMyCarsTable();
        setupTableClick();
        setupMyCarsTableClick();

        loadUserProfile();
        loadCars();
        loadMyPurchases();

        btnLogout.addActionListener(e -> {
            new auth().setVisible(true);
            dispose();
        });

        setVisible(true);
    }

    private void loadUserProfile() {
        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
                List<QueryDocumentSnapshot> docs = db.collection("users")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .get()
                        .getDocuments();

                SwingUtilities.invokeLater(() -> {
                    if (!docs.isEmpty()) {
                        lblGreeting.setText("გამარჯობა, " + docs.get(0).getString("username") + "!");
                    } else {
                        lblGreeting.setText("გამარჯობა, " + currentUserEmail + "!");
                    }
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());;
            }
        }).start();
    }

    private void setupTable() {
        String[] columns = {"Image", "ID", "Brand", "Model", "Year", "Price", "Action"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Icon.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbCars.setModel(tableModel);
        tbCars.setRowHeight(60);

        tbCars.getColumnModel().getColumn(1).setMinWidth(0);
        tbCars.getColumnModel().getColumn(1).setMaxWidth(0);
    }

    private void setupMyCarsTable() {
        String[] columns = {"Image", "Purchase ID", "Brand", "Model", "Year", "Purchase Date", "Price", "Action"};

        myCarsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Icon.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbMyCars.setModel(myCarsTableModel);
        tbMyCars.setRowHeight(60);

        tbMyCars.getColumnModel().getColumn(1).setMinWidth(0);
        tbMyCars.getColumnModel().getColumn(1).setMaxWidth(0);
    }

    private void setupTableClick() {
        tbCars.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tbCars.rowAtPoint(e.getPoint());
                int col = tbCars.columnAtPoint(e.getPoint());

                if (col == 6 && row >= 0) {
                    handlePurchase(row);
                }
            }
        });
    }

    private void setupMyCarsTableClick() {
        tbMyCars.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tbMyCars.rowAtPoint(e.getPoint());
                int col = tbMyCars.columnAtPoint(e.getPoint());

                if (col == 7 && row >= 0) {
                    handleViewReceipt(row);
                }
            }
        });
    }

    private void loadCars() {
        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
                List<QueryDocumentSnapshot> docs = db.collection("cars").get().get().getDocuments();

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);

                    for (QueryDocumentSnapshot doc : docs) {
                        Car car = doc.toObject(Car.class);

                        ImageIcon icon = null;
                        try {
                            if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
                                Image img = ImageIO.read(new URL(car.getImageUrl()));
                                icon = new ImageIcon(img.getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        tableModel.addRow(new Object[]{
                                icon,
                                doc.getId(),
                                car.getBrand(),
                                car.getModel(),
                                String.valueOf(car.getYear()),
                                "$" + car.getPrice(),
                                "Buy Now"
                        });
                    }
                });

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }).start();
    }

    private void loadMyPurchases() {
        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
                List<QueryDocumentSnapshot> docs = db.collection("purchases")
                        .whereEqualTo("buyerEmail", currentUserEmail)
                        .get()
                        .get()
                        .getDocuments();

                SwingUtilities.invokeLater(() -> {
                    myCarsTableModel.setRowCount(0);

                    for (QueryDocumentSnapshot doc : docs) {
                        String brand = doc.getString("brand");
                        String model = doc.getString("model");
                        String price = doc.getString("price");
                        String purchaseDate = doc.getString("date");
                        String year = doc.getString("year");
                        String imageUrl = doc.getString("imageUrl");

                        ImageIcon icon = null;
                        try {
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Image img = ImageIO.read(new URL(imageUrl));
                                icon = new ImageIcon(img.getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                            }
                        } catch (Exception ignored) {
                        }

                        myCarsTableModel.addRow(new Object[]{
                                icon,
                                doc.getId(),
                                brand,
                                model,
                                year,
                                purchaseDate,
                                price,
                                "See Receipt"
                        });
                    }
                });

            } catch (Exception e) {
                System.out.println(e.toString());;
            }
        }).start();
    }

    private void handlePurchase(int row) {
        String carId = (String) tbCars.getValueAt(row, 1);
        String brand = (String) tbCars.getValueAt(row, 2);
        String model = (String) tbCars.getValueAt(row, 3);
        String year = (String) tbCars.getValueAt(row, 4);
        String price = (String) tbCars.getValueAt(row, 5);

        String receipt = buildReceipt(brand, model, price);

        Object[] options = {"OK", "Download Receipt"};
        int choice = JOptionPane.showOptionDialog(
                this,
                receipt,
                "Purchase Successful",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) {
            saveReceiptToFile(receipt, brand, model);
        }

        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();

                // Fetch car to get imageUrl
                Car car = db.collection("cars").document(carId).get().get().toObject(Car.class);
                String imageUrl = (car != null) ? car.getImageUrl() : null;

                Map<String, Object> purchaseData = new HashMap<>();
                purchaseData.put("buyerEmail", currentUserEmail);
                purchaseData.put("carId", carId);
                purchaseData.put("brand", brand);
                purchaseData.put("model", model);
                purchaseData.put("year", year);
                purchaseData.put("price", price);
                purchaseData.put("date", LocalDate.now().toString());
                purchaseData.put("receipt", receipt);
                purchaseData.put("imageUrl", imageUrl);

                db.collection("purchases").add(purchaseData).get();

                loadCars();
                loadMyPurchases();

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Failed to save purchase.", "Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private void handleViewReceipt(int row) {
        String purchaseId = (String) tbMyCars.getValueAt(row, 1);
        String brand = (String) tbMyCars.getValueAt(row, 2);
        String model = (String) tbMyCars.getValueAt(row, 3);

        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
                String receipt = db.collection("purchases")
                        .document(purchaseId)
                        .get()
                        .get()
                        .getString("receipt");

                SwingUtilities.invokeLater(() -> {
                    if (receipt != null) {
                        Object[] options = {"OK", "Download"};
                        int choice = JOptionPane.showOptionDialog(
                                this,
                                receipt,
                                "Purchase Receipt",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                options,
                                options[0]
                        );

                        if (choice == 1) {
                            saveReceiptToFile(receipt, brand, model);
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Receipt not found.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Failed to load receipt.", "Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private String buildReceipt(String brand, String model, String price) {
        return "-------- SALE RECEIPT --------\n" +
                "Customer: " + currentUserEmail + "\n" +
                "Vehicle: " + brand + " " + model + "\n" +
                "Price: " + price + "\n" +
                "Date: " + LocalDate.now() + "\n" +
                "------------------------------\n\n" +
                "Thank you for your purchase!";
    }

    private void saveReceiptToFile(String receipt, String brand, String model) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Receipt");
        chooser.setSelectedFile(new File(brand + " " + model + ".txt"));

        int option = chooser.showSaveDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(receipt);
                JOptionPane.showMessageDialog(
                        this,
                        "Receipt saved successfully!",
                        "Saved",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to save receipt",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


}