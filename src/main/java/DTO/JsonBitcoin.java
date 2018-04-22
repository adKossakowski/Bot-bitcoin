package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonBitcoin {

    @JsonProperty("USD")
    private DolarJsonBitcoinData dolarJsonBitcoinData;

    @Override
    public String toString() {
        return "JsonBitcoin{" +
                "dolarJsonBitcoinData=" + dolarJsonBitcoinData +
                '}';
    }

    public DolarJsonBitcoinData getDolarJsonBitcoinData() {
        return dolarJsonBitcoinData;
    }

    public void setDolarJsonBitcoinData(DolarJsonBitcoinData dolarJsonBitcoinData) {
        this.dolarJsonBitcoinData = dolarJsonBitcoinData;
    }

    static class DolarJsonBitcoinData{
         @JsonProperty("last")
        private BigDecimal last;
        @JsonProperty("buy")
        private BigDecimal buy;
        @JsonProperty("sell")
        private BigDecimal sell;
        @JsonProperty("symbol")
        private char symbol;

        public BigDecimal getLast() {
            return last;
        }

        public void setLast(BigDecimal last) {
            this.last = last;
        }

        public BigDecimal getBuy() {
            return buy;
        }

        public void setBuy(BigDecimal buy) {
            this.buy = buy;
        }

        public BigDecimal getSell() {
            return sell;
        }

        public void setSell(BigDecimal sell) {
            this.sell = sell;
        }

        public char getSymbol() {
            return symbol;
        }

        public void setSymbol(char symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return "DolarJsonBitcoinData{" +
                    "last=" + last +
                    ", buy=" + buy +
                    ", sell=" + sell +
                    ", symbol=" + symbol +
                    '}';
        }
    }
}
