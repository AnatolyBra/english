package org.example.desktop;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class DesktopMain {

    private DesktopMain() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // stay on the default look and feel
            }
            TrainerFrame frame = new TrainerFrame();
            frame.setVisible(true);
        });
    }
}
