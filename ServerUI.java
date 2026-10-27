import javax.swing.*;
import java.awt.*;

public class ServerUI extends JFrame {

    private JTextArea logArea;
    private JTextField portField;
    private JButton startBtn, stopBtn;
    private Server server;

    public ServerUI() {
        setTitle("🔐 Secure File Transfer - SERVER");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel
        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Port:"));
        portField = new JTextField("5000", 6);
        top.add(portField);
        startBtn = new JButton("▶ Start Server");
        stopBtn = new JButton("⏹ Stop");
        stopBtn.setEnabled(false);
        top.add(startBtn);
        top.add(stopBtn);
        add(top, BorderLayout.NORTH);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Events
        startBtn.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText().trim());
            server = new Server(this);
            server.start(port);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        });

        stopBtn.addActionListener(e -> {
            if (server != null) server.stop();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerUI().setVisible(true));
    }
}