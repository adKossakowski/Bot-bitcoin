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

    private String typeOfUsage;

    public String getTypeOfUsage() {
        return typeOfUsage;
    }

    public void setTypeOfUsage(String typeOfUsage) {
        this.typeOfUsage = typeOfUsage;
    }

    public BotTableDB(Date date, String currency, BigDecimal money, String typeOfUsage) {
        this.date = date;
        this.currency = currency;
        this.money = money;
        this.typeOfUsage = typeOfUsage;
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
}
