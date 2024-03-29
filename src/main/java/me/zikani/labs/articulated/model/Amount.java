/**
 * MIT License
 *
 * Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.zikani.labs.articulated.model;


import java.math.BigDecimal;
import java.util.regex.Pattern;

public class Amount {
    public static final Pattern KWACHA_REGEX_2 = Pattern.compile("(m?w?k)?\\s?(?<amount>\\d+(\\.?\\d+)?)\\s?(?<denomination>((hundred)?thousand|(m|b|tr)(illio)?n)(\\s+kwacha)?)");

    private final BigDecimal amount;
    private final String denomination;
    private final String currency;
    private final BigDecimal amountInDenomination;
    private final String denominationRaw;

    public Amount(String currency, BigDecimal amountInDenomination, String denominationRaw) {
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
    private BigDecimal calcActualAmountInBaseDenomination() {
        return switch (this.denomination) {
            case "million" -> this.amountInDenomination.multiply(BigDecimal.valueOf(1_000_000d));
            case "billion" -> this.amountInDenomination.multiply(BigDecimal.valueOf(1_000_000_000d));
            case "trillion" -> this.amountInDenomination.multiply(BigDecimal.valueOf(1_000_000_000_000d));
            default -> this.amountInDenomination;
        };
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDenomination() {
        return denomination;
    }
}
