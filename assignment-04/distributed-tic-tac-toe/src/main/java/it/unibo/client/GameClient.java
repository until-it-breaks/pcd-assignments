package it.unibo.client;

import it.unibo.server.GameManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
    static void main() {
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            GameManager manager = (GameManager) registry.lookup("GameManager");
            ClientGUI gui = new ClientGUI(manager);
            gui.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
