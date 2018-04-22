package Controller;

//import org.rosuda.REngine.REXP;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;

public class R_prediction_controller {

    public static void prediction(String []args) {
        Rengine re = new Rengine(args, false, null);
        System.out.println("Rengine created, waiting for R");
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }
        re.eval("library(forecast);");
        re.eval("data<-scan('bitcoin_currency_file.txt',skip=1);");
        re.eval("datats<-data;");
        // I use auto.arima function to forecast my data for 12 periods.
        // but the period of forecast result is ten period.
        // How can I do for 12 periods of forecast ?

        re.eval("arima<-auto.arima(datats);");
        re.eval("fcast<-forecast(arima,h=365);");
        REXP fs = re.eval("summary(fcast);");
        // I want to get result of forecast and returned it at an array
        double[] forecast = fs.asDoubleArray();
        for(int i=0; i<forecast.length; i++)
            System.out.println(forecast[i]);
        re.end();
    }

}
