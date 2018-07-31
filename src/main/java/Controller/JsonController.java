package Controller;

import DTO.HistoryJsonUpdate;
import DTO.JsonBitcoin;
import DTO.JsonModelBitcoin;
import Model.HistoryBitcoinDBModel;
import Model.Money;
import Model.PredictionCurrencyDBModel;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


@RestController
@RequestMapping("/currency")
public class JsonController {

    Logger logger;

    RestTemplate restTemplate;
    HistoryJsonUpdate jsonUpdate;
    EntityManagerFactory entityMangerFactory;
    HistoryBitcoinDBModel bitcoinObj;

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
                    throw new NullPointerException();
                }
                if (currencyDBModel.getPrice() <= 0) {
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

}
//https://blockchain.info/charts/market-price?timespan=9years&format=json
//https://blockchain.info/charts/market-price?format=json
//http://api.bitcoincharts.com/v1/trades.csv?symbol=krakenUSD&start=16022018
