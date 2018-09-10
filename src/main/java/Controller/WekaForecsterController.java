package Controller;

import Model.PredictionCurrencyDBModel;
import Model.PredictionParametersModel;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.eval.TSEvaluation;
import weka.classifiers.timeseries.eval.graph.JFreeChartDriver;
import weka.core.Instances;
import weka.core.converters.CSVLoader;


import javax.persistence.*;
import javax.swing.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WekaForecsterController {

    public void forecastTimeSeries() throws Exception{
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("bitcoin_currency_file.csv"));
        Instances data = loader.getDataSet();
        System.out.println("liczba atrybutów="+data.numAttributes()
                +", liczba obserwacji="+data.numInstances());
        System.out.println(data.firstInstance());
        System.out.println(data.lastInstance());
        System.out.println(data.toSummaryString());

//        train_size > test_size*window_size

        EntityManagerFactory entityMangerFactory_pp = Persistence.createEntityManagerFactory("prediction_parameters");
        EntityManager entityManager_pp = entityMangerFactory_pp.createEntityManager();
        TypedQuery<PredictionParametersModel> query = entityManager_pp.createQuery("Select pp from PredictionParametersModel pp order by id DESC", PredictionParametersModel.class).setMaxResults(1);
        PredictionParametersModel predictionParameters = query.getSingleResult();
        entityManager_pp.close();
        entityMangerFactory_pp.close();
        System.out.println(predictionParameters.toString());
        int train_size;
        int test_size;
        int start_idx;
        int window_size;
        if(predictionParameters != null) {
            train_size = predictionParameters.getTrain_size();
            test_size = predictionParameters.getTest_size();
            start_idx = 1;
            window_size = predictionParameters.getWindow_size();
        }else{
            train_size = 74;
            test_size = 73;
            start_idx = 1;
            window_size = 1;
        }

        Instances train = new Instances(data, start_idx, train_size);
        Instances test = new Instances(data, start_idx+train_size+1, test_size);

        for (int i = 0; i < test.numInstances(); i++)
        {
            System.out.println(test.instance(i));
        }

        // new forecaster
        WekaForecaster forecaster = new WekaForecaster();

        SMOreg svm_reg=new SMOreg();  forecaster.setFieldsToForecast("price");

        forecaster.setBaseForecaster(svm_reg);
        forecaster.getTSLagMaker().setMinLag(1);
        forecaster.getTSLagMaker().setMaxLag(test_size*window_size);
        forecaster.getTSLagMaker().setTimeStampField("date");

        System.out.println("Przed predykcja");
        forecaster.buildForecaster(train, System.out);
        forecaster.primeForecaster(train);

        List<List<NumericPrediction>> forecast = forecaster.forecast(test_size, System.out);

        for (int i = 0; i < test_size; i++) {
            List<NumericPrediction> predsAtStep = forecast.get(i);
            double predicted = predsAtStep.get(0).predicted();
            double predicted_round=Math.round(predicted *100.0)/100.0;

            double actual=test.instance(i).value(test.attribute("price"));
            double actual_round=Math.round(actual *100.0)/100.0;

            double abs_diff = Math.abs(predicted-actual);
            double abs_diff_round=Math.round(abs_diff *10000.0)/10000.0;

            System.out.print("" +  predicted_round
                    + "\t "+ actual_round
                    + "\t abs_diff="+abs_diff_round
                    + "\t " + test.instance(i).value(test.attribute("date"))
                    //+ "\t " + test.instance(i).value(test.attribute("godzina"))
            );
            System.out.println();
        }

        Instances data_eval = new Instances(data, start_idx, train_size+test_size);

        TSEvaluation tse = new TSEvaluation(data_eval, test_size);
        tse.setHorizon(test_size);
        tse.setPrimeWindowSize(test_size*window_size);

        tse.evaluateForecaster(forecaster, System.out);

        System.out.println("Ending of prediction");
        // output the predictions
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();

        EntityManagerFactory emf_date = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_date = emf_date.createEntityManager();
        java.sql.Date hist_date = new java.sql.Date(em_date.createQuery("Select date from HistoryBitcoinDBModel order by date desc", Date.class)
                .setMaxResults(1).getSingleResult().getTime());
        hist_date = BotTableController.addDays(hist_date, 1);
        for (int i = 0; i < forecast.size(); i++) {
            List<NumericPrediction> predsAtStep = forecast.get(i);
            NumericPrediction predForTarget = predsAtStep.get(0);
            System.out.println("" + predForTarget.predicted() + " ");
            PredictionCurrencyDBModel bitcoinObj = new PredictionCurrencyDBModel();
            bitcoinObj.setUnix_time(hist_date.getTime()/1000L);

            bitcoinObj.setPrice(new BigDecimal(predForTarget.predicted()));
            bitcoinObj.setCurrency("USD");
            bitcoinObj.setUse(false);
            bitcoinObj.setDate(hist_date);
            System.out.println("Date: \t " + bitcoinObj.getDate() + " " + hist_date);
            hist_date = BotTableController.addDays(hist_date, 1);
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
            String s = dt.format(bitcoinObj.getDate());
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date query_date = format.parse(s);
            entityManager.getTransaction().begin();
            PredictionCurrencyDBModel is_exist = entityManager.find(PredictionCurrencyDBModel.class, bitcoinObj.getDate());
            if (is_exist != null){
                is_exist.setPrice(bitcoinObj.getPrice());
            }else{
                entityManager.persist(bitcoinObj);
            }
            entityManager.getTransaction().commit();
        }
        entityManager.close();

    }

}
