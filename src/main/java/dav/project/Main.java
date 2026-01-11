package dav.project;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println(" Firebase is successfully connected!");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect database:\n" + e.getMessage(),
                    "Critical Error",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println(e.getMessage());;
            return;
        }

        SwingUtilities.invokeLater(auth::new);

    }
}