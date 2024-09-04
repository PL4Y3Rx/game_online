package networking;

import java.io.*;
import java.net.*;
import main.GamePanel;

public class Client {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private GamePanel gamePanel;

    public Client(GamePanel gamePanel) throws IOException {
        this.gamePanel = gamePanel;
    }

    public void start(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        // Start a new thread to listen for server messages
        new Thread(this::listenForMessages).start();
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readUTF()) != null) {
                gamePanel.processMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPlayerUpdate(String id, int x, int y, String direction, int spriteNum) throws IOException {
        out.writeUTF("PLAYER_UPDATE " + id + " " + x + " " + y + " " + direction + " " + spriteNum);
    }
}
