package it.unibo.reactive;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.unibo.api.ImmutableBucket;
import it.unibo.api.ImmutableFSReport;
import it.unibo.api.ScanParameters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

public class ReactiveGUI {
    final JFrame frame = new JFrame("RxFSStat");
    private String targetPath = "";
    private static final int DEFAULT_MAX_FILE_SIZE_BYTES = 1000;
    private static final int DEFAULT_BAND_COUNT = 5;
    private final RxFSStatService service = new RxFSStatService();
    private Disposable subscription = Disposable.disposed();

    public ReactiveGUI() {
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel topPanel = new JPanel(new BorderLayout());
        JTextField pathField = new JTextField("Target directory");
        pathField.setEditable(false);
        JButton browseButton = new JButton("Browse");
        topPanel.add(pathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);

        JTextArea resultsArea = new JTextArea(20, 40);
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JSpinner sizeSpinner = new JSpinner();
        sizeSpinner.setValue(DEFAULT_MAX_FILE_SIZE_BYTES);
        JSpinner bandCountSpinner = new JSpinner();
        bandCountSpinner.setValue(DEFAULT_BAND_COUNT);
        JProgressBar progressBar = new JProgressBar();
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        JPanel spinnerGroup = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spinnerGroup.add(new JLabel("Max Size (bytes)"));
        spinnerGroup.add(sizeSpinner);
        spinnerGroup.add(new JLabel("Band count"));
        spinnerGroup.add(bandCountSpinner);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonGroup.add(startButton);
        buttonGroup.add(stopButton);

        bottomPanel.add(spinnerGroup, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(buttonGroup, BorderLayout.EAST);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        browseButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
                targetPath = chooser.getSelectedFile().getAbsolutePath();
            }
        });

        startButton.addActionListener(_ -> {
            if (targetPath.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select a directory first.");
                return;
            }

            int currentMax = (int) sizeSpinner.getValue();
            int currentBandCount = (int) bandCountSpinner.getValue();

            subscription.dispose();
            subscription = service.getFSReportUpdates(new ScanParameters(Path.of(targetPath), currentMax, currentBandCount))
                .observeOn(Schedulers.from(SwingUtilities::invokeLater))
                .doOnSubscribe(_ -> {
                    resultsArea.setText("Scanning...");
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    progressBar.setIndeterminate(true);
                })
                .doFinally(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    progressBar.setIndeterminate(false);
                })
                .subscribe(
                    report -> resultsArea.setText(formatReport(report)),
                    error -> resultsArea.setText(error.toString())
                );
        });

        stopButton.addActionListener(_ -> {
            subscription.dispose();
            resultsArea.append("\nScan Cancelled");
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                subscription.dispose();
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String formatReport(ImmutableFSReport report) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Total Files: ").append(report.getTotalFiles()).append("\n\n");
        stringBuilder.append("Size Distribution:\n");
        for (ImmutableBucket bucket : report.buckets()) {
            stringBuilder.append(String.format("%-15s : %d\n", bucket.label(), bucket.count()));
        }
        return stringBuilder.toString();
    }
}
