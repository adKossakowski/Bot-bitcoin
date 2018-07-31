package Controller;

import Model.PredictionCurrencyDBModel;
import org.hibernate.Session;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.timeseries.TSForecaster;
import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.eval.TSEvaluation;
import weka.classifiers.timeseries.eval.graph.JFreeChartDriver;
import weka.core.Instances;
import weka.core.converters.CSVLoader;


import javax.lang.model.util.ElementScanner6;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
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
        //liczba atrybutów=4, liczba obserwacji=8733
        System.out.println(data.firstInstance());
        //1,01/02/2014,0,24.25
        System.out.println(data.lastInstance());
        //8733,31/12/2014,23,11.25
        System.out.println(data.toSummaryString());

        /*int train_size=30*24;
        int test_size=4*24;
        int start_idx=4000;
        int window_size=4;*/

        int train_size=74;
        int test_size=73;
        int start_idx=1;
        int window_size=1;

        Instances train = new Instances(data, start_idx, train_size);
        Instances test = new Instances(data, start_idx+train_size+1, test_size);

		 /*ArffSaver s= new ArffSaver();
		 s.setInstances(train);
		 s.setFile(new File("train.arff"));
		 s.writeBatch();
		 */

        for (int i = 0; i < test.numInstances(); i++)
        {
            System.out.println(test.instance(i));
        }



        // new forecaster
        WekaForecaster forecaster = new WekaForecaster();
        forecaster.setFieldsToForecast("price");

        SMOreg svm_reg=new SMOreg();
        //String[] svm_options = weka.core.Utils.splitOptions("-C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -T 0.001 -V -P 1.0E-12 -L 0.001 -W 1\" " +
        //		"-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -L -C 250007\"");
        //svm_reg.setOptions(svm_options);
        //Kernel ker=svm_reg.getKernel();
        //String[] svm_options = weka.core.Utils.splitOptions("-E 2");
        //svm_reg.setOptions(svm_options);

        forecaster.setBaseForecaster(svm_reg);
        forecaster.getTSLagMaker().setMinLag(1);
        forecaster.getTSLagMaker().setMaxLag(test_size*window_size);
        forecaster.getTSLagMaker().setTimeStampField("date");

        System.out.println("Przed predykcja");
        forecaster.buildForecaster(train, System.out);
        forecaster.primeForecaster(train);

        List<List<NumericPrediction>> forecast = forecaster.forecast(test_size, System.out);

        // output the predictions. Outer list is over the steps; inner list is over
        // the targets
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

        System.out.println(tse.getEvaluateOnTestData());
        System.out.println(tse.getEvaluateOnTrainingData());
        System.out.println(tse.getEvaluationModules());
        System.out.println(tse.getPrimeForTestDataWithTestData());
        System.out.println(tse.getPrimeWindowSize());

        tse.evaluateForecaster(forecaster, System.out);
        System.out.println(tse.toSummaryString());

        List<String> targets=new ArrayList<String>();
        targets.add("flow");

        List<Integer> steps= new ArrayList<Integer>();
        steps.add(test_size);
        System.out.println("Ending of prediction");
        // output the predictions
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
        //teuncate table before new insertion

//        Query query = entityManager.createNativeQuery("truncate table PredictionCurrencyDBModel");
//        query.executeUpdate();

        long unixTime = System.currentTimeMillis() / 1000L;

        for (int i = 0; i < forecast.size(); i++) {
            List<NumericPrediction> predsAtStep = forecast.get(i);
            NumericPrediction predForTarget = predsAtStep.get(0);
            System.out.println("" + predForTarget.predicted() + " ");
            PredictionCurrencyDBModel bitcoinObj = new PredictionCurrencyDBModel();
            bitcoinObj.setUnix_time(unixTime);

            bitcoinObj.setPrice(predForTarget.predicted());
            bitcoinObj.setCurrency("USD");
            bitcoinObj.setUse(false);
            bitcoinObj.setDate(new java.util.Date(unixTime*1000L));
            unixTime+=86400;
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
            String s = dt.format(bitcoinObj.getDate());
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date query_date = format.parse(s);
            entityManager.getTransaction().begin();
            PredictionCurrencyDBModel is_exist = entityManager.find(PredictionCurrencyDBModel.class, query_date);
            if (is_exist != null){
                is_exist.setPrice(bitcoinObj.getPrice());
            }else{
                entityManager.persist(bitcoinObj);
            }
            entityManager.getTransaction().commit();
        }
        entityManager.close();

