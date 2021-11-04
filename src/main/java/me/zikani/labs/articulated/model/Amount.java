package me.zikani.labs.articulated.model;


import java.util.regex.Pattern;

public class Amount {
    public static final Pattern KWACHA_REGEX_2 = Pattern.compile("(m?w?k)?\\s?(?<amount>\\d+(\\.?\\d+)?)\\s?(?<denomination>((hundred)?thousand|(m|b|tr)(illio)?n)(\\s+kwacha)?)");

    private final double amount;
    private final String denomination;
    private final String currency;
    private final double amountInDenomination;
    private final String denominationRaw;

    public Amount(String currency, double amountInDenomination, String denominationRaw) {
        this.currency = currency;
        this.amountInDenomination = amountInDenomination;
        this.denominationRaw = denominationRaw;
        this.denomination = this.normalizeDenomination();
        this.amount = calcActualAmountInBaseDenomination();
    }

    private String normalizeDenomination() {
        return this.denominationRaw.replaceAll("\\s?kwacha", "")
                .replace("tn", "trillion")
                .replace("bn", "billion")
                .replace("mn", "million");
    }
    private double calcActualAmountInBaseDenomination() {
        return switch (this.denomination) {
            case "million" -> this.amountInDenomination * 1_000_000d;
            case "billion" -> this.amountInDenomination * 1_000_000_000d;
            case "trillion" -> this.amountInDenomination * 1_000_000_000_000d;
            default -> this.amountInDenomination;
        };
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public String getDenomination() {
        return denomination;
    }
}
