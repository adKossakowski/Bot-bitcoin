package Controller;

import Model.HistoryBitcoinDBModel;
import Model.PredictionParametersModel;
import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.PostMapping;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instances;
import weka.filters.supervised.attribute.TSLagMaker;

import javax.persistence.*;
import java.io.*;
import java.util.Collections;
import java.util.List;

public class TMPController {

    public static void create_file_with_data(){
        EntityManagerFactory entityMangerFactory_pp = Persistence.createEntityManagerFactory("prediction_parameters");
        EntityManager entityManager_pp = entityMangerFactory_pp.createEntityManager();
        TypedQuery<PredictionParametersModel> query_pp = entityManager_pp.createQuery("Select pp from PredictionParametersModel pp order by id DESC", PredictionParametersModel.class).setMaxResults(1);
        PredictionParametersModel predictionParameters = query_pp.getSingleResult();
        entityManager_pp.close();
        entityMangerFactory_pp.close();

        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
//        TypedQuery<HistoryBitcoinDBModel> query = /*(List<HistoryBitcoinDBModel>)*/entityManager.createQuery("SELECT bht FROM HistoryBitcoinDBModel bht where bht.unix_time >= 1388534400 AND bht.unix_time <= 1432944000", HistoryBitcoinDBModel.class);
        TypedQuery<HistoryBitcoinDBModel> query = /*(List<HistoryBitcoinDBModel>)*/entityManager.createQuery("FROM HistoryBitcoinDBModel order by unix_time desc", HistoryBitcoinDBModel.class)
                .setMaxResults(predictionParameters.getTest_size() * predictionParameters.getWindow_size() + predictionParameters.getTrain_size() + 2);
        List<HistoryBitcoinDBModel> file_bitcoin_data = query.getResultList();

        entityManager.close();
        entityMangerFactory.close();
       /* for(HistoryBitcoinDBModel item: file_bitcoin_data) {
            System.out.println(item.getUnixtime() + " " + item.getPrice() + "\n");
        }*/
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("bitcoin_currency_file.csv"), "utf-8"))) {
//            writer.write("Date  Price");
            writer.write("date,price\n");
            Collections.reverse(file_bitcoin_data);
            for(HistoryBitcoinDBModel item: file_bitcoin_data) {
                writer.write("\"" + item.getUnixtime() + "\"," + item.getPrice() + System.lineSeparator());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @PostMapping("/SVMPrediction")
    public static void currency_prediction(){
        try {
            System.out.println("Bitcoin currency forecasting");
//            String pathToWineData = weka.core.WekaPackageManager.PACKAGES_DIR.toString()
//                    + File.separator + "timeseriesForecasting" + File.separator + "sample-data"
//                    + File.separator + "wine.arff";
            Instances bitcoin_currency = new Instances(new BufferedReader(new FileReader("bitcoin_currency_file.csv")));
            WekaForecaster forecaster = new WekaForecaster();
            forecaster.setFieldsToForecast("Date,Price");
            //forecaster.setBaseForecaster(new GaussianProcesses());
            forecaster.getTSLagMaker().setTimeStampField("Date"); // date time stamp
            forecaster.getTSLagMaker().setMinLag(1);
            forecaster.getTSLagMaker().setMaxLag(12); // monthly data
            forecaster.getTSLagMaker().setAddMonthOfYear(true);
            //forecaster.getTSLagMaker().setAddQuarterOfYear(true);
            forecaster.buildForecaster(bitcoin_currency, System.out);
            forecaster.primeForecaster(bitcoin_currency);
            List<List<NumericPrediction>> forecast = forecaster.forecast(12, System.out);
            DateTime currentDt = getCurrentDateTime(forecaster.getTSLagMaker());
            for (int i = 0; i < 12; i++) {
                List<NumericPrediction> predsAtStep = forecast.get(i);
                System.out.print(currentDt + " ");
                for (int j = 0; j < 2; j++) {
                    NumericPrediction predForTarget = predsAtStep.get(j);
                    System.out.print("" + predForTarget.predicted() + " ");
                }
                System.out.println();
                currentDt = advanceTime(forecaster.getTSLagMaker(), currentDt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static DateTime getCurrentDateTime(TSLagMaker lm) throws Exception {
        return new DateTime((long)lm.getCurrentTimeStampValue());
    }



    private static DateTime advanceTime(TSLagMaker lm, DateTime dt) {
        return new DateTime((long)lm.advanceSuppliedTimeValue(dt.getMillis()));
    }
}
