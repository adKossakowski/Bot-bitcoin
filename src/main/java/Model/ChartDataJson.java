package Model;

import java.math.BigDecimal;
import java.util.Date;

public class ChartDataJson {

    private Date date;
    private BigDecimal prediction;
    private BigDecimal currency;

    public ChartDataJson(Date date, BigDecimal prediction, BigDecimal currency) {
        this.prediction = prediction;
        this.currency = currency;
        this.date = date;
    }

    public ChartDataJson() {
    }

    public BigDecimal getPrediction() {
        return prediction;
    }

    public void setPrediction(BigDecimal prediction) {
        this.prediction = prediction;
    }

    public BigDecimal getCurrency() {
        return currency;
    }

    public void setCurrency(BigDecimal currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
