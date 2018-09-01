package Controller;

import Model.BotTableDB;
import Model.HistoryBitcoinDBModel;
import Model.PredictionCurrencyDBModel;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.MathContext;
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
        System.out.println("botTableController start");
        EntityManagerFactory emf_bot_table = Persistence.createEntityManagerFactory("bot_table");
        em_bot_table = emf_bot_table.createEntityManager();
        TypedQuery<BotTableDB> predictionQuery = em_bot_table.createQuery("SELECT bt from BotTableDB bt order by date asc", BotTableDB.class);
        bot_table = new ArrayList<>(predictionQuery.getResultList());
        for(BotTableDB b : bot_table){
            System.out.println("BotTableDB " + b.toString());
        }
        if(bot_table == null ){
            System.out.println("Bot table is null");
            em_bot_table.close();
            emf_bot_table.close();
            return ;
        }
        System.out.println("Bot table is not null");
        this.updateTable();
        em_bot_table.close();
        emf_bot_table.close();
    }

    private void updateTable(){
        System.out.println("updateTable start");
        EntityManagerFactory emf_history = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_history = emf_history.createEntityManager();
        if(this.bot_table.get(bot_table.size()-1).getDate() == new java.sql.Date(new java.util.Date().getTime() - 1)){
            return ;
        } else {
            Date bot_table_date = new Date(em_bot_table.createQuery("Select d.date from BotTableDB d order by date desc", java.util.Date.class).setMaxResults(1).getSingleResult().getTime());
            Date history_date =  new Date(em_history.createQuery("Select d.date from HistoryBitcoinDBModel d order by date desc ", java.util.Date.class).setMaxResults(1).getSingleResult().getTime());
            long daydiff = history_date.getTime() - bot_table_date.getTime();
            System.out.println("daydiff " + daydiff);
//            this.updateToday((int)TimeUnit.DAYS.convert(daydiff, TimeUnit.MILLISECONDS));
            int dd = (int)TimeUnit.DAYS.convert(daydiff, TimeUnit.MILLISECONDS);
            System.out.println("dd " + dd);
            for( int i = 0; i <= dd; i++){//czy nie równe

                System.out.println("bot_table_date " + bot_table_date);
                int decision = checkLastTwo(this.addDays(bot_table_date,1));
                if(decision <= 0){
                    System.out.println("Sell");
//                    this.sell(this.addDays(bot_table_date,1));
                }else{
                    System.out.println("Buy");
//                    this.buy(this.addDays(bot_table_date,1));
                }
                bot_table_date = this.addDays(bot_table_date,1);
            }
        }



    }

    private int checkLastTwo(Date dateParameter) {
        System.out.println("checkLastTwo " + dateParameter);
        EntityManagerFactory emf_prediction = Persistence.createEntityManagerFactory("prediction_bitcoin_currency_table");
        EntityManager em_prediction = emf_prediction.createEntityManager();
        List<BigDecimal> ratesList = em_prediction.createQuery("Select price from PredictionCurrencyDBModel where date <= :dateParameter order by date DESC", BigDecimal.class)
                .setParameter("dateParameter", dateParameter).setMaxResults(2).getResultList();
        if((ratesList.size()<= 1) || (ratesList.get(0).intValue() < 0) || (ratesList.get(1).intValue() < 0)){
            try {
                System.out.println("Prediction reload");
                TMPController.create_file_with_data();
                WekaForecsterController wekaForecsterController = new WekaForecsterController();
                wekaForecsterController.forecastTimeSeries();
                ratesList = em_prediction.createQuery("Select price from PredictionCurrencyDBModel where date <= :dateParameter order by date DESC", BigDecimal.class)
                        .setParameter("dateParameter", dateParameter).setMaxResults(2).getResultList();
            }catch(Exception e){
                e.printStackTrace();
            }
            if((ratesList.size()<= 1) || (ratesList.get(0).intValue() < 0) || (ratesList.get(1).intValue() < 0)) {
                return -1;
            }
        }
        System.out.println("Prediction wynik " + ratesList.get(0).subtract(ratesList.get(1)).intValue());
        return ratesList.get(0).subtract(ratesList.get(1)).intValue();
    }

    private BigDecimal getLastHistoryRate(Date date){
        EntityManagerFactory emf_history = Persistence.createEntityManagerFactory("bitcoin_history_table");
        EntityManager em_history = emf_history.createEntityManager();
        return em_history.createQuery("Select price from HistoryBitcoinDBModel where date = :dateParameter", BigDecimal.class).setParameter("dateParameter", date).setMaxResults(1).getSingleResult();
    }

    public static Date addDays(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return new Date(c.getTimeInMillis());
    }

}
