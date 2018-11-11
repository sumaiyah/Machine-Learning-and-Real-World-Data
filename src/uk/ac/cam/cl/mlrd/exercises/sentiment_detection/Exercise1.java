package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import uk.ac.cam.cl.mlrd.interfaces.IExercise1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Exercise1 implements IExercise1 {
    /**
     * Read the lexicon and determine whether the sentiment of each review in
     * the test set is positive or negative based on whether there are more
     * positive or negative words.
     *
     * @param testSet
     *            {@link Set}<{@link Path}> Paths to reviews to classify
     * @param lexiconFile
     *            {@link Path} Path to the lexicon file
     * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
     *         sentiment for each review
     * @throws IOException
     */


    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException{
        Map<Path, Sentiment> result = new HashMap<Path, Sentiment>();

        // create a map
        Map<String, List<String>> lexiconMap = new HashMap<String, List<String>>();

        Scanner sc = new Scanner(lexiconFile);
        while (sc.hasNextLine()) {
            String current = sc.nextLine();
            String[] arr = current.split(" |\\=");
            List<String> polarity = new ArrayList<>();
            polarity.add(arr[3]);
            polarity.add(arr[5]);
            lexiconMap.put(arr[1], polarity);
        }

        // for each path in set
            // tokenize words for each word see if it is pos/neg
        Map<Path, List<String>> reviews = new HashMap<Path, List<String>>();
        for (Path p: testSet){
            int pos = 0;
            int neg = 0;

            // Hashmap of paths to tokenised lists

            reviews.put(p, Tokenizer.tokenize(p));

            for (String currentWord: reviews.get(p)){
                if (lexiconMap.get(currentWord) != null){
                    if (lexiconMap.get(currentWord).get(1).equals("positive") ){
                        pos += 1;
                    } else { neg += 1; }

                    if (neg <= pos) result.put(p, Sentiment.POSITIVE); else result.put(p, Sentiment.NEGATIVE);

                }
            }

        }

        return result;
    }

    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments){
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

    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException{
        Map<Path, Sentiment> result = new HashMap<Path, Sentiment>();

        // create a map
        Map<String, List<String>> lexiconMap = new HashMap<String, List<String>>();

        Scanner sc = new Scanner(lexiconFile);
        while (sc.hasNextLine()) {
            String current = sc.nextLine();
            String[] arr = current.split(" |\\=");
            List<String> polarity = new ArrayList<>();
            polarity.add(arr[3]);
            polarity.add(arr[5]);
            lexiconMap.put(arr[1], polarity);
        }

        // for each path in set
        // tokenize words for each word see if it is pos/neg
        Map<Path, List<String>> reviews = new HashMap<Path, List<String>>();
        for (Path p: testSet){
            int pos = 0;
            int neg = 0;

            // Hashmap of paths to tokenised lists

            reviews.put(p, Tokenizer.tokenize(p));


            for (String currentWord: reviews.get(p)){
                if (lexiconMap.get(currentWord) != null){
                    if (lexiconMap.get(currentWord).get(1).equals("positive")){
                        if (lexiconMap.get(currentWord).get(0).equals("strong")) {
                            pos += 1;
                        } //else { pos += 1; }
                    } else if ((lexiconMap.get(currentWord).get(1).equals("negative"))) {
                        if (lexiconMap.get(currentWord).get(0).equals("strong")){
                            neg += 1;
                        } //else { neg += 1; }
                    }
                }
            }

            if (neg < pos) result.put(p, Sentiment.POSITIVE); else result.put(p, Sentiment.NEGATIVE);



        }

        return result;
    }


}