//        JFreeChartDriver driver= new JFreeChartDriver();
//        //JPanel panel=tse.graphFutureForecastOnTraining(driver, forecaster,targets);
//        JPanel panel=tse.graphPredictionsForStepsOnTraining(driver.getDefaultDriver(), (TSForecaster)forecaster, "price",steps , 0);
//        driver.saveChartToFile(panel, "wykres_czasu_flow.png", 1200, 800);
//
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
//        panel.setLayout(layout);
//        frame.setSize(1600, 1000);
//        frame.add(panel);
//        frame.setVisible(true);

    }

    public void testPrediction() throws Exception{
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("2014 MIDAS Site 10 AL1811 per hour.csv"));
        Instances data = loader.getDataSet();
        System.out.println("liczba atrybutów="+data.numAttributes()
                +", liczba obserwacji="+data.numInstances());
        //liczba atrybutów=4, liczba obserwacji=8733
        System.out.println(data.firstInstance());
        //1,01/02/2014,0,24.25
        System.out.println(data.lastInstance());
        //8733,31/12/2014,23,11.25
        System.out.println(data.toSummaryString());

        int train_size=30*24;
        int test_size=4*24;
        int start_idx=4000;
        int window_size=4;

        Instances train = new Instances(data, start_idx, train_size);
        Instances test = new Instances(data, start_idx+train_size+1, test_size);

		 /*ArffSaver s= new ArffSaver();
		 s.setInstances(train);
		 s.setFile(new File("train.arff"));
		 s.writeBatch();
		 */

        for (int i = 0; i < test.numInstances(); i++)
        {
            System.out.println(test.instance(i));
        }



        // new forecaster
        WekaForecaster forecaster = new WekaForecaster();
        forecaster.setFieldsToForecast("flow");

        SMOreg svm_reg=new SMOreg();
        //String[] svm_options = weka.core.Utils.splitOptions("-C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -T 0.001 -V -P 1.0E-12 -L 0.001 -W 1\" " +
        //		"-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -L -C 250007\"");
        //svm_reg.setOptions(svm_options);
        //Kernel ker=svm_reg.getKernel();
        //String[] svm_options = weka.core.Utils.splitOptions("-E 2");
        //svm_reg.setOptions(svm_options);

        forecaster.setBaseForecaster(svm_reg);
        forecaster.getTSLagMaker().setMinLag(1);
        forecaster.getTSLagMaker().setMaxLag(test_size*window_size);
        forecaster.getTSLagMaker().setTimeStampField("idx");

        System.out.println("Przed predykcja");
        forecaster.buildForecaster(train, System.out);
        forecaster.primeForecaster(train);

        List<List<NumericPrediction>> forecast = forecaster.forecast(test_size, System.out);

        // output the predictions. Outer list is over the steps; inner list is over
        // the targets
        for (int i = 0; i < test_size; i++) {
            List<NumericPrediction> predsAtStep = forecast.get(i);
            double predicted = predsAtStep.get(0).predicted();
            double predicted_round=Math.round(predicted *100.0)/100.0;

            double actual=test.instance(i).value(test.attribute("flow"));
            double actual_round=Math.round(actual *100.0)/100.0;

            double abs_diff = Math.abs(predicted-actual);
            double abs_diff_round=Math.round(abs_diff *10000.0)/10000.0;

            System.out.print("" +  predicted_round
                    + "\t "+ actual_round
                    + "\t abs_diff="+abs_diff_round
                    + "\t " + test.instance(i).stringValue(test.attribute("data"))
                    + "\t " + test.instance(i).value(test.attribute("godzina"))
            );
            System.out.println();
        }


        Instances data_eval=new Instances(data, start_idx, train_size+test_size);

        TSEvaluation tse=new TSEvaluation(data_eval, test_size);
        tse.setHorizon(test_size);
        tse.setPrimeWindowSize(test_size*window_size);

        System.out.println(tse.getEvaluateOnTestData());
        System.out.println(tse.getEvaluateOnTrainingData());
        System.out.println(tse.getEvaluationModules());
        System.out.println(tse.getPrimeForTestDataWithTestData());
        System.out.println(tse.getPrimeWindowSize());

        tse.evaluateForecaster(forecaster, System.out);
        System.out.println(tse.toSummaryString());

        List<String> targets=new ArrayList<String>();
        targets.add("flow");

        List<Integer> steps= new ArrayList<Integer>();
        steps.add(test_size);

        JFreeChartDriver driver= new JFreeChartDriver();
        JPanel panel=tse.graphFutureForecastOnTraining(driver, forecaster,targets);
        //JPanel panel=tse.graphPredictionsForStepsOnTraining(driver.getDefaultDriver(), (TSForecaster)forecaster, "temperatura",steps , 0);
        driver.saveChartToFile(panel, "wykres_czasu_flow.png", 1200, 800);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        frame.setSize(1600, 1000);
        frame.add(panel);
        frame.setVisible(true);

    }
}
