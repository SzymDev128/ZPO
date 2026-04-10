package com.lab.statistics;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainFrame {
    protected JFrame frame;
    private final int liczbaWyrazowStatystyki = 10;
    private final AtomicBoolean fajrant = new AtomicBoolean(false);
    private final int liczbaProducentow = 1;
    private final int liczbaKonsumentow = 2;
    private ExecutorService executor;
    private List<Future<?>> producentFuture = new CopyOnWriteArrayList<>();

    public MainFrame() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Statystyka Wyrazów");
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(e -> getMultiThreadedStatistics());

        JButton btnStop = new JButton("Stop");
        btnStop.addActionListener(e -> {
            fajrant.set(true);
            producentFuture.forEach(f -> f.cancel(true));
        });

        panel.add(btnStart);
        panel.add(btnStop);
    }

    private void getMultiThreadedStatistics() {
        fajrant.set(false);
        producentFuture.clear();
        executor = Executors.newFixedThreadPool(liczbaProducentow + liczbaKonsumentow);

        // Kolejka blokująca na ścieżki plików opakowane w Optional
        BlockingQueue<Optional<Path>> kolejka = new LinkedBlockingQueue<>(10);

        // PRODUCENT
        Runnable producent = () -> {
            try {
                // Przeszukiwanie drzewa plików
                Files.walkFileTree(Paths.get("files"), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (fajrant.get()) return FileVisitResult.TERMINATE;
                        if (file.toString().endsWith(".txt")) {
                            try {
                                kolejka.put(Optional.of(file));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                // Wysyłanie "Poison Pills" dla konsumentów
                for (int i = 0; i < liczbaKonsumentow; i++) {
                    kolejka.put(Optional.empty());
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // KONSUMENT
        Runnable konsument = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Optional<Path> optPath = kolejka.take();
                    if (!optPath.isPresent()) break; // Wykrycie poison pill

                    // Wywołanie logiki z osobnej klasy
                    Map<String, Long> stats = WordStatsCalculator.getLinkedCountedWords(optPath.get(), liczbaWyrazowStatystyki);
                    System.out.println("Plik: " + optPath.get().getFileName() + " -> " + stats);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        producentFuture.add(executor.submit(producent));
        for (int i = 0; i < liczbaKonsumentow; i++) {
            executor.execute(konsument);
        }
    }
}