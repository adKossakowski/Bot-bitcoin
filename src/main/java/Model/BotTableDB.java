package Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name="bot_table")
public class BotTableDB {

    @Id
    private Date date;

    private String currency;

    private BigDecimal money;

    private String type_of_usage;

    public String getTypeOfUsage() {
        return type_of_usage;
    }

    public void setTypeOfUsage(String typeOfUsage) {
        this.type_of_usage = typeOfUsage;
    }

    public BotTableDB() {
    }


    public BotTableDB(Date date, String currency, BigDecimal money, String typeOfUsage) {
        this.date = date;
        this.currency = currency;
        this.money = money;
        this.type_of_usage = typeOfUsage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "BotTableDB{" +
                "date=" + date +
                ", currency='" + currency + '\'' +
                ", money=" + money +
                ", typeOfUsage='" + type_of_usage + '\'' +
                '}';
    }
}
