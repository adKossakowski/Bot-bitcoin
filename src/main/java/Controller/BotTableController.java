package Controller;

import Model.BotTableDB;
import Model.HistoryBitcoinDBModel;
import Model.PredictionCurrencyDBModel;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BotTableController {

    ArrayList<BotTableDB> bot_table;
    EntityManager em_bot_table;

    public void botTableController(){
        EntityManagerFactory emf_bot_table = Persistence.createEntityManagerFactory("bot_table");
        em_bot_table = emf_bot_table.createEntityManager();
        TypedQuery<BotTableDB> predictionQuery = em_bot_table.createQuery("SELECT bt from BotTableDB bt order by date asc", BotTableDB.class);
        bot_table = new ArrayList<>(predictionQuery.getResultList());
        if(bot_table == null ){
            em_bot_table.close();
            emf_bot_table.close();
            return ;
        }
        this.updateTable();
        em_bot_table.close();
        emf_bot_table.close();
    }

    private void updateTable(){
        EntityManagerFactory emf_history = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_history = emf_history.createEntityManager();
        if(this.bot_table.get(bot_table.size()-1).getDate() == new java.sql.Date(new java.util.Date().getTime() - 1)){
            return ;
        } else {
            Date bot_table_date = em_bot_table.createQuery("Select d.date from BotTableDB d order by date desc", Date.class).getSingleResult();
            Date history_date =  em_history.createQuery("Select d.date from HistoryBitcoinDBModel d order by date desc ", Date.class).getSingleResult();
            long daydiff = bot_table_date.getTime() - history_date.getTime();
//            this.updateToday((int)TimeUnit.DAYS.convert(daydiff, TimeUnit.MILLISECONDS));
            int dd = (int)TimeUnit.DAYS.convert(daydiff, TimeUnit.MILLISECONDS);
            for( int i = 0; i <= dd; i++){
                bot_table_date = this.addDays(bot_table_date,1);
                int decision = checkLastTwo(bot_table_date);
                if(decision <= 0){
                    this.sell(bot_table_date);
                }else{
                    this.buy(bot_table_date);
                }
            }
        }



    }

    private int checkLastTwo(Date dateParameter){
        EntityManagerFactory emf_prediction = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager em_prediction = emf_prediction.createEntityManager();
        List<BigDecimal> ratesList = em_prediction.createQuery("Select price from PredictionCurrencyDBModel where date <= :dateParameter oreder by price DESC", BigDecimal.class)
                .setParameter("dateParameter", dateParameter).setMaxResults(2).getResultList();
        if(ratesList.get(0).intValue() < 0 || ratesList.get(1).intValue() < 0){
            try {
                TMPController.create_file_with_data();
                WekaForecsterController wekaForecsterController = new WekaForecsterController();
                wekaForecsterController.forecastTimeSeries();
            }catch(Exception e){
                e.printStackTrace();
            }
            return -1;
        }
        return ratesList.get(0).subtract(ratesList.get(1)).intValue();
    }

    private BigDecimal getLastHistoryRate(Date date){
        EntityManagerFactory emf_history = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_history = emf_history.createEntityManager();
        return em_history.createQuery("Select price from HistoryBitcoinDBModel where date = :dateParameter", BigDecimal.class).setParameter("dateParameter", date).getSingleResult();
    }

    public static Date addDays(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return new Date(c.getTimeInMillis());
    }

    private void sell(Date date){
        BotTableDB botTableDB;
        BotTableDB tableDB = em_bot_table.createQuery("Select t from BotTableDB t order by t.date desc", BotTableDB.class).getSingleResult();
        if(tableDB.getCurrency().equals("BTC")){
           BigDecimal money = tableDB.getMoney().multiply(this.getLastHistoryRate(date));
            botTableDB = new BotTableDB(date, "USD", money, "BUY");
        }else{
            botTableDB = new BotTableDB(date, "USD", tableDB.getMoney(), "KEEP");
        }
        em_bot_table.persist(botTableDB);
    }

    private void buy(Date date){
        BotTableDB botTableDB;
        BotTableDB tableDB = em_bot_table.createQuery("Select t from BotTableDB t order by t.date desc", BotTableDB.class).getSingleResult();
        if(tableDB.getCurrency().equals("USD")){
            BigDecimal money = tableDB.getMoney().divide(this.getLastHistoryRate(date));
            botTableDB = new BotTableDB(date, "BTC", money, "SELL");
        }else{
            botTableDB = new BotTableDB(date, "BTC", tableDB.getMoney(), "KEEP");
        }
        em_bot_table.persist(botTableDB);
    }

}
