import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

public class Server {

    private ServerUI ui;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public Server(ServerUI ui) {
        this.ui = ui;
    }

    public void start(int port) {
        running = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                ui.log("✅ Server started on port " + port);
                ui.log("⏳ Waiting for client...");

                while (running) {
                    Socket client = serverSocket.accept();
                    ui.log("🔗 Client connected: " + client.getInetAddress());
                    handleClient(client);
                }
            } catch (Exception e) {
                if (running) ui.log("❌ Server error: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        new Thread(() -> {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // 1. Send RSA public key
                KeyPair rsaKeys = CryptoUtils.generateRSAKeyPair();
                byte[] pubKeyBytes = CryptoUtils.publicKeyToBytes(rsaKeys.getPublic());
                dos.writeInt(pubKeyBytes.length);
                dos.write(pubKeyBytes);
                dos.flush();
                ui.log("🔑 Sent RSA public key to client");

                // 2. Receive RSA-encrypted AES key
                int encKeyLen = dis.readInt();
                byte[] encAesKey = new byte[encKeyLen];
                dis.readFully(encAesKey);
                byte[] aesKeyBytes = CryptoUtils.rsaDecrypt(encAesKey, rsaKeys.getPrivate());
                SecretKey aesKey = CryptoUtils.bytesToAESKey(aesKeyBytes);
                ui.log("🔐 Received & decrypted AES key");

                // 3. Receive filename
                String filename = dis.readUTF();
                ui.log("📄 Receiving file: " + filename);

                // 4. Receive encrypted data
                int dataLen = dis.readInt();
                byte[] encData = new byte[dataLen];
                dis.readFully(encData);
                ui.log("📦 Received " + dataLen + " encrypted bytes");

                // 5. Decrypt
                byte[] decrypted = CryptoUtils.aesDecrypt(encData, aesKey);

                // 6. Verify integrity
                String receivedHash = dis.readUTF();
                String computedHash = CryptoUtils.sha256(decrypted);

                boolean valid = receivedHash.equals(computedHash);
                ui.log(valid ? "✅ Integrity VERIFIED" : "⚠️ Hash mismatch!");

                // 7. Save file
                File outFile = new File("received_" + filename);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(decrypted);
                }
                ui.log("💾 Saved: " + outFile.getAbsolutePath());

                // 8. Send ACK
                dos.writeUTF(valid ? "SUCCESS" : "HASH_MISMATCH");
                dos.flush();

                socket.close();
            } catch (Exception e) {
                ui.log("❌ Error handling client: " + e.getMessage());
            }
        }).start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            ui.log("🛑 Server stopped");
        } catch (IOException e) {
            ui.log("Error stopping server: " + e.getMessage());
        }
    }
}