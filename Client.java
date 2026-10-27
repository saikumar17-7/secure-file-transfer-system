import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.PublicKey;

public class Client {

    private ClientUI ui;

    public Client(ClientUI ui) {
        this.ui = ui;
    }

    public void sendFile(String host, int port, File file) {
        new Thread(() -> {
            try {
                ui.log("🔗 Connecting to " + host + ":" + port + "...");
                Socket socket = new Socket(host, port);
                ui.log("✅ Connected");

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // 1. Receive RSA public key
                int pubLen = dis.readInt();
                byte[] pubKeyBytes = new byte[pubLen];
                dis.readFully(pubKeyBytes);
                PublicKey serverPub = CryptoUtils.bytesToPublicKey(pubKeyBytes);
                ui.log("🔑 Received server's RSA public key");

                // 2. Generate AES key & encrypt with RSA
                SecretKey aesKey = CryptoUtils.generateAESKey();
                byte[] encAesKey = CryptoUtils.rsaEncrypt(aesKey.getEncoded(), serverPub);
                dos.writeInt(encAesKey.length);
                dos.write(encAesKey);
                dos.flush();
                ui.log("🔐 AES key encrypted & sent");

                // 3. Read file and encrypt
                byte[] fileData = Files.readAllBytes(file.toPath());
                ui.log("📄 File size: " + fileData.length + " bytes");

                byte[] encData = CryptoUtils.aesEncrypt(fileData, aesKey);
                String hash = CryptoUtils.sha256(fileData);

                // 4. Send filename, data, hash
                dos.writeUTF(file.getName());
                dos.writeInt(encData.length);
                dos.write(encData);
                dos.writeUTF(hash);
                dos.flush();
                ui.log("📦 Encrypted file sent (" + encData.length + " bytes)");

                // 5. Wait for ACK
                String ack = dis.readUTF();
                ui.log("📨 Server response: " + ack);
                ui.log(ack.equals("SUCCESS") ? "✅ Transfer complete!" : "❌ Hash mismatch");

                socket.close();
            } catch (Exception e) {
                ui.log("❌ Error: " + e.getMessage());
            }
        }).start();
    }
}