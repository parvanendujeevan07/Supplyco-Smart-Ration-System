
package com.mycompany.supplycosmartrationsystem;

public class AAYCard extends RationCard {

    public AAYCard(String cardNo, String holderName) {
        super(cardNo, holderName);
    }

    @Override
    public String getCategory() {
        return "AAY";
    }

    @Override
    public double calculateSubsidy(
        double quantity,
        double marketPrice,
        double subsidizedPrice
    ) {
        return quantity *
            Math.max(0, marketPrice - subsidizedPrice) * 0.90;
    }
}
