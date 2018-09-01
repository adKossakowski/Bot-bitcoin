package Model;

import java.math.BigDecimal;

public class BotJson {

    private BigDecimal ostatniKurs;

    private BigDecimal ostatniePrzewidywania;

    public BotJson() {
    }

    public BotJson(BigDecimal ostatniKurs, BigDecimal ostatniePrzewidywania) {
        this.ostatniKurs = ostatniKurs;
        this.ostatniePrzewidywania = ostatniePrzewidywania;
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
                ", ostatniKurs=" + ostatniKurs +
                ", ostatniePrzewidywania=" + ostatniePrzewidywania +
                '}';
    }
}
