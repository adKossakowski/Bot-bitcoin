package Model;

public class Money {

    private String currency;
    private double amount;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Money() {
    }

    public Money(String currency, double amount) {

        this.currency = currency;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
