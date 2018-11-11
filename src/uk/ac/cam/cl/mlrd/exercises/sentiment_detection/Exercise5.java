package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import uk.ac.cam.cl.mlrd.interfaces.IExercise1;
import uk.ac.cam.cl.mlrd.interfaces.IExercise2;
import uk.ac.cam.cl.mlrd.interfaces.IExercise5;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise5 implements IExercise5 {
    public static final int numFold = 10;

    //Split the given data randomly into 10 folds.
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed){
        List<Map<Path, Sentiment>> splitData = new ArrayList<Map<Path, Sentiment>>();

        // Dont want to change original dataset
        Map<Path, Sentiment> dataSetCopy = new HashMap<Path, Sentiment>();
        for (Path p: dataSet.keySet()){
            dataSetCopy.put(p, dataSet.get(p));
        }

        Random rnd = new Random();
        int foldSize = dataSet.size()/numFold;
        rnd.setSeed(seed);

        for (int foldCount = 0; foldCount < numFold; foldCount++){
            Map<Path, Sentiment> foldX = new HashMap<Path, Sentiment>();
            for (int foldSizeCount = 0; foldSizeCount < foldSize; foldSizeCount++){
                Object[] keyArr = dataSetCopy.keySet().toArray();
                Object key = keyArr[new Random().nextInt(keyArr.length)];
                foldX.put((Path)key, dataSetCopy.get(key));
                dataSetCopy.remove(key);
            }
            splitData.add(foldX);
        }
        return splitData;
    }

    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed){
        // Dont want to change original dataset
        Map<Path, Sentiment> dataSetCopy = new HashMap<Path, Sentiment>();
        for (Path p: dataSet.keySet()){
            dataSetCopy.put(p, dataSet.get(p));
        }

        Random rnd = new Random();
        int foldSize = dataSet.size()/numFold;
        rnd.setSeed(seed);

        int numPos = 0;
        int numNeg = 0;

        // numPos/numNeg hold total pos/neg
        for (Path p: dataSet.keySet()){
            if (dataSet.get(p) == Sentiment.POSITIVE) numPos++;
            else if (dataSet.get(p) == Sentiment.NEGATIVE) numNeg++;
        }

        // num of pos and neg in each fold
        numPos = numPos/numFold;
        numNeg = numNeg/numFold;


        List<Map<Path, Sentiment>> splitData = new ArrayList<Map<Path, Sentiment>>();

        //Object[] keyArr = dataSet.keySet().toArray();
        //Object key = keyArr[new Random().nextInt(keyArr.length)];
        //Object key = keyArr[new Random().nextInt(keyArr.length)];

        for (int foldCount = 0; foldCount <10; foldCount++){
            Map<Path, Sentiment> foldX = new HashMap<Path, Sentiment>();
            int posCount = 0;
            int negCount = 0;
            boolean found;
            for (int foldSizeCount = 0; foldSizeCount < foldSize; foldSizeCount++){
                Object[] keyArr = dataSetCopy.keySet().toArray();
                Object key = keyArr[new Random().nextInt(keyArr.length)];


                if (dataSet.get(key) == Sentiment.POSITIVE){
                    if (posCount < numPos){
                        posCount++;
                        foldX.put((Path)key, dataSetCopy.get(key));
                    } else {
                        found = false;
                        while (found == false){
                            key = keyArr[new Random().nextInt(keyArr.length)];
                            if (dataSetCopy.get(key) == Sentiment.NEGATIVE){
                                found = true;
                                negCount ++;
                                foldX.put((Path)key, dataSetCopy.get(key));
                            }
                        }
                    }
                } else {
                    if (negCount < numNeg){
                        negCount++;
                        foldX.put((Path)key, dataSetCopy.get(key));
                    } else {
                        found = false;
                        while (found == false){
                            key = keyArr[new Random().nextInt(keyArr.length)];
                            if (dataSetCopy.get(key) == Sentiment.POSITIVE){
                                found = true;
                                posCount ++;
                                foldX.put((Path)key, dataSetCopy.get(key));
                            }
                        }
                    }
                }

                dataSetCopy.remove(key);
            }
            splitData.add(foldX);
        }

        return splitData;
    }

    public double cvAccuracy(double[] scores){
        double result = 0;
        for (double r : scores) result += r;
        return (result/numFold);
    }

    public double cvVariance(double[] scores){
        double result = 0;
        for (double r : scores) result += (Math.pow((r - cvAccuracy(scores)), 2));
        return (result/numFold);
    }

    //Run cross-validation on the dataset according to the folds.
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException{
        // run naive bayes using 9 sets and tset with 10th
        IExercise2 implementation = (IExercise2) new Exercise2();
        Map<Sentiment, Double> classProbabilities;
        Map<String, Map<Sentiment, Double>> smoothedLogProbs;
        Map<Path, Sentiment> smoothedNBPredictions;

        double NBAccuracy;
        double[] results = new double[numFold];

        for (int count = 0; count < numFold; count++){
            Map<Path, Sentiment> trainingSet = new HashMap<Path, Sentiment>();
            Map<Path, Sentiment> testSet = new HashMap<Path, Sentiment>();
            for (Map m: folds){
                testSet = folds.get(count);
                if (!(folds.get(count) == m)) trainingSet.putAll(m);
            }

            classProbabilities = implementation.calculateClassProbabilities(trainingSet);
            smoothedLogProbs = implementation.calculateSmoothedLogProbs(trainingSet);
            smoothedNBPredictions = implementation.naiveBayes(testSet.keySet(), smoothedLogProbs, classProbabilities);

            IExercise1 implementation1 = (IExercise1) new Exercise1();
            NBAccuracy = implementation1.calculateAccuracy(testSet, smoothedNBPredictions);
            results[count] = NBAccuracy;
        }
        return results;
    }

    public double signTestImp(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
                              Map<Path, Sentiment> classificationB){
        Exercise4 signTester = (Exercise4) new Exercise4();
        return signTester.signTest(actualSentiments, classificationA, classificationB);
    }
}
