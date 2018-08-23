package Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.Date;

@Entity
@Table(name="prediction_parameters")
public class PredictionParametersModel {

    @Id
    private int id;

    private int train_size;

    private int test_size;

    private int window_size;

    private Date updated_at;

    public PredictionParametersModel() {
    }

    public PredictionParametersModel(int train_size, int test_size, int window_size, Date updated_at) {
        this.train_size = train_size;
        this.test_size = test_size;
        this.window_size = window_size;
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "PredictionParametersModel{" +
                "id=" + id +
                ", train_size=" + train_size +
                ", test_size=" + test_size +
                ", window_size=" + window_size +
                ", updated_at=" + updated_at +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTrain_size() {
        return train_size;
    }

    public void setTrain_size(int train_size) {
        this.train_size = train_size;
    }

    public int getTest_size() {
        return test_size;
    }

    public void setTest_size(int test_size) {
        this.test_size = test_size;
    }

    public int getWindow_size() {
        return window_size;
    }

    public void setWindow_size(int window_size) {
        this.window_size = window_size;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
