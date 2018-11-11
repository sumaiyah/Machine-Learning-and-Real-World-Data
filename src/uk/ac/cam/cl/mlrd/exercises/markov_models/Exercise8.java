package uk.ac.cam.cl.mlrd.exercises.markov_models;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.interfaces.IExercise7;
import uk.ac.cam.cl.mlrd.interfaces.IExercise8;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Exercise8 implements IExercise8 {
    public static final int numFold = 10;

    public List<List<Path>>  splitCVRandom(List<Path> dataSet, int seed){
        List<List<Path>> splitData = new ArrayList();

        // Dont want to change original dataset
        List<Path> dataSetCopy = new ArrayList<>();
        dataSetCopy.addAll(dataSet);

        Random rnd = new Random();
        int foldSize = dataSet.size()/numFold;
        rnd.setSeed(seed);

        int index = 0;
        for (int foldCount = 0; foldCount < numFold; foldCount++){
            List<Path> foldX = new ArrayList<>();
            for (int foldSizeCount = 0; foldSizeCount < foldSize; foldSizeCount++){
                foldX.add(dataSetCopy.get(index));
                index++;
            }
            splitData.add(foldX);
        }
        return splitData;
    }

    public double[] crossValidate(List<List<Path>> folds) throws IOException{
        double[] results = new double[numFold];

        for (int count = 0; count < numFold; count++){
            List<Path> trainingSet = new ArrayList<>();
            List<Path> testSet = new ArrayList<>();
            for (List<Path> l: folds){
                testSet = folds.get(count);
                if (!(folds.get(count) == l)) trainingSet.addAll(l);
            }
            IExercise7 implementation7 = (IExercise7) new Exercise7();
            HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(trainingSet);

            results[count] = 1.0;
        }
        return results;
    }

    public Map<DiceType, Double> getProbs(int t, DiceType current, Set<DiceType> previousStates, HiddenMarkovModel<DiceRoll, DiceType> model,
                             List<DiceRoll> observedSequence, List<Map<DiceType, Double>> delta){
        Map<DiceType, Double> probs = new HashMap<>();

        double transition;
        double omitBF;
        double prevDelta;

        for (DiceType d: previousStates){
            transition = model.getTransitions(d).get(current);
            omitBF = model.getEmissions(current).get(observedSequence.get(t));
            prevDelta = delta.get(t-1).get(d); // already log
            probs.put(d, Math.log(transition*omitBF) + prevDelta);
        }
        return probs;
    }

    public DiceType getPrev(Map<DiceType, Double> probs){
        double max = -Integer.MAX_VALUE;
        DiceType maxDice = null;
        for (DiceType d: probs.keySet()){
            if (probs.get(d) > max){
                max = probs.get(d);
                maxDice = d;
            }
        }
        return maxDice;
    }

    // observed seq starts with S and ends with E
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence){
        List<Map<DiceType, DiceType>> psi = new ArrayList<>();
        List<Map<DiceType, Double>> delta = new ArrayList<>();
        List<DiceType> hiddenSeq = new ArrayList<>();

        Map<DiceType, Double> deltaMap = new HashMap<>();
        Map<DiceType, DiceType> psiMap = new HashMap<>();

        Set<DiceType> states = new HashSet<>(model.getTransitionMatrix().keySet());
        states.remove(DiceType.START);

        int t = 0;
        deltaMap.put(DiceType.START, model.getEmissions(DiceType.START).get(observedSequence.get(t)));
        delta.add(deltaMap);
        psiMap.put(DiceType.START, DiceType.START);
        psi.add(psiMap);

        t++;
        deltaMap = new HashMap<>();
        psiMap = new HashMap<>();
        states.add(DiceType.END);
        for (DiceType d: states){
            double transProbAiF =  model.getTransitions(DiceType.START).get(d);
            double emitProbBF = model.getEmissions(d).get(observedSequence.get(t));
            double currentDelta = delta.get(t-1).get(DiceType.START);
            deltaMap.put(d, Math.log(transProbAiF*emitProbBF*currentDelta));
            psiMap.put(d, DiceType.START);
        }
        delta.add(deltaMap);
        psi.add(psiMap);

        states.add(DiceType.END);
        for (t = 2; t < observedSequence.size(); t++){
            Set<DiceType> previous = delta.get(t-1).keySet();
            deltaMap = new HashMap<>();
            psiMap = new HashMap<>();
            for (DiceType current: states){
                if (model.getEmissionMatrix().get(current).get(observedSequence.get(t)) > 0){
                    Map<DiceType, Double> probs = getProbs(t,current,previous ,model,observedSequence,delta);
                    double maxProb = Collections.max(probs.values());
                    deltaMap.put(current, maxProb);
                    DiceType prev = getPrev(probs);
                    psiMap.put(current, prev);
                }
            }
            delta.add(deltaMap);
            psi.add(psiMap);
        }

        // gives you a reversed list
        DiceType current = DiceType.END;
        for (int i = 0; i < observedSequence.size(); i++){
            hiddenSeq.add(current);
            current = psi.get(observedSequence.size()-1-i).get(current);
        }
        Collections.reverse(hiddenSeq);
        return hiddenSeq;
    }

    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model,
                                                          List<Path> testFiles) throws IOException{
        Map<List<DiceType>, List<DiceType>> result = new HashMap<>();

        for (Path p: testFiles){
            HMMDataStore<DiceRoll, DiceType> file = HMMDataStore.loadDiceFile(p);
            List<DiceRoll> observedSequence = file.observedSequence;
            List<DiceType> trueList = file.hiddenSequence;
            List<DiceType> predicted = viterbi(model, observedSequence);
            result.put(trueList, predicted);
        }
        return result;
    }
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap){
        //        loaded is the interesting state
        double numCorrectL = 0;
        double numPredL = 0;

        for (List<DiceType> trueList: true2PredictedMap.keySet()){
            List<DiceType> predList = true2PredictedMap.get(trueList);
            for (int index= 0; index < trueList.size(); index++){
                if ((trueList.get(index) == predList.get(index)) && (predList.get(index) == DiceType.WEIGHTED)) numCorrectL += 1;
                if (predList.get(index) == DiceType.WEIGHTED) numPredL += 1;
            }
        }
        return (numCorrectL/numPredL);
    }

    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap){
        double numCorrectL = 0;
        double numTrueL = 0;
        for (List<DiceType> trueList: true2PredictedMap.keySet()){
            List<DiceType> predList = true2PredictedMap.get(trueList);
            for (int index= 0; index < trueList.size(); index++){
                if ((trueList.get(index) == predList.get(index)) && (predList.get(index) == DiceType.WEIGHTED)) numCorrectL += 1;
                if (trueList.get(index) == DiceType.WEIGHTED) numTrueL += 1;
            }
        }
        return (numCorrectL/numTrueL);
    }

    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap){
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        return (2 * ((precision*recall)/(precision+recall)));
    }


}
