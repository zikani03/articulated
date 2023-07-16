package me.zikani.labs.articulated.ml;

import lombok.Data;
import me.zikani.labs.articulated.model.Amount;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.processor.WordFrequencyCounter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Integer> keywords;

    public static ArticleFeature0 make(Article article) {
        ArticleFeature0 feature0 = new ArticleFeature0();
        feature0.keywords = new HashMap<>();

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

        feature0.countAndSet("budget", "budget", article);
        feature0.countAndSet("revenue", "revenue|revenues|profits|sales", article);
        feature0.countAndSet("borrowed", article);
        feature0.countAndSet("credit", article);
        feature0.countAndSet("funding", "fund|funding", article);
        feature0.countAndSet("donations", "donates|donation", article);
        feature0.countAndSet("fundraiser", "fundraiser|fundraising", article);
        feature0.countAndSet("loan", "loan|loans", article);
        feature0.countAndSet("corruption", article);
        feature0.countAndSet("bribes", article);
        feature0.countAndSet("mwk", article);
        feature0.countAndSet("usd", article);
        feature0.countAndSet("stolen", article);
        feature0.countAndSet("win", "wins|winner|winnings", article);
        feature0.countAndSet("disbursed", article);
        feature0.countAndSet("sponsorship", "sponsor|sponsors|sponsorship", article);

        return feature0;
    }

    private void countAndSet(String keywordKey, Article article) {
        this.keywords.put(keywordKey, article.countOccurencesOf(keywordKey));
    }
    private void countAndSet(String keywordKey, String keywordValue, Article article) {
        this.keywords.put(keywordKey, article.countOccurencesOf(keywordValue));
    }
}
