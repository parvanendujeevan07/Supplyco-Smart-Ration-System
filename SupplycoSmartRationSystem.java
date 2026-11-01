
package com.mycompany.supplycosmartrationsystem;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Locale;

public class SupplycoSmartRationSystem extends JFrame {

    private JTextArea outputArea;

    public SupplycoSmartRationSystem() {
        setTitle("Supplyco Smart Ration Card & Subsidy Allocation Ledger System");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel(
            "Supplyco Smart Ration System",
            SwingConstants.CENTER
        );

        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton processButton = new JButton("Process Transactions");
        JButton viewButton = new JButton("View Audit Ledger");
        JButton exitButton = new JButton("Exit");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(processButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(exitButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        processButton.addActionListener(e -> processTransactions());
        viewButton.addActionListener(e -> viewAuditLedger());
        exitButton.addActionListener(e -> System.exit(0));
    }

    // Creates the correct ration card object using its category.
    private RationCard createCard(
        String cardNo,
        String holderName,
        String category
    ) {
        switch (category.toUpperCase(Locale.ROOT)) {
            case "AAY":
                return new AAYCard(cardNo, holderName);

            case "PHH":
                return new PHHCard(cardNo, holderName);

            case "NPHH":
                return new NPHHCard(cardNo, holderName);

            default:
                throw new IllegalArgumentException(
                    "Invalid ration card category: " + category
                );
        }
    }

    private void processTransactions() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Daily ePOS Transactions File");

        if (chooser.showOpenDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File inputFile = chooser.getSelectedFile();

        File outputFile = new File(
            inputFile.getParentFile(),
            "monthly_supplyco_subsidy_audit.txt"
        );

        int validCount = 0;
        int rejectedCount = 0;
        double totalSubsidy = 0.0;

        StringBuilder report = new StringBuilder();

        report.append("SUPPLYCO MONTHLY SUBSIDY AUDIT LEDGER\n");
        report.append("=====================================\n\n");

        try (
            BufferedReader reader = new BufferedReader(
                new FileReader(inputFile)
            );
            PrintWriter writer = new PrintWriter(
                new FileWriter(outputFile)
            )
        ) {

            String line;

            writer.println("SUPPLYCO MONTHLY SUBSIDY AUDIT LEDGER");
            writer.println("=====================================");
            writer.println();

            writer.printf(
                "%-12s %-18s %-8s %-10s%n",
                "Card No", "Holder", "Category", "Subsidy"
            );

            writer.println("-----------------------------------------------");

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                // Skip the CSV header row.
                if (line.toLowerCase(Locale.ROOT)
                        .startsWith("cardno,")) {
                    continue;
                }

                try {

                    String[] data = line.split(",", -1);

                    if (data.length != 6) {
                        throw new IllegalArgumentException(
                            "Expected exactly 6 fields."
                        );
                    }

                    String cardNo = data[0].trim();
                    String holderName = data[1].trim();
                    String category = data[2].trim();

                    if (cardNo.isEmpty() || holderName.isEmpty()) {
                        throw new IllegalArgumentException(
                            "Card number and holder name cannot be empty."
                        );
                    }

                    double quantity =
                        Double.parseDouble(data[3].trim());

                    double marketPrice =
                        Double.parseDouble(data[4].trim());

                    double subsidizedPrice =
                        Double.parseDouble(data[5].trim());

                    if (!Double.isFinite(quantity)
                            || !Double.isFinite(marketPrice)
                            || !Double.isFinite(subsidizedPrice)
                            || quantity <= 0
                            || marketPrice < 0
                            || subsidizedPrice < 0
                            || subsidizedPrice > marketPrice) {

                        throw new IllegalArgumentException(
                            "Invalid quantity or price values."
                        );
                    }

                    // Runtime polymorphism:
                    // The selected subclass calculates the subsidy.
                    RationCard card = createCard(
                        cardNo,
                        holderName,
                        category
                    );

                    double subsidy = card.calculateSubsidy(
                        quantity,
                        marketPrice,
                        subsidizedPrice
                    );

                    totalSubsidy += subsidy;
                    validCount++;

                    String entry = String.format(
                        Locale.ROOT,
                        "%-12s %-18s %-8s Rs. %.2f",
                        card.getCardNo(),
                        card.getHolderName(),
                        card.getCategory(),
                        subsidy
                    );

                    writer.println(entry);
                    report.append(entry).append("\n");

                } catch (IllegalArgumentException ex) {

                    // NumberFormatException is already a subclass
                    // of IllegalArgumentException, so catch only this.

                    rejectedCount++;

                    String error = "REJECTED: " + line
                        + " | Reason: " + ex.getMessage();

                    writer.println(error);
                    report.append(error).append("\n");
                }
            }

            writer.println();
            writer.println("-----------------------------------------------");
            writer.println("Valid transactions: " + validCount);
            writer.println("Rejected transactions: " + rejectedCount);

            writer.printf(
                Locale.ROOT,
                "Total illustrative subsidy: Rs. %.2f%n",
                totalSubsidy
            );

            report.append("\n-----------------------------------------------\n");
            report.append("Valid transactions: ")
                  .append(validCount).append("\n");

            report.append("Rejected transactions: ")
                  .append(rejectedCount).append("\n");

            report.append(String.format(
                Locale.ROOT,
                "Total illustrative subsidy: Rs. %.2f%n",
                totalSubsidy
            ));

            outputArea.setText(report.toString());

            JOptionPane.showMessageDialog(
                this,
                "Transactions processed successfully!\n"
                + "Valid: " + validCount
                + "\nRejected: " + rejectedCount
                + "\nAudit file saved at:\n"
                + outputFile.getAbsolutePath()
            );

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                this,
                "File error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void viewAuditLedger() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Audit Ledger File");

        if (chooser.showOpenDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try (
            BufferedReader reader = new BufferedReader(
                new FileReader(file)
            )
        ) {

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            outputArea.setText(content.toString());

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                this,
                "Unable to read audit file: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new SupplycoSmartRationSystem().setVisible(true);
        });
    }
}
