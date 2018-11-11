package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import uk.ac.cam.cl.mlrd.interfaces.IExercise4;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;
import java.lang.Object;

public class Exercise4 implements IExercise4 {
    // * Modify the simple classifier from Exercise1 to include the information about the magnitude of a sentiment.
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException{
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
                        pos ++;
                        if (lexiconMap.get(currentWord).get(0).equals("strong")) {
                            pos ++;
                        }
                    } else if ((lexiconMap.get(currentWord).get(1).equals("negative"))) {
                        neg ++;
                        if (lexiconMap.get(currentWord).get(0).equals("strong")){
                            neg ++;
                        }
                    }
                }
            }

            if (neg <= pos) result.put(p, Sentiment.POSITIVE); else result.put(p, Sentiment.NEGATIVE);
        }

        return result;
    }

    public BigInteger binomial(int N, int K) {
        BigInteger ret = BigInteger.ONE;
        for (int k = 0; k < K; k++) {
            ret = ret.multiply(BigInteger.valueOf(N-k))
                    .divide(BigInteger.valueOf(k+1));
        }
        return ret;
    }

    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
                           Map<Path, Sentiment> classificationB){
        // Count all cases when system 1 is better than system 2, when system 2 is better
        //than system 1, and when they are the same. Call these numbers Plus, Minus and Null
        double Plus = 0;
        double Minus = 0;
        double Null = 0;
        for (Path p :actualSentiments.keySet()){
            Sentiment correct = actualSentiments.get(p);
            // if A = B = correct
            if ((classificationA.get(p) == correct) && (classificationB.get(p) == correct)){
                Null ++;
            } else if (classificationA.get(p) == correct){
                Plus ++;
            } else if (classificationB.get(p) == correct){
                Minus ++;
            }
        }

        double n = 2*(Math.ceil(Null/2)) + Plus + Minus;
        double k = (Null/2) + Math.min(Plus, Minus);
        double q = 0.5;

        BigDecimal ans = new BigDecimal(0.0);
        for (int i = 0; i<=k; i++){
            BigDecimal add = new BigDecimal(binomial((int) n, i));

            add = add.multiply(new BigDecimal(Math.pow(q, i)));
            add = add.multiply(new BigDecimal(Math.pow((1-q), n - i)));
            ans = ans.add(add);
        }
        ans = ans.multiply(new BigDecimal(2));

        // convert to a double
        return ans.doubleValue();
    }

}
