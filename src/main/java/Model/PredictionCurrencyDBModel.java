package Model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name="prediction_bitcoin_currency_table")
public class PredictionCurrencyDBModel {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredictionCurrencyDBModel that = (PredictionCurrencyDBModel) o;
        return Objects.equals(date, that.date);
    }

    public static int compare(PredictionCurrencyDBModel a, PredictionCurrencyDBModel b) {
        return a.date.compareTo(b.date);
    }

    @Override
    public int hashCode() {

        return Objects.hash(date);
    }

    private long unix_time;
//    @Column
    private String currency;
//    @Column
    private BigDecimal price;
//    @Column
    private Boolean isUse;
//    @Column
    @Id
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getUnix_time() {
        return unix_time;
    }

    public void setUnix_time(long unix_time) {
        this.unix_time = unix_time;
    }

    public Boolean getUse() {
        return isUse;
    }

    public void setUse(Boolean use) {
        isUse = use;
    }



    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "PredictionCurrencyDBModel{" +
                "unix_time=" + unix_time +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                ", isUse=" + isUse +
                ", date=" + date +
                '}';
    }
}
