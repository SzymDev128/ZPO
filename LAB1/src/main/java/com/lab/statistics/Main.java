package com.lab.statistics;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Uruchomienie GUI w wątku zdarzeń Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame window = new MainFrame();
            window.frame.setVisible(true);
        });
    }
}