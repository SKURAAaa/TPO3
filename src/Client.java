import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends JFrame {

    private JComboBox<String> languageDropdown;
    private JTextField wordField;
    private JButton translateButton;
    private JTextArea outputArea;

    private static final String MAIN_SERVER_ADDRESS = "localhost";
    private static final int MAIN_SERVER_PORT = 20000;
    private static final int PORT = 2000;

    public Client() {
        setTitle("Tłumacz boży");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        wordField = new JTextField();
        panel.add(new JLabel("Word:"));
        panel.add(wordField);

        languageDropdown = new JComboBox<>();
        panel.add(new JLabel("Language:"));
        panel.add(languageDropdown);

        translateButton = new JButton("Translate");
        panel.add(translateButton);

        outputArea = new JTextArea();
        panel.add(new JScrollPane(outputArea));

        add(panel);

        // Set visibility
        setVisible(true);

        // Get language servers and initialize GUI
        getLanguageServers();

        // Add action listener for Translate button
        translateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translateWord();
            }
        });
    }

    private void getLanguageServers() {
        try (Socket socket = new Socket(MAIN_SERVER_ADDRESS, MAIN_SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("l");
            String[] languages = in.readLine().split(";");
            languageDropdown.setModel(new DefaultComboBoxModel<>(languages));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void translateWord() {
        String word = wordField.getText();
        String language = (String) languageDropdown.getSelectedItem();

        try (Socket socket = new Socket(MAIN_SERVER_ADDRESS, MAIN_SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             ServerSocket serverSocket = new ServerSocket(PORT)) {

            out.println("t;" + language + ";" + word + ";" + PORT);
            out.flush();

            Socket clientSocket = serverSocket.accept();
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = clientIn.readLine();

            outputArea.append("Translation: " + response + "\n");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}