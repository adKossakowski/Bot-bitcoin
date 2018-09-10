package Controller;

import Model.HistoryJsonUpdate;
import Model.JsonBitcoin;
import Model.JsonModelBitcoin;
import Model.*;
//import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.apache.commons.math3.exception.NullArgumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
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
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/currency")
public class MainController {

    HistoryJsonUpdate jsonUpdate;
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

    @GetMapping("updateJdbc")
    public void updateJdbc(){
        RestTemplate restTemplate = new RestTemplate();
        jsonUpdate = restTemplate.getForObject("https://blockchain.info/charts/market-price?timespan=7years&format=json", HistoryJsonUpdate.class);
        System.out.println("RestTemplate stop");
        ArrayList<JsonModelBitcoin> bitcoinArrayList = jsonUpdate.getJsonModelArrayList();
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


    private void updateToday(int days){
        StringBuffer sb = new StringBuffer("https://blockchain.info/charts/market-price?timespan=" + (days-1) + "days&format=json");
        RestTemplate restTemplate = new RestTemplate();
        jsonUpdate = restTemplate.getForObject(sb.toString(), HistoryJsonUpdate.class);
        System.out.println(jsonUpdate.toString());
        ArrayList<JsonModelBitcoin> bitcoinArrayList = jsonUpdate.getJsonModelArrayList();
        EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager entityManager = entityMangerFactory.createEntityManager();
        for(JsonModelBitcoin item: bitcoinArrayList) {
            bitcoinObj = new HistoryBitcoinDBModel(jsonUpdate.getCurrency(), item.getBitcoinPrice(),  item.getUnixTime(), item.unixTimeToDate());
            try {
                System.out.println("Entity manager transaction started");
                entityManager.getTransaction().begin();
                entityManager.persist(bitcoinObj);
                entityManager.getTransaction().commit();
                System.out.println(bitcoinObj.toString() + "update db succedded");
            } catch (Exception e) {
               System.out.println(e.toString());
            }
            System.out.println(bitcoinObj.toString());
        }
        entityManager.close();
        entityMangerFactory.close();
    }

    private HistoryBitcoinDBModel getLastHistoryBitcoinDBModel(){
        EntityManagerFactory emf_bitcoin_history = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_bitcoin_hostory = emf_bitcoin_history.createEntityManager();
        TypedQuery<HistoryBitcoinDBModel> query = em_bitcoin_hostory.createQuery("Select p from HistoryBitcoinDBModel p order by date DESC", HistoryBitcoinDBModel.class).setMaxResults(1);
        HistoryBitcoinDBModel last_history_rate = query.getSingleResult();
        return last_history_rate;
    }

    private HistoryBitcoinDBModel reload_HistoryBitcoinDBModel(){
        this.updateJdbc();
        return this.getLastHistoryBitcoinDBModel();
    }

    @GetMapping("initProgram")
    public void initProgram(){
        java.util.Date utilDate = new java.util.Date();
        HistoryBitcoinDBModel last_history_rate = this.getLastHistoryBitcoinDBModel();
        if(last_history_rate == null){
            last_history_rate = this.reload_HistoryBitcoinDBModel();
        }else if(last_history_rate.getDate() == new java.sql.Date(utilDate.getTime() - 1)){
            this.updateToday(1);
        }else{
            long daydiff = new java.sql.Date(utilDate.getTime() - 1).getTime() - last_history_rate.getDate().getTime();
            this.updateToday((int)TimeUnit.DAYS.convert(daydiff, TimeUnit.MILLISECONDS));
        }
    }

    @GetMapping("defaultPrediction")//wywwołanie predykcji dla ostatnich danych z bazy
    public void newPrediction() throws Exception{
        TMPController.create_file_with_data();
        WekaForecsterController wekaForecsterController = new WekaForecsterController();
        wekaForecsterController.forecastTimeSeries();
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("dedicatedPrediction")//zmienienie danych do predykcji
    public void newPredictionWithParameters(@RequestParam ("trainSet") int trainingSet,
                                            @RequestParam ("testSet") int testingSet,
                                            @RequestParam ("predWindow") int predictionWindow) throws Exception{
        if(trainingSet > testingSet * predictionWindow) {
            System.out.println("Dobrze");
            EntityManagerFactory entityMangerFactory_pp = Persistence.createEntityManagerFactory("prediction_parameters");
            EntityManager entityManager_pp = entityMangerFactory_pp.createEntityManager();
            PredictionParametersModel pp = new PredictionParametersModel(trainingSet, testingSet, predictionWindow, new Date());
            entityManager_pp.getTransaction().begin();
            entityManager_pp.persist(pp);
            entityManager_pp.getTransaction().commit();
            entityManager_pp.close();
            entityMangerFactory_pp.close();
        }
        System.out.println("zle");
    }

    @GetMapping("getPrediction")
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
                    newPrediction();
                }
                if (currencyDBModel.getPrice().compareTo(new BigDecimal(0)) <= 0) {
                    newPrediction();
                } else {
                    break;
                }
            } catch (NullPointerException e) {
                newPrediction();
            } catch (NullArgumentException e) {
                newPrediction();
            }
        } while(true);

        return new Money(currencyDBModel.getCurrency(), currencyDBModel.getPrice());
    }

//    @Async
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value="chartData", produces = MediaType.APPLICATION_JSON_VALUE)//pobiera dane do wykresu
    public ArrayList<ChartDataJson> getChartData() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateParameter = new Date();

        EntityManagerFactory predictionsEntityManagerFactory = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager predictionEntityManager = predictionsEntityManagerFactory.createEntityManager();
        TypedQuery<PredictionCurrencyDBModel> predictionQuery = predictionEntityManager.createQuery("SELECT p from PredictionCurrencyDBModel p where date < :dateParameter order by date asc", PredictionCurrencyDBModel.class).setParameter("dateParameter", new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(dateParameter)));
        ArrayList<PredictionCurrencyDBModel> predictions = new ArrayList<>(predictionQuery.getResultList());

        EntityManagerFactory currencyEntityManagerFactory = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager currencyEntityManager = currencyEntityManagerFactory.createEntityManager();
        TypedQuery<HistoryBitcoinDBModel> currencyQuery = currencyEntityManager.createQuery("SELECT p from HistoryBitcoinDBModel p where date < :dateParameter order by date DESC", HistoryBitcoinDBModel.class)
                .setParameter("dateParameter", new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(dateParameter)))
                .setMaxResults(predictions.size());
        ArrayList<HistoryBitcoinDBModel> currency = new ArrayList<>(currencyQuery.getResultList());

        predictionEntityManager.close();
        predictionsEntityManagerFactory.close();
        currencyEntityManager.close();
        currencyEntityManagerFactory.close();
        System.out.println("Predykcje: \n");
        for(PredictionCurrencyDBModel f : predictions){
            System.out.println(f.toString());
        }
        System.out.println("Historyczne: \n");
        for(HistoryBitcoinDBModel f : currency){
            System.out.println(f.toString());
        }
        return ChartData.jsonParser(predictions, currency);
    }

    @GetMapping(value="getBotData", produces = MediaType.APPLICATION_JSON_VALUE)
    public BotJson getBotJson(){
        BotJson botJson = new BotJson();
        EntityManagerFactory emf_bitcoin_history = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_bitcoin_history = emf_bitcoin_history.createEntityManager();

        HistoryBitcoinDBModel historyBitcoinDBModel = em_bitcoin_history.createQuery("select bit from HistoryBitcoinDBModel bit order by date desc", HistoryBitcoinDBModel.class)
                .setMaxResults(1).getSingleResult();

        EntityManagerFactory emf_prediction = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager em_bitcoin_prediction = emf_bitcoin_history.createEntityManager();
        PredictionCurrencyDBModel currencyDBModel = em_bitcoin_prediction.createQuery("select pred from PredictionCurrencyDBModel pred where date = :dateParameter", PredictionCurrencyDBModel.class)
                .setParameter("dateParameter", historyBitcoinDBModel.getDate()).setMaxResults(1).getSingleResult();

        em_bitcoin_history.close();
        em_bitcoin_prediction.close();
        emf_bitcoin_history.close();
        emf_prediction.close();

        botJson.setOstatniKurs(historyBitcoinDBModel.getPrice());
        botJson.setOstatniePrzewidywania(currencyDBModel.getPrice());
        return botJson;
    }

}

