package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.cl.mlrd.interfaces.IExercise2;
import uk.ac.cam.cl.mlrd.utils.DataSplit;


public class Exercise2 implements IExercise2 {
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException{
//      Calculate the probability of a document belonging to a given class based
     //* on the training data.
        double pos = 0;
        double neg = 0;
        for (Path p : trainingSet.keySet()){
            if (trainingSet.get(p) == Sentiment.POSITIVE){
                pos += 1;
            } else {
                neg +=1;
            }
        }

        double posTotal = pos/(pos+neg);
        double negTotal = neg/(pos+neg);

        Map<Sentiment, Double> result = new HashMap<Sentiment, Double>();
        result.put(Sentiment.POSITIVE, posTotal);
        result.put(Sentiment.NEGATIVE, negTotal);

        return result;
    }

    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException{
//        For each word and sentiment present in the training set, estimate the
//                * unsmoothed log probability of a word to occur in a review with a
//     * particular sentiment.
        Map<String, Map<Sentiment, Double>> result = new HashMap<String, Map<Sentiment, Double>>();

        double numPos = 0;
        double numNeg = 0;


        for (Path p : trainingSet.keySet()){
            List<String> current = Tokenizer.tokenize(p);
            for (String word: current){
                Map<Sentiment, Double> wordSent = new HashMap<Sentiment, Double>();

                if (result.containsKey(word)){
                    wordSent = result.get(word);
                } else {
                    wordSent.put(Sentiment.POSITIVE, 0.0);
                    wordSent.put(Sentiment.NEGATIVE, 0.0);
                }

                if (trainingSet.get(p) == Sentiment.POSITIVE){
                    wordSent.put(Sentiment.POSITIVE, (wordSent.get(Sentiment.POSITIVE) + 1.0));
                    numPos +=1;
                }
                else if (trainingSet.get(p) == Sentiment.NEGATIVE) {
                    wordSent.put(Sentiment.NEGATIVE, wordSent.get(Sentiment.NEGATIVE) + 1.0);
                    numNeg += 1;
                }
                result.put(word, wordSent);
            }
        }

        for (String current: result.keySet()){
           Map<Sentiment, Double> currentWordMap = result.get(current);
           double total = currentWordMap.get(Sentiment.POSITIVE) + currentWordMap.get(Sentiment.NEGATIVE);
           double posProb = currentWordMap.get(Sentiment.POSITIVE) / numPos;
           double negProb = currentWordMap.get(Sentiment.NEGATIVE) / numNeg;

           currentWordMap.put(Sentiment.POSITIVE, Math.log(posProb));
           currentWordMap.put(Sentiment.NEGATIVE, Math.log(negProb));

           result.put(current, currentWordMap);
        }

        return result;
    }

    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException{

        Map<String, Map<Sentiment, Double>> result = new HashMap<String, Map<Sentiment, Double>>();

        double numPos = 0;
        double numNeg = 0;



        for (Path p : trainingSet.keySet()){
            List<String> current = Tokenizer.tokenize(p);
            for (String word: current){
                Map<Sentiment, Double> wordSent;
                boolean newWord = false;

                if (result.containsKey(word)){
                    wordSent = result.get(word);
                } else {
                    wordSent = new HashMap<Sentiment, Double>();
                    wordSent.put(Sentiment.POSITIVE, 1.0);
                    wordSent.put(Sentiment.NEGATIVE, 1.0);
                    newWord = true;
                }

                if (trainingSet.get(p) == Sentiment.POSITIVE){
                    wordSent.put(Sentiment.POSITIVE, (wordSent.get(Sentiment.POSITIVE) + 1.0));
                    if (!(newWord)){
                        numPos +=1;
                    } else {
                        numPos += 2;
                        numNeg += 1;
                    }
                }
                else if (trainingSet.get(p) == Sentiment.NEGATIVE) {
                    wordSent.put(Sentiment.NEGATIVE, wordSent.get(Sentiment.NEGATIVE) + 1.0);
                    if (!(newWord)){
                        numNeg +=1;
                    } else {
                        numNeg += 2;
                        numPos += 1;
                    }
                }
                result.put(word, wordSent);
            }
        }

        for (String current: result.keySet()){
            Map<Sentiment, Double> currentWordMap = result.get(current);
            double total = currentWordMap.get(Sentiment.POSITIVE) + currentWordMap.get(Sentiment.NEGATIVE);

            double posProb = currentWordMap.get(Sentiment.POSITIVE) / numPos;
            double negProb = currentWordMap.get(Sentiment.NEGATIVE) / numNeg;

            currentWordMap.put(Sentiment.POSITIVE, Math.log(posProb));
            currentWordMap.put(Sentiment.NEGATIVE, Math.log(negProb));

            result.put(current, currentWordMap);
        }

        return result;
    }

    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException{
//      Use the estimated log probabilities to predict the sentiment of each
//      review in the test set
        Map<Path, Sentiment> result = new HashMap<Path, Sentiment>();

        double pos = Math.log(classProbabilities.get(Sentiment.POSITIVE));
        double neg = Math.log(classProbabilities.get(Sentiment.NEGATIVE));

        double posTotal = 0;
        double negTotal = 0;

        for (Path p: testSet){
            posTotal = 0;
            negTotal = 0;
            List<String> words = Tokenizer.tokenize(p);

            for (String word : words){
                if (tokenLogProbs.get(word) != null){
                    posTotal += (tokenLogProbs.get(word).get(Sentiment.POSITIVE));
                    negTotal += (tokenLogProbs.get(word).get(Sentiment.NEGATIVE));
                }
            }

            if ((pos + posTotal) > (neg + negTotal)){
                result.put(p, Sentiment.POSITIVE);
            } else {
                result.put(p, Sentiment.NEGATIVE);
            }

        }

        return result;
    }
}
