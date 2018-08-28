package Model;

import org.springframework.stereotype.Controller;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;
//import java.util.Date;


@Entity
@Table(name="bitcoin_history_table")
public class HistoryBitcoinDBModel {


    @Id
    private long unix_time;
//    @Column
    private String currency;
//    @Column
    private BigDecimal price;
//    @Column
    private Date date;

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

    public long getUnixtime() {
        return unix_time;
    }

    public void setUnixtime(long unixtime) {
        this.unix_time = unixtime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public HistoryBitcoinDBModel(String currency, BigDecimal price, long unixtime, Date date) {
        this.unix_time = unixtime;
        this.currency = currency;
        this.price = price;
        this.date = date;
    }

    public HistoryBitcoinDBModel() {
    }

    public static int compare(HistoryBitcoinDBModel a, HistoryBitcoinDBModel b) {
        return a.date.compareTo(b.date);
    }

    @Override
    public String toString() {
        return "HistoryBitcoinDBModel{" +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                ", unixtime=" + unix_time +
                ", date=" + date +
                '}';
    }
}
