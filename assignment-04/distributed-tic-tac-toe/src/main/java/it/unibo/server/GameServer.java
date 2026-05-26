package it.unibo.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameServer {
    static void main() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            GameManager manager = new GameManagerImpl();
            registry.rebind("GameManager", manager);
            System.out.println("Tic-Tac-Toe Server is running on port 1099...");
        } catch (RemoteException e) {
            System.err.println("Server failed to start due to RMI error:" + e.getMessage());
        }
    }
}
