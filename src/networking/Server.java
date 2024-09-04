package networking;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 5050;
    private ServerSocket serverSocket;
    private Map<Socket, DataOutputStream> clientOutputs = new ConcurrentHashMap<>();
    private Map<String, PlayerData> players = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            clientOutputs.put(clientSocket, out);

            // Read player ID from the client
            String playerId = in.readUTF();
            if (players.containsKey(playerId)) {
                out.writeUTF("ERROR ID_ALREADY_EXISTS");
                return;
            }

            // Register the new player with the provided ID
            PlayerData newPlayerData = new PlayerData(100, 100, "down", 1);
            newPlayerData.setSocket(clientSocket); // Set the socket
            players.put(playerId, newPlayerData);

            // Log player registration
            System.out.println("Player registered: " + playerId);

            // Send initial player info to the new client
            out.writeUTF("PLAYER_REGISTER " + playerId + " 100 100 down 1");

            // Send the current state of all players to the new client
            for (PlayerData playerData : players.values()) {
                out.writeUTF("PLAYER_REGISTER "+
                             playerData.getX() + " " + playerData.getY() + " " +
                             playerData.getDirection() + " " + playerData.getSpriteNum());
            }

            // Broadcast new player info to all clients
            broadcast("PLAYER_REGISTER " + playerId + " 100 100 down 1");

            String message;
            while ((message = in.readUTF()) != null) {
                if (message.startsWith("PLAYER_UPDATE")) {
                    String[] tokens = message.split(" ");
                    String id = tokens[1];
                    int x = Integer.parseInt(tokens[2]);
                    int y = Integer.parseInt(tokens[3]);
                    String direction = tokens[4];
                    int spriteNum = Integer.parseInt(tokens[5]);

                    PlayerData updatedData = new PlayerData(x, y, direction, spriteNum);
                    updatedData.setSocket(clientSocket); // Update socket reference
                    players.put(id, updatedData);
                    broadcast(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                String playerId = getPlayerIdBySocket(clientSocket);
                if (playerId != null) {
                    players.remove(playerId);
                    System.out.println("Player disconnected: " + playerId);
                    broadcast("PLAYER_DISCONNECT " + playerId);
                }
                clientSocket.close();
                clientOutputs.remove(clientSocket);
                System.out.println("Client disconnected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPlayerIdBySocket(Socket socket) {
        for (Map.Entry<String, PlayerData> entry : players.entrySet()) {
            if (entry.getValue().getSocket() == socket) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void broadcast(String message) {
        for (DataOutputStream out : clientOutputs.values()) {
            try {
                out.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class PlayerData {
    private int x, y;
    private String direction;
    private int spriteNum;
    private Socket socket; // Keep track of the socket

    public PlayerData(int x, int y, String direction, int spriteNum) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.spriteNum = spriteNum;
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public String getDirection() { return direction; }
    public int getSpriteNum() { return spriteNum; }
    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }
}
