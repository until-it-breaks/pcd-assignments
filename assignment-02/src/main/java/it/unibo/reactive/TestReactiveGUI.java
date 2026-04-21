package it.unibo.reactive;

import javax.swing.*;

public class TestReactiveGUI {
    static void main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(ReactiveGUI::new);
    }
}
