package Model;

import java.math.BigDecimal;

public class BotJson {

    private BigDecimal Money;

    private BigDecimal Bitcoins;

    private BigDecimal ostatniKurs;

    private BigDecimal ostatniePrzewidywania;

    public BotJson() {
    }

    public BotJson(BigDecimal money, BigDecimal bitcoins, BigDecimal ostatniKurs, BigDecimal ostatniePrzewidywania) {
        Money = money;
        Bitcoins = bitcoins;
        this.ostatniKurs = ostatniKurs;
        this.ostatniePrzewidywania = ostatniePrzewidywania;
    }

    public BigDecimal getMoney() {
        return Money;
    }

    public void setMoney(BigDecimal money) {
        Money = money;
    }

    public BigDecimal getBitcoins() {
        return Bitcoins;
    }

    public void setBitcoins(BigDecimal bitcoins) {
        Bitcoins = bitcoins;
    }

    public BigDecimal getOstatniKurs() {
        return ostatniKurs;
    }

    public void setOstatniKurs(BigDecimal ostatniKurs) {
        this.ostatniKurs = ostatniKurs;
    }

    public BigDecimal getOstatniePrzewidywania() {
        return ostatniePrzewidywania;
    }

    public void setOstatniePrzewidywania(BigDecimal ostatniePrzewidywania) {
        this.ostatniePrzewidywania = ostatniePrzewidywania;
    }

    @Override
    public String toString() {
        return "BotJson{" +
                "Money=" + Money +
                ", Bitcoins=" + Bitcoins +
                ", ostatniKurs=" + ostatniKurs +
                ", ostatniePrzewidywania=" + ostatniePrzewidywania +
                '}';
    }
}
