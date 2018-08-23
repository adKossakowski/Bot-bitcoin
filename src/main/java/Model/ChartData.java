package Model;

import javafx.print.Collation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ChartData {

    private PredictionCurrencyDBModel predictions;
    private HistoryBitcoinDBModel currency;

    public ChartData() {
    }

    public ChartData(PredictionCurrencyDBModel predictions, HistoryBitcoinDBModel currency) {
        this.predictions = predictions;
        this.currency = currency;
    }

    public PredictionCurrencyDBModel getPredictions() {
        return predictions;
    }

    public void setPredictions(PredictionCurrencyDBModel predictions) {
        this.predictions = predictions;
    }

    public HistoryBitcoinDBModel getCurrency() {
        return currency;
    }

    public void setCurrency(HistoryBitcoinDBModel currency) {
        this.currency = currency;
    }

    public static ArrayList<ChartDataJson> jsonParser(ArrayList<PredictionCurrencyDBModel> predictions, ArrayList<HistoryBitcoinDBModel> currency) {
//        Collections.sort(predictions, (PredictionCurrencyDBModel a, PredictionCurrencyDBModel b) -> {return a.getDate().compareTo(b.getDate())});
        //https://stackoverflow.com/questions/2784514/sort-arraylist-of-custom-objects-by-property
//        Arrays.sort(currency, (HistoryBitcoinDBModel a,HistoryBitcoinDBModel b) -> a.getDate().compareTo(b.getDate()));
        Collections.sort(predictions, PredictionCurrencyDBModel::compare);
        Collections.sort(currency, HistoryBitcoinDBModel::compare);
        ArrayList<ChartDataJson> r = new ArrayList<>();
        for (PredictionCurrencyDBModel p: predictions
             ) {
            for (HistoryBitcoinDBModel c: currency
                 ) {
                if(p.getDate()==c.getDate()){
                    r.add(new ChartDataJson(p.getDate(), p.getPrice(), c.getPrice()));
                }
            }
        }
       return r;
    }
}
