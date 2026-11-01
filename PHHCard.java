
package com.mycompany.supplycosmartrationsystem;

public class PHHCard extends RationCard {

    public PHHCard(String cardNo, String holderName) {
        super(cardNo, holderName);
    }

    @Override
    public String getCategory() {
        return "PHH";
    }

    @Override
    public double calculateSubsidy(
        double quantity,
        double marketPrice,
        double subsidizedPrice
    ) {
        return quantity *
            Math.max(0, marketPrice - subsidizedPrice) * 0.60;
    }
}
