
package com.mycompany.supplycosmartrationsystem;

public abstract class RationCard {

    private String cardNo;
    private String holderName;

    public RationCard(String cardNo, String holderName) {
        this.cardNo = cardNo;
        this.holderName = holderName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getHolderName() {
        return holderName;
    }

    public abstract String getCategory();

    public abstract double calculateSubsidy(
        double quantity,
        double marketPrice,
        double subsidizedPrice
    );
}
