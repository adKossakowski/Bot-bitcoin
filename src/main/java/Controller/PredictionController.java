package Controller;

import Model.HistoryBitcoinDBModel;
import Model.LookupError;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.springframework.web.bind.annotation.*;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.supervised.attribute.TSLagMaker;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prediction")
public class PredictionController {

    private WekaForecaster forecaster;
    private Instances data;


   /* public PredictionController() {
       *//* try {
            EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
            EntityManager entityManager = entityMangerFactory.createEntityManager();
            DataSource source = new DataSource(path);
            data = source.getDataSet();
            if(data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*//*
    }

    private void setupForecaster() {
        if(forecaster == null) {
            try {
                forecaster = new WekaForecaster();
                forecaster.getTSLagMaker().setTimeStampField("DateTime"); // date time stamp
                forecaster.setFieldsToForecast("Price");
                forecaster.buildForecaster(data, System.out);
                forecaster.primeForecaster(data);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public HistoryBitcoinDBModel lookUpHour(DateTime dt) throws LookupError {
        dt = new DateTime(dt.getMillis()).withTime(dt.getHourOfDay(), 0, 0, 0);
        // Get the last data entry's datetime
        DateTime lastDataSetEntry = DateTime.parse(data.get(data.size() - 1).toString(0));
        if(dt.isAfter(lastDataSetEntry)) {
            throw new LookupError("The requested datetime is after the latest data entry datetime!");
        }
        for(Instance i : data) {
            // Get the price and datetime of entry
            Double price = i.classValue();
            DateTime time = DateTime.parse(i.toString(0));
            if(dt.equals(time)) {
                return new HistoryBitcoinDBModel(new BigDecimal(price));
            }
        }
        return new HistoryBitcoinDBModel();
    }



    public Map<DateTime, HistoryBitcoinDBModel> lookUpDay(DateTime dt) throws LookupError {
        Map<DateTime, HistoryBitcoinDBModel> dtPrice = new HashMap<>(24);
        dt = new DateTime(dt.withTimeAtStartOfDay().getMillis());
        DateTime stop = dt.plusDays(1);
        while(dt.isBefore(stop)) {
            dtPrice.put(dt, lookUpHour(dt));
            dt = dt.plusHours(1);
        }
        return dtPrice;
    }



    private Map<DateTime, HistoryBitcoinDBModel> forecast(DateTime from, Hours hoursAhead) {
        try {
            int hourCnt = hoursAhead.getHours();
            List<List<NumericPrediction>> forecast = forecaster.forecast(hourCnt);
            TSLagMaker lm = forecaster.getTSLagMaker();
            // Move the forecaster up to the requested time
            // this is because the forecaster continues from the dataset (15-11-13)
            setForecasterTimeStamp(from);
            Map<DateTime, HistoryBitcoinDBModel> dtPrice = new HashMap<>();
            for(int i = 0; i < hourCnt; i++) {
                NumericPrediction prediction = forecast.get(i).get(0);
                dtPrice.put(from, HistoryBitcoinDBModel.euro(prediction.predicted()));
                // Increase time to next time slot
                from = new DateTime((long)lm.advanceSuppliedTimeValue(from.getMillis()));
            }
            return dtPrice;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void setForecasterTimeStamp(DateTime to) {
        setupForecaster();
        try {
            TSLagMaker lm = forecaster.getTSLagMaker();
            double currentTime = lm.getCurrentTimeStampValue();
            DateTime currentDt = new DateTime((long)currentTime);
            if(to.isAfter(currentDt)) {
                while(to.isAfter(currentDt)) {
                    currentTime = lm.advanceSuppliedTimeValue(currentTime);
                    currentDt = new DateTime((long)currentTime);
                }
            } else {
                // Not sure why you'd ever need to go back in time
                // TODO: This predicts instead of just using the data. Wat.
                while(to.isBefore(currentDt)) {
                    currentTime = lm.decrementSuppliedTimeValue(currentTime);
                    currentDt = new DateTime((long)currentTime);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Map<DateTime, HistoryBitcoinDBModel> forecastDay(DateTime from) {
        setupForecaster();
        try {
            // From http://forums.pentaho.com/showthread.php?89640-Weka-Programmatically-Get-Dates-along-with-Predicted-values
            // Set it so that we are at the start of the hour to match
            // the forecast data, which span between whole hours
            from = new DateTime(from.withTime(from.getHourOfDay(), 0, 0, 0).getMillis());
            return forecast(from, Hours.hours(25));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }*/

}
