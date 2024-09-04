package main;

import javax.swing.JFrame;

public class main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Game");
        GamePanel gamePanel = new GamePanel();

        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        gamePanel.startGameThread(); // Start the game loop
        gamePanel.requestFocusInWindow(); // Ensure the panel gets focus for key events
    }
}
