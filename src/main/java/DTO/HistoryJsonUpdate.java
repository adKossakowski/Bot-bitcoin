package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryJsonUpdate {

    @JsonProperty("unit")
    private String currency;

    @JsonProperty("values")
    private ArrayList<JsonModelBitcoin> jsonModelArrayList;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ArrayList<JsonModelBitcoin> getJsonModelArrayList() {
        return jsonModelArrayList;
    }

    public void setJsonModelArrayList(ArrayList<JsonModelBitcoin> jsonModelArrayList) {
        this.jsonModelArrayList = jsonModelArrayList;
    }

    @Override
    public String toString() {
        return "HistoryJsonUpdate{" +
                "czestosc='" + currency + '\'' +
                ", jsonModelArrayList=" + jsonModelArrayList +
                '}';
    }
}
