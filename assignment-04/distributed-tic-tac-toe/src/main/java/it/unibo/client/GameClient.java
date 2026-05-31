package it.unibo.client;

import it.unibo.server.GameManager;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
    static void main() {
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            GameManager manager = (GameManager) registry.lookup("GameManager");
            SwingUtilities.invokeLater(() -> {
                ClientGUI gui = new ClientGUI(manager);
                gui.start();
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
