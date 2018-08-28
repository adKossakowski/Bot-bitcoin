package DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
//import java.util.Date;
import java.util.Calendar;
import java.sql.Date;

public class JsonModelBitcoin {

    @JsonProperty("x")
    private long unixTime;

    @JsonProperty("y")
    private BigDecimal bitcoinPrice;

    public long getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }

    public BigDecimal getBitcoinPrice() {
        return bitcoinPrice;
    }

    public void setBitcoinPrice(BigDecimal bitcoinPrice) {
        this.bitcoinPrice = bitcoinPrice;
    }

    @Override
    public String toString() {
        return "JsonModelBitcoin{" +
                "unixTime=" + unixTime +
                ", bitcoinPrice=" + bitcoinPrice +
                '}';
    }

    public Date unixTimeToDate(){
        return new Date((long)unixTime*1000);
    }
}
