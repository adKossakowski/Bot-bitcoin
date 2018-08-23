package Controller;

import DTO.HistoryJsonUpdate;
import DTO.JsonBitcoin;
import DTO.JsonModelBitcoin;
import Model.*;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/currency")
public class JsonController {

    Logger logger;

    RestTemplate restTemplate;
    HistoryJsonUpdate jsonUpdate;
    EntityManagerFactory entityMangerFactory;
    HistoryBitcoinDBModel bitcoinObj;

    @GetMapping(value="predictionParamters", produces = MediaType.APPLICATION_JSON_VALUE)
    public PredictionParametersModel getPredictionParameters(){
        EntityManagerFactory entityMangerFactory_pp = Persistence.createEntityManagerFactory("prediction_parameters");
        EntityManager entityManager_pp = entityMangerFactory_pp.createEntityManager();
        TypedQuery<PredictionParametersModel> query = entityManager_pp.createQuery("Select pp from PredictionParametersModel pp order by id DESC", PredictionParametersModel.class).setMaxResults(1);
        PredictionParametersModel predictionParameters = query.getSingleResult();
        entityManager_pp.close();
        entityMangerFactory_pp.close();
        System.out.println(predictionParameters.toString());
        return predictionParameters;
    }

    @GetMapping(value = "/update")
    public void updateCurrency(){

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        RestTemplate restTemplate = new RestTemplate();
        JsonBitcoin currencyJson = restTemplate.getForObject("https://blockchain.info/pl/ticker", JsonBitcoin.class);
        System.out.println(currencyJson.toString());

    }

    @PostMapping("updateBotTable")
    public void postBotMoney(){
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
    }

    @GetMapping("updateJdbc")
    public void updateJdbc(){
        RestTemplate restTemplate = new RestTemplate();
        jsonUpdate = restTemplate.getForObject("https://blockchain.info/charts/market-price?timespan=7years&format=json", HistoryJsonUpdate.class);
        System.out.println("RestTemplate stop");
        ArrayList<JsonModelBitcoin> bitcoinArrayList = jsonUpdate.getJsonModelArrayList();
//        System.out.println(jsonUpdate.toString());
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
        for(JsonModelBitcoin item: bitcoinArrayList){
            bitcoinObj = new HistoryBitcoinDBModel(jsonUpdate.getCurrency(), item.getBitcoinPrice(), item.getUnixTime(), item.unixTimeToDate());
            System.out.println(bitcoinObj.toString());
            entityManager.getTransaction().begin();
            entityManager.persist(bitcoinObj);
            entityManager.getTransaction().commit();
        }
        entityManager.close();
//        entityMangerFactory.close();
        System.out.println("DB update success");
    }

    @GetMapping("updateToday")
    public boolean updateToday(){
        RestTemplate restTemplate = new RestTemplate();
        jsonUpdate = restTemplate.getForObject("https://blockchain.info/charts/market-price?timespan=1days&format=json", HistoryJsonUpdate.class);
        System.out.println(jsonUpdate.toString());
        ArrayList<JsonModelBitcoin> bitcoinArrayList = jsonUpdate.getJsonModelArrayList();
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
        bitcoinObj = new HistoryBitcoinDBModel(jsonUpdate.getCurrency(), bitcoinArrayList.get(0).getBitcoinPrice(), bitcoinArrayList.get(0).getUnixTime(), bitcoinArrayList.get(0).unixTimeToDate());
        try {
            System.out.println("Entity manager transaction started");
            entityManager.getTransaction().begin();
            entityManager.persist(bitcoinObj);
            entityManager.getTransaction().commit();
            System.out.println(bitcoinObj.toString() + "update db succedded");
            return true;
        } catch(Exception e){
            logger.error("Cannot update jdbc for caused by: ");
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("defaultPrediction")
    public void setPrediction() throws Exception{
        TMPController.create_file_with_data();
        WekaForecsterController wekaForecsterController = new WekaForecsterController();
        wekaForecsterController.forecastTimeSeries();
    }

    @GetMapping("dedicatedPrediction")
    public void setPredictionWithParameters(@PathVariable("trainingSet") int trainingSet,
                                            @PathVariable("testingSet") int testingSet,
                                            @PathVariable("predictionWindow") int predictionWindow) throws Exception{

        EntityManagerFactory entityMangerFactory_pp = Persistence.createEntityManagerFactory("prediction_parameters");
        EntityManager entityManager_pp = entityMangerFactory_pp.createEntityManager();
        PredictionParametersModel pp = new PredictionParametersModel(trainingSet, testingSet, predictionWindow, new Date());
        entityManager_pp.persist(pp);
        entityManager_pp.close();
        entityMangerFactory_pp.close();
        TMPController.create_file_with_data();
        WekaForecsterController wekaForecsterController = new WekaForecsterController();
        wekaForecsterController.forecastTimeSeries();
    }

    @GetMapping("database")
    public Money getPrediction() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar dateParameter = Calendar.getInstance();
        Date dateParameter = new Date();
        PredictionCurrencyDBModel currencyDBModel;
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
        do {
            TypedQuery<PredictionCurrencyDBModel> query = entityManager.createQuery("SELECT p from PredictionCurrencyDBModel p where date = :dateParameter", PredictionCurrencyDBModel.class).setParameter("dateParameter", new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(dateParameter)));
            try {
                currencyDBModel = query.getSingleResult();
                if (currencyDBModel == null) {
                    setPrediction();
                }
                if (currencyDBModel.getPrice().compareTo(new BigDecimal(0)) <= 0) {
                    setPrediction();
                } else {
                    break;
                }
            } catch (NullPointerException e) {
                setPrediction();
            } catch (NullArgumentException e) {
                setPrediction();
            }
        } while(true);

        return new Money(currencyDBModel.getCurrency(), currencyDBModel.getPrice());
    }

    @GetMapping("chartData")
    public ArrayList<ChartDataJson> getChartData() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateParameter = new Date();

        EntityManagerFactory predictionsEntityManagerFactory = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager predictionEntityManager = predictionsEntityManagerFactory.createEntityManager();
        TypedQuery<PredictionCurrencyDBModel> predictionQuery = predictionEntityManager.createQuery("SELECT p from PredictionCurrencyDBModel p where date < :dateParameter order by :dateParameter DESC", PredictionCurrencyDBModel.class).setParameter("dateParameter", new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(dateParameter)));
        ArrayList<PredictionCurrencyDBModel> predictions = new ArrayList<>(predictionQuery.getResultList());

        EntityManagerFactory currencyEntityManagerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager currencyEntityManager = currencyEntityManagerFactory.createEntityManager();
        TypedQuery<HistoryBitcoinDBModel> currencyQuery = currencyEntityManager.createQuery("SELECT p from HistoryBitcoinDBModel p where date < :dateParameter order by :dateParameter DESC", HistoryBitcoinDBModel.class)
                .setParameter("dateParameter", new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(dateParameter)))
                .setMaxResults(predictions.size());
        ArrayList<HistoryBitcoinDBModel> currency = new ArrayList<>(currencyQuery.getResultList());



        return ChartData.jsonParser(predictions, currency);
    }

}
//https://blockchain.info/charts/market-price?timespan=9years&format=json
//https://blockchain.info/charts/market-price?format=json
//http://api.bitcoincharts.com/v1/trades.csv?symbol=krakenUSD&start=16022018
