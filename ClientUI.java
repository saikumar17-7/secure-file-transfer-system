import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ClientUI extends JFrame {

    private JTextArea logArea;
    private JTextField hostField, portField;
    private JLabel fileLabel;
    private File selectedFile;
    private Client client;

    public ClientUI() {
        setTitle("🔐 Secure File Transfer - CLIENT");
        setSize(650, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel — connection info
        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel row1 = new JPanel(new FlowLayout());
        row1.add(new JLabel("Server IP:"));
        hostField = new JTextField("127.0.0.1", 10);
        row1.add(hostField);
        row1.add(new JLabel("Port:"));
        portField = new JTextField("5000", 6);
        row1.add(portField);

        JPanel row2 = new JPanel(new FlowLayout());
        JButton chooseBtn = new JButton("📁 Choose File");
        fileLabel = new JLabel("No file selected");
        JButton sendBtn = new JButton("🚀 Send Securely");
        row2.add(chooseBtn);
        row2.add(fileLabel);
        row2.add(sendBtn);

        top.add(row1);
        top.add(row2);
        add(top, BorderLayout.NORTH);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        client = new Client(this);

        chooseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                fileLabel.setText(selectedFile.getName() + " (" + selectedFile.length() + " bytes)");
                log("📁 Selected: " + selectedFile.getAbsolutePath());
            }
        });

        sendBtn.addActionListener(e -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Please choose a file first!");
                return;
            }
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            client.sendFile(host, port, selectedFile);
        });
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}