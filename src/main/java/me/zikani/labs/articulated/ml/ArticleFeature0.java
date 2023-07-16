package me.zikani.labs.articulated.ml;

import lombok.Data;
import me.zikani.labs.articulated.model.Amount;
import me.zikani.labs.articulated.model.Article;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Data
public class ArticleFeature0 {

    private String articleID;

    private String articleTitle;

    private long timestamp;

    private BigDecimal minAmountMWK;

    private BigDecimal maxAmountMWK;

    private Integer numHundredsValuesMWK;

    private Integer numThousandsValuesMWK;

    private Integer numMillionsValuesMWK;

    private Integer numBillionsValuesMWK;

    private Integer numTrillionsValuesMWK;

    private Integer numGreaterThanTrillionsValuesMWK;

    private Integer numKeywordDonations;

    private Integer numKeywordStolen;

    private Integer numKeywordFunding;

    private Integer numKeywordWins;

    private Integer numKeywordCorruption;

    private Integer numKeywordBudget;

    private Integer numKeywordDisbursed;

    private Integer numKeywordLoan;

    private Integer numKeywordBorrowed;

    private Integer numKeywordCredit;

    private Integer numKeywordSponsor;

    private Integer numKeywordMWK;

    private Integer numKeywordUSD;



    public static ArticleFeature0 make(Article article) {
        ArticleFeature0 feature0 = new ArticleFeature0();

        BigDecimal minAmount = BigDecimal.ZERO, maxAmount = BigDecimal.ZERO;
        int hundreds = 0, thousands = 0, millions = 0, billions = 0, trillions = 0, overTrillions = 0;
        for (Amount amount: article.getMentionedAmounts()) {
            minAmount = minAmount.min(amount.getAmount());
            maxAmount = maxAmount.max(amount.getAmount());

            if (amount.getAmount().longValue()  >= 100 && amount.getAmount().longValue() < 1e3) {
                hundreds++;
            } else if (amount.getAmount().longValue()  >= 1e3 && amount.getAmount().longValue() < 1e6) {
                thousands++;
            } else if (amount.getAmount().longValue()  >= 1e6 && amount.getAmount().longValue() < 1e9) {
                millions++;
            } else if (amount.getAmount().longValue()  >= 1e9 && amount.getAmount().longValue() < 1e12) {
                billions++;
            } else if (amount.getAmount().longValue()  >= 1e12 && amount.getAmount().longValue() < 1e15) {
                trillions++;
            } else if (amount.getAmount().longValue() > 1e15) {
                overTrillions++;
            }
        }

        feature0.setArticleID(article.getId());
        feature0.setArticleTitle(article.getTitle());
        feature0.setMinAmountMWK(minAmount);
        feature0.setMaxAmountMWK(maxAmount);
        feature0.setNumHundredsValuesMWK(hundreds);
        feature0.setNumThousandsValuesMWK(thousands);
        feature0.setNumMillionsValuesMWK(millions);
        feature0.setNumBillionsValuesMWK(billions);
        feature0.setNumTrillionsValuesMWK(trillions);
        feature0.setNumGreaterThanTrillionsValuesMWK(overTrillions);
        feature0.setTimestamp(article.getPublishedOn().toEpochSecond(LocalTime.now(), ZoneOffset.UTC));

        feature0.setNumKeywordBudget(article.countOccurencesOf("budget"));
        feature0.setNumKeywordBorrowed(article.countOccurencesOf("borrowed"));
        feature0.setNumKeywordCredit(article.countOccurencesOf("credit"));
        feature0.setNumKeywordFunding(article.countOccurencesOf("funding"));
        feature0.setNumKeywordDonations(article.countOccurencesOf("donates|donation"));
        feature0.setNumKeywordLoan(article.countOccurencesOf("loan"));
        feature0.setNumKeywordCorruption(article.countOccurencesOf("corruption"));
        feature0.setNumKeywordMWK(article.countOccurencesOf("mwk"));
        feature0.setNumKeywordUSD(article.countOccurencesOf("usd"));
        feature0.setNumKeywordStolen(article.countOccurencesOf("stolen"));
        feature0.setNumKeywordWins(article.countOccurencesOf("wins|winner"));
        feature0.setNumKeywordDisbursed(article.countOccurencesOf("disbursed"));
        feature0.setNumKeywordSponsor(article.countOccurencesOf("sponsors"));

        return feature0;
    }
}
