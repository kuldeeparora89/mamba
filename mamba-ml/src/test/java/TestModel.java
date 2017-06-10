import org.kd.app.mamba.ml.RecommendationModel;

/**
 * Created by kuldeep on 30-05-2017.
 */
public class TestModel {


    public static void main(String[] args) {


        System.setProperty("env","dev");

        RecommendationModel rm = new RecommendationModel();

        rm.build();


    }

}
