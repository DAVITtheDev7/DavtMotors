package dav.project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class auth extends JFrame {
    private JPanel panel;

    private JTextField tfLogin;
    private JTextField tfUsername;
    private JTextField tfRegEmail;

    private JPasswordField pfPassword;
    private JPasswordField pfRegPass;

    private JButton btnAuth;
    private JButton btnRegister;

    private JTabbedPane tpRegister;

    private JLabel lblError;

    public auth() {
        setTitle("ავტორიზაცია");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(panel);
        setSize(600, 500);

        // LOGIN BUTTON
        btnAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performAuth();
            }
        });

        // REGISTER BUTTON
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });

        if (lblError != null) {
            lblError.setText("");
        }

        setVisible(true);
    }

    // REGISTRATION LOGIC
    private void performRegister() {
        // Get Data
        String username = tfUsername.getText().trim();
        String email = tfRegEmail.getText().trim();
        String password = new String(pfRegPass.getPassword()).trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("ყველა ველი სავალდებულოა!");
            return;
        }

        if (isValidEmail(email)) {
            showError("ელ-ფოსტის ფორმატი არასწორია!");
            return;
        }

        btnRegister.setEnabled(false);

        // Hash Password
        String hashedPassword = hashPassword(password);

        // Create User Object for Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", hashedPassword);
        user.put("createdAt", System.currentTimeMillis());

        // Save to Firestore
        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();

                // Add to "users" collection.
                ApiFuture<DocumentReference> addedDocRef = db.collection("users").add(user);

                System.out.println("User added with ID: " + addedDocRef.get().getId());

                // Update UI on success
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "რეგისტრაცია წარმატებით დასრულდა!");
                    // Clear fields
                    tfUsername.setText("");
                    tfRegEmail.setText("");
                    pfRegPass.setText("");
                    btnRegister.setEnabled(true);
                });

            } catch (InterruptedException | ExecutionException ex) {
                System.out.println(ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    showError("შეცდომა ბაზასთან დაკავშირებისას: " + ex.getMessage());
                    btnRegister.setEnabled(true);
                });
            }
        }).start();
    }

    private void performAuth() {
        String email = tfLogin.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (lblError != null) lblError.setText("");

        // VALIDATION
        if (email.isEmpty()) {
            showError("ელ-ფოსტა აუცილებელია!");
            return;
        }
        if (password.isEmpty()) {
            showError("პაროლი აუცილებელია!");
            return;
        }
        if (isValidEmail(email)) {
            showError("ელ-ფოსტის ფორმატი არასწორია!");
            return;
        }

        // ADMIN LOGIN CHECK
        if (email.equals("admin@admin.com") && password.equals("admin")) {
            new adminHome().setVisible(true);
            this.dispose();
            return;
        }

        // USER LOGIN LOGIC
        btnAuth.setEnabled(false);

        new Thread(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();

                // Query the database for the email
                List<QueryDocumentSnapshot> documents = db.collection("users")
                        .whereEqualTo("email", email)
                        .get().get().getDocuments();

                SwingUtilities.invokeLater(() -> {
                    if (documents.isEmpty()) {
                        showError("მომხმარებელი არ მოიძებნა!");
                        btnAuth.setEnabled(true);
                        return;
                    }

                    // Get the user data
                    DocumentSnapshot userDoc = documents.get(0);
                    String storedPassword = userDoc.getString("password");

                    // Hash the input password to compare
                    String inputHashedPass = hashPassword(password);

                    // Compare passwords
                    if (storedPassword != null && storedPassword.equals(inputHashedPass)) {
                        new home(email).setVisible(true);
                        this.dispose();
                    } else {
                        showError("პაროლი არასწორია!");
                        btnAuth.setEnabled(true);
                    }
                });

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    showError("შეცდომა: " + ex.getMessage());
                    btnAuth.setEnabled(true);
                });
            }
        }).start();
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return !email.matches(emailRegex);
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
        } else {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // PASSWORD HASHING
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}