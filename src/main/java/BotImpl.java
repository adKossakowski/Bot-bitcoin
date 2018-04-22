import java.math.BigDecimal;

public class BotImpl {

    private BigDecimal botMoneyBitcoin;
    private BigDecimal botMoneyCurrency;
    private char currency;

    public BigDecimal getBotMoneyBitcoin() {
        return botMoneyBitcoin;
    }

    public void setBotMoneyBitcoin(BigDecimal botMoneyBitcoin) {
        this.botMoneyBitcoin = botMoneyBitcoin;
    }

    public BigDecimal getBotMoneyCurrency() {
        return botMoneyCurrency;
    }

    public void setBotMoneyCurrency(BigDecimal botMoneyCurrency) {
        this.botMoneyCurrency = botMoneyCurrency;
    }

    public char getCurrency() {
        return currency;
    }

    public void setCurrency(char currency) {
        this.currency = currency;
    }

    private void buyBitcoin(BigDecimal buy){
        botMoneyBitcoin = botMoneyBitcoin.add(botMoneyCurrency.multiply(checkCurrentCurrency()));
        botMoneyCurrency = botMoneyCurrency.subtract(buy);
    }

    private void sellBitcoin_InBitcoin(BigDecimal sell){
        botMoneyBitcoin = botMoneyBitcoin.subtract(sell);
        botMoneyCurrency = botMoneyCurrency.add(sell.multiply(checkCurrentCurrency()));
    }

    private void sellBitcoin_InCurrency(BigDecimal sell){
        BigDecimal tmpBitcoinMoney = sell.multiply(checkCurrentCurrency());
        botMoneyBitcoin = botMoneyBitcoin.subtract(tmpBitcoinMoney);
        botMoneyCurrency = botMoneyCurrency.add(tmpBitcoinMoney.multiply(checkCurrentCurrency()));
    }

    private BigDecimal checkCurrentCurrency(){
        return null;
    }

    public void checkFutureCurrency(){

    }
}
