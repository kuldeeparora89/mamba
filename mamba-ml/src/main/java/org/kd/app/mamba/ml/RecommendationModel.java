package org.kd.app.mamba.ml;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.kd.app.mamba.ml.common.Constants;
import org.kd.app.mamba.common.Env;
import org.kd.app.mamba.common.Resources;

/**
 * Created by kuldeep on 27-05-2017.
 */
public class RecommendationModel {



    public void build(){


        Resources res = Resources.INSTANCE;

        if(Env.DEV == res.getEnv()){
            System.setProperty("hadoop.home.dir", "C:\\study\\Spark\\hadoop");
        }


        SparkSession spark = SparkSession
                .builder()
                .appName("Movie Recommender")
                .master(res.getProperty(Constants.PROPS_MASTER_URL))
                .config("spark.sql.warehouse.dir", res.getProperty(Constants.PROPS_WAREHOUSE_PATH))
                .getOrCreate();


        StringIndexer userStringIndexer = new StringIndexer();
        userStringIndexer.setInputCol("user_id").setOutputCol("numeric_user_id");
        StringIndexer businessStringIndexer = new StringIndexer();
        businessStringIndexer.setInputCol("business_id").setOutputCol("numeric_business_id");

        Pipeline ratingsPipeline = new Pipeline().setStages(new PipelineStage[]{userStringIndexer,businessStringIndexer});



        Dataset<Row> users=  spark.read().json(res.getProperty(Constants.PROP_DATASET_USER));
        Dataset<Row> business=  spark.read().json(res.getProperty(Constants.PROP_DATASET_BUSINESS));
        Dataset<Row> ratings =   spark.read().json(res.getProperty(Constants.PROP_DATASET_RATING));

       // ratings=ratings.limit(100);

        Dataset<Row> ratingsDF = ratingsPipeline.fit(ratings).transform(ratings);






        Dataset<Row>[] set = ratingsDF.randomSplit(new double[]{0.8,0.2});
        Dataset<Row> training = set[0];
        Dataset<Row> test = set[1];


        ALS als  = new ALS().setUserCol("numeric_user_id").setItemCol("numeric_business_id").setRatingCol("stars");


        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("stars").setPredictionCol("prediction");

        ParamMap[] paramGrid = new ParamGridBuilder().
                addGrid(als.rank(), new int[]{1, 5, 10}).addGrid(als.maxIter(), new int[]{5}).addGrid(als.regParam(), new double[]{0.05, 0.1, 0.5}).build();

        CrossValidator crossVal =new CrossValidator().
                setEstimator(als).
                setEstimatorParamMaps(paramGrid).
                setNumFolds(2)
                .setEvaluator(evaluator)
                ;



        CrossValidatorModel cvModel = crossVal.fit(training);
        Dataset<Row>  predictions = cvModel.transform(test);
        predictions.show();


        //System.out.println("The root mean squared error for our model is: " + evaluator.evaluate(predictions));















    }



}
