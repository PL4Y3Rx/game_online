package main;

import networking.Client;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private final int tileSize = 30;
    private final int rectSpeed = 5; // Speed of rectangle movement
    private int rectX = 100;
    private int rectY = 100;
    private String playerId;

    private Client client;
    private Map<String, Player> players = new HashMap<>();
    private Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(this);

        // Initialize client and connect to server
        try {
            client = new Client(this);
            client.start("127.0.0.1", 5050); // Connect to the server
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize player
        playerId = "player" + System.currentTimeMillis(); // Generate unique ID
        players.put(playerId, new Player(playerId, rectX, rectY));
    }

    @Override
    public void run() {
        while (true) {
            update();
            repaint();
            try {
                Thread.sleep(16); // Approximately 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Update game logic if needed
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Player player : players.values()) {
            // Debug statement to print player ID and position
            g.setColor(Color.red);
            g.fillRect(player.getX(), player.getY(), tileSize, tileSize);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
                rectY -= rectSpeed;
                break;
            case KeyEvent.VK_DOWN:
                rectY += rectSpeed;
                break;
            case KeyEvent.VK_LEFT:
                rectX -= rectSpeed;
                break;
            case KeyEvent.VK_RIGHT:
                rectX += rectSpeed;
                break;
        }
        // Update player position and send to server
        Player player = players.get(playerId);
        if (player != null) {
            player.setX(rectX);
            player.setY(rectY);
            try {
                client.sendPlayerUpdate(playerId, rectX, rectY, "down", 1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Optional: Implement if needed
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Optional: Implement if needed
    }

    public void processMessage(String message) {
        String[] tokens = message.split(" ");
        String command = tokens[0];

        if (command.equals("PLAYER_REGISTER")) {
            if (!players.containsKey(playerId)) {
                int x = Integer.parseInt(tokens[2]);
                int y = Integer.parseInt(tokens[3]);
                String direction = tokens[4];
                int spriteNum = Integer.parseInt(tokens[5]);

                // Register the new player only if they are not already registered
                Player player = new Player(playerId, x, y);
                player.setDirection(direction);
                player.setSpriteNum(spriteNum);
                players.put(playerId, player);
                System.out.println("Registered new player: " + playerId);
            } else {
                System.out.println("Player already registered: " + playerId);
            }
        } else if (command.equals("PLAYER_UPDATE")) {
            if (players.containsKey(playerId)) {
                int x = Integer.parseInt(tokens[2]);
                int y = Integer.parseInt(tokens[3]);
                String direction = tokens[4];
                int spriteNum = Integer.parseInt(tokens[5]);

                // Update the existing player's information
                Player player = players.get(playerId);
                player.setX(x);
                player.setY(y);
                player.setDirection(direction);
                player.setSpriteNum(spriteNum);
            }
        }

        repaint();
    }


    public void startGameThread() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
}

class Player {
    private String id;
    private int x, y;
    private String direction;
    private int spriteNum;

    public Player(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = "down";
        this.spriteNum = 1;
    }

    // Getters and setters
    public String getId() { return id; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public int getSpriteNum() { return spriteNum; }
    public void setSpriteNum(int spriteNum) { this.spriteNum = spriteNum; }
}
