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
    private final JFrame frame = new JFrame("RxFSStat");
    private final JTextField pathField = new JTextField("Target directory");
    private final JTextArea resultsArea = new JTextArea(20, 40);
    private final JProgressBar progressBar = new JProgressBar();
    private final JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 100));
    private final JSpinner bandCountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    private final JButton browseButton = new JButton("Browse");
    private final JButton startButton = new JButton("Start");
    private final JButton stopButton = new JButton("Stop");

    private final RxFSStatService service = new RxFSStatService();
    private String targetPath = "";
    private Disposable subscription = Disposable.disposed();

    public ReactiveGUI() {
        setupLayout();
        setupActions();
        finalizeFrame();
    }

    private void setupLayout() {
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        pathField.setEditable(false);
        topPanel.add(pathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);

        resultsArea.setEditable(false);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createConfigPanel(), BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(createControlPanel(), BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Max Size:"));
        panel.add(sizeSpinner);
        panel.add(new JLabel("Bands:"));
        panel.add(bandCountSpinner);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        stopButton.setEnabled(false);
        panel.add(startButton);
        panel.add(stopButton);
        return panel;
    }

    private void setupActions() {
        startButton.addActionListener(_ -> startScan());
        stopButton.addActionListener(_ -> subscription.dispose());
        browseButton.addActionListener(_ -> handleBrowse());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                subscription.dispose();
            }
        });
    }

    private void handleBrowse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            targetPath = chooser.getSelectedFile().getAbsolutePath();
            pathField.setText(targetPath);
        }
    }

    private void startScan() {
        if (targetPath.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Select a directory.");
            return;
        }

        ScanParameters params = new ScanParameters(
                Path.of(targetPath),
                (int) sizeSpinner.getValue(),
                (int) bandCountSpinner.getValue()
        );

        subscription.dispose();
        subscription = service.getFSReportUpdates(params)
                .observeOn(Schedulers.from(SwingUtilities::invokeLater))
                .doOnSubscribe(_ -> updateUIState(true))
                .doFinally(() -> updateUIState(false))
                .subscribe(
                    report -> resultsArea.setText(formatReport(report)),
                    error -> resultsArea.setText("Error: " + error.getMessage())
                );
    }

    private void updateUIState(boolean isRunning) {
        startButton.setEnabled(!isRunning);
        stopButton.setEnabled(isRunning);
        progressBar.setIndeterminate(isRunning);
    }

    private void finalizeFrame() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
