package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import uk.ac.cam.cl.mlrd.interfaces.IExercise6;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise6 implements IExercise6 {
    private static int numFold = 10;

    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException{
        double pos = 0;
        double neg = 0;
        double neu = 0;

        for (Path p : trainingSet.keySet()){
            if (trainingSet.get(p) == NuancedSentiment.POSITIVE){
                pos += 1;
            } else if (trainingSet.get(p) == NuancedSentiment.NEGATIVE){
                neg +=1;
            } else if (trainingSet.get(p) == NuancedSentiment.NEUTRAL){
                neu +=1;
            }
        }

        double posTotal = pos/(pos+neg+neu);
        double negTotal = neg/(pos+neg+neu);
        double neuTotal = neu/(pos+neg+neu);

        Map<NuancedSentiment, Double> result = new HashMap<NuancedSentiment, Double>();
        result.put(NuancedSentiment.POSITIVE, posTotal);
        result.put(NuancedSentiment.NEGATIVE, negTotal);
        result.put(NuancedSentiment.NEUTRAL, neuTotal);

        return result;
    }

    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet)
            throws IOException{
        Map<String, Map<NuancedSentiment, Double>> result = new HashMap<String, Map<NuancedSentiment, Double>>();

        double numPos = 0;
        double numNeg = 0;
        double numNeu = 0;


        for (Path p : trainingSet.keySet()){
            List<String> current = Tokenizer.tokenize(p);
            for (String word: current){
                Map<NuancedSentiment, Double> wordSent;
                boolean newWord = false;

                if (result.containsKey(word)){
                    wordSent = result.get(word);
                } else {
                    wordSent = new HashMap<NuancedSentiment, Double>();
                    wordSent.put(NuancedSentiment.POSITIVE, 1.0);
                    wordSent.put(NuancedSentiment.NEGATIVE, 1.0);
                    wordSent.put(NuancedSentiment.NEUTRAL, 1.0);
                    newWord = true;
                }

                if (trainingSet.get(p) == NuancedSentiment.POSITIVE){
                    wordSent.put(NuancedSentiment.POSITIVE, (wordSent.get(NuancedSentiment.POSITIVE) + 1.0));
                    if (!(newWord)){
                        numPos +=1;
                    } else {
                        numPos += 2;
                        numNeg += 1;
                        numNeu += 1;
                    }
                }
                else if (trainingSet.get(p) == NuancedSentiment.NEGATIVE) {
                    wordSent.put(NuancedSentiment.NEGATIVE, wordSent.get(NuancedSentiment.NEGATIVE) + 1.0);
                    if (!(newWord)){
                        numNeg +=1;
                    } else {
                        numNeg += 2;
                        numPos += 1;
                        numNeu += 1;
                    }
                } else if (trainingSet.get(p) == NuancedSentiment.NEUTRAL) {
                    wordSent.put(NuancedSentiment.NEUTRAL, wordSent.get(NuancedSentiment.NEUTRAL) + 1.0);
                    if (!(newWord)){
                        numNeu +=1;
                    } else {
                        numNeu += 2;
                        numNeg += 1;
                        numPos += 1;
                    }
                }
                result.put(word, wordSent);
            }
        }

        for (String current: result.keySet()){
            Map<NuancedSentiment, Double> currentWordMap = result.get(current);

            double posProb = currentWordMap.get(NuancedSentiment.POSITIVE) / numPos;
            double negProb = currentWordMap.get(NuancedSentiment.NEGATIVE) / numNeg;
            double neuProb = currentWordMap.get(NuancedSentiment.NEUTRAL) / numNeu;

            currentWordMap.put(NuancedSentiment.POSITIVE, Math.log(posProb));
            currentWordMap.put(NuancedSentiment.NEGATIVE, Math.log(negProb));
            currentWordMap.put(NuancedSentiment.NEUTRAL, Math.log(neuProb));

            // TODO CHECK THIS  S H I T
            result.put(current, currentWordMap);
        }

        return result;
    }

    public 	Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet,
                                                            Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities)
            throws IOException{
        Map<Path, NuancedSentiment> result = new HashMap<Path, NuancedSentiment>();

        double pos = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
        double neg = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
        double neu = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));

        double posTotal = 0;
        double negTotal = 0;
        double neuTotal = 0;

        for (Path p: testSet){
            posTotal = 0;
            negTotal = 0;
            neuTotal = 0;
            List<String> words = Tokenizer.tokenize(p);

            for (String word : words){
                if (tokenLogProbs.get(word) != null){
                    posTotal += (tokenLogProbs.get(word).get(NuancedSentiment.POSITIVE));
                    negTotal += (tokenLogProbs.get(word).get(NuancedSentiment.NEGATIVE));
                    neuTotal += (tokenLogProbs.get(word).get(NuancedSentiment.NEUTRAL));
                }
            }

            if (((pos + posTotal) > (neg + negTotal)) && ((pos + posTotal) > (neu + neuTotal))){
                result.put(p, NuancedSentiment.POSITIVE);
            } else if (((neg + negTotal) > (pos + posTotal)) && ((neg + negTotal) > (neu + neuTotal))){
                result.put(p, NuancedSentiment.NEGATIVE);
            } else {
                result.put(p, NuancedSentiment.NEUTRAL);
            }
        }

        return result;
    }

    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
                                  Map<Path, NuancedSentiment> predictedSentiments){
        int correct = 0;
        int total = trueSentiments.keySet().size();
        for (Path p: trueSentiments.keySet()){
            if (trueSentiments.get(p) == predictedSentiments.get(p)){
                correct += 1;
            }
        }
        double accuracy = (double)correct/total;
        // c/c+i
        return accuracy;
    }

    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments){
        Map<Integer, Map<Sentiment, Integer>> result = new HashMap<>();

        Iterator<Map<Integer, Sentiment>> it1;

        for (int i = 1; i <= 4; i++){
            Map <Sentiment, Integer> r1 = new HashMap<>();
            r1.put(Sentiment.POSITIVE, 0);
            r1.put(Sentiment.NEGATIVE, 0);
            it1 = predictedSentiments.iterator();
            while (it1.hasNext()){
                Map<Integer, Sentiment> current = it1.next();

                if (current.get(i) == Sentiment.POSITIVE) r1.put(Sentiment.POSITIVE, (r1.get(Sentiment.POSITIVE) + 1));
                else if (current.get(i) == Sentiment.NEGATIVE) r1.put(Sentiment.NEGATIVE, (r1.get(Sentiment.NEGATIVE) + 1));
            }
            result.put(i, r1);
        }

        return result;
    }

    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable){
        double pe = calcPE(agreementTable);
        double pa = calcPA(agreementTable);

        return ((pa - pe) / (1 - pe));
    }

    public double calcPE(Map<Integer, Map<Sentiment, Integer>> agreementTable){
        double n = 101;

        double posVal = 0;
        double negVal = 0;

        for (Integer i: agreementTable.keySet()){
            Map<Sentiment, Integer> current = agreementTable.get(i);
            for (Sentiment s: current.keySet()){
               int value = current.get(s);

               if (s.equals(Sentiment.POSITIVE)) posVal += value;
               else negVal += value;
            }
        }

        double factor = 1.0 / (n * agreementTable.size());
        posVal = posVal * factor;
        negVal = negVal * factor;

        return ((Math.pow(posVal, 2)) + (Math.pow(negVal, 2)));

    }

    public double calcPA(Map<Integer, Map<Sentiment, Integer>> agreementTable){
        double N = agreementTable.size();

        double[] pis = new double[agreementTable.size()];
        double n = 101;

        double sum;
        for (Integer i: agreementTable.keySet()){
            sum = 0;
            Map<Sentiment, Integer> current = agreementTable.get(i);
            for (Sentiment s: current.keySet()){
                double nij = agreementTable.get(i).get(s);
                sum += (nij*(nij - 1));
            }
            pis[i - 1] = 1/(n * (n - 1)) * sum;
        }

        sum = 0;
        for (double i: pis){
            sum += i;
        }
        sum = sum/N;

        return sum;
    }

    public static void main(String[] args) throws IOException {
        Map<Integer, Map<Sentiment, Integer>> table12 = new HashMap<Integer, Map<Sentiment, Integer>>();
        Map<Sentiment, Integer> a = new HashMap<>();
        a.put(Sentiment.POSITIVE, 2);
        a.put(Sentiment.NEGATIVE, 67);
        Map<Sentiment, Integer> b = new HashMap<>();
        b.put(Sentiment.POSITIVE, 1);
        b.put(Sentiment.NEGATIVE, 68);
        table12.put(2,a);
        table12.put(3,b);
        Exercise6 x = new Exercise6();
        System.out.println(x.kappa(table12));
    }
}
