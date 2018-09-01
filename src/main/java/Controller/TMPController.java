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
        TypedQuery<HistoryBitcoinDBModel> query = /*(List<HistoryBitcoinDBModel>)*/entityManager.createQuery("FROM HistoryBitcoinDBModel order by unix_time desc", HistoryBitcoinDBModel.class)
                .setMaxResults(predictionParameters.getTest_size() * predictionParameters.getWindow_size() + predictionParameters.getTrain_size() + 2);
        List<HistoryBitcoinDBModel> file_bitcoin_data = query.getResultList();

        entityManager.close();
        entityMangerFactory.close();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("bitcoin_currency_file.csv"), "utf-8"))) {
            writer.write("date,price\n");
            Collections.reverse(file_bitcoin_data);
            for(HistoryBitcoinDBModel item: file_bitcoin_data) {
                writer.write("\"" + item.getUnixtime() + "\"," + item.getPrice() + System.lineSeparator());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
