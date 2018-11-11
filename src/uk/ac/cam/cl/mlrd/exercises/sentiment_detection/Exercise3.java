package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import javafx.scene.chart.Chart;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.ChartPlotter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Scanner;

public class Exercise3 {
    static final Path dataDirectory = Paths.get("data/large_dataset");

    public static Map<String, Integer> countFreq (List<Path> dataSet) throws IOException{
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

        for (Path p: dataSet) {
            for (String word : (Tokenizer.tokenize(p))){
                if (wordCount.containsKey(word)){
                    wordCount.put(word, (wordCount.get(word) + 1));
                } else {
                    wordCount.put(word, 1);
                }
            }
        }
        return wordCount;
    }

    // Split dataset into list of paths
    private static List<Path> loadReviews(Path reviewsDir) throws IOException {
        List<Path> reviewList = new LinkedList<Path>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(reviewsDir)){
            for (Path item: files){
                reviewList.add(item);
            }
        } catch (IOException e){
            throw new IOException("Can't read the data.", e);
        }
        return reviewList;
    }

    private static void freqRankGraph(Map<String, Integer> wordCount){
        Map<String, Integer> sortedMap = sortByValue(wordCount);
    }

    private static void Heapsfhesdf(Path reviewsDir) throws IOException{


        try (DirectoryStream<Path> files = Files.newDirectoryStream(reviewsDir)){
            HashSet<String> foundTokens = new HashSet<>();
            List<BestFit.Point> powers = new ArrayList<>();

            int countn = 0;
            int countTotal = 0;

            for (Path item: files){
                for (String w: (Tokenizer.tokenize(item))){
                    countTotal ++;
                    if(!foundTokens.contains(w)){
                        countn ++;
                        foundTokens.add(w);
                        if ((countn & (countn - 1)) == 0){
                            powers.add(new BestFit.Point(Math.log(countn), Math.log(countTotal)));
                        }
                    }
                }
            }
            System.out.println("Count total: "+ countTotal);
            ChartPlotter.plotLines(powers);
        } catch (IOException e){
            throw new IOException("Can't read the data.", e);
        }
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {
        // https://www.mkyong.com/java/how-to-sort-a-map-in-java/

        // 1. Convert Map to List of Map
        List<Entry<String, Integer>> list =
                new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                               Entry<String, Integer> o2) {
                return (o2.getValue()) - (o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private static void DrawGraph(){
        ChartPlotter.plotLines();
    }

    private static int getFreq (Map<String, Integer> wordFreq, String word){
        if (wordFreq.get(word) != null){
            return wordFreq.get(word);
        }
        return 0;
    }

    private static void getEstFreq(BestFit.Line l,  int rank, List<Integer> tenWordRanks, List<Integer> tenWords, String[] wordList){
        // -----------------------------------------------------------------
        double estFreq = Math.exp((l.gradient * Math.log(rank)) + l.yIntercept);
        System.out.println("A word of rank: " + rank + " will have an est freq of: " + estFreq);
        // ----------------------------------------------------

        System.out.println("Diff in frequency ");
        // use y = mx + c to work out estimated ranks of words
        for (int count=0; count<10; count++){
            estFreq = Math.exp((l.gradient * Math.log(tenWordRanks.get(count))) + l.yIntercept);
            double diff = tenWords.get(count) - estFreq;
            System.out.println(wordList[count] + ": " + Math.abs(diff));
        }

        double k = Math.exp(l.yIntercept);
        double alpha = (0 - (l.gradient));

        System.out.println("K = " + k + " alpha: " + alpha);
    }

    private static List<BestFit.Point> logGraph (List<Integer> first10000){
        List<BestFit.Point> logPoints = new ArrayList<>();
        double count2 = 1.0;
        for (Integer word: first10000){
            logPoints.add(new BestFit.Point(Math.log(count2), Math.log(word)));
            count2 ++;
        }
        return logPoints;


        // Plot log graph
        //ChartPlotter.plotLines(logPoints);
    }

    private static BestFit.Line returnLine(List<Integer> first10000){
        double count2 = 1.0;
        Map<BestFit.Point, Double> logGraph = new HashMap<>();
        for (Integer word: first10000){
            logGraph.put(new BestFit.Point(Math.log(count2), Math.log(word)), (double)word);
            count2++;
        }
        BestFit.Line l = BestFit.leastSquares(logGraph);
        return l;
    }

    private static List<BestFit.Point> logBestFit(List<Integer> first10000){
        double count2 = 1.0;
        Map<BestFit.Point, Double> logGraph = new HashMap<>();
        for (Integer word: first10000){
            logGraph.put(new BestFit.Point(Math.log(count2), Math.log(word)), (double)word);
            count2++;
        }
        BestFit.Line l = BestFit.leastSquares(logGraph);

        List<BestFit.Point> bestFitPoints = new ArrayList<>();
        for (double x=0; x<11; x++){
            bestFitPoints.add(new BestFit.Point(x, (l.gradient * x)+l.yIntercept));
        }
        return bestFitPoints;
    }

    public static void main(String[] args) throws IOException{
        Path reviewsDir = dataDirectory.resolve("large_dataset");
        List<Path> reviewList = loadReviews(reviewsDir);
        Map<String, Integer> wordCount = countFreq(reviewList);
        Map<String, Integer> sortedWords = (sortByValue(wordCount));
        List<Integer> first10000 = new ArrayList<Integer>();

        String[] wordList = {"annoyed", "astonishing", "exciting", "terrible", "great", "sad", "tasteful", "adventurous", "amazing", "enjoyable"};
        List<Integer> tenWords = new ArrayList<Integer>();

        // points is first 10000 on graph
        List<BestFit.Point> points = new ArrayList<>();

        // points 2 is set of points for 10 words i chose
        List<BestFit.Point> points2 = new ArrayList<>();

        List<Integer> tenWordRanks = new ArrayList<>();

        // get first 10000 words
        int count = 0;
        for (String s: sortedWords.keySet()){
            if (count == 10000) break;
            first10000.add(sortedWords.get(s));
            points.add(new BestFit.Point(count, (double)sortedWords.get(s)));
            count ++;

            // Adding my 10 words to the list with correct rank
            for (String t: wordList){
                if (s.equals(t)){
                    // Adding first 10000 words to graph
                    points2.add(new BestFit.Point(count, getFreq(sortedWords, t)));
                    tenWordRanks.add(count);
                }
                tenWords.add(getFreq(sortedWords, t));
            }
        }

        // Plot 2 graphs
        ChartPlotter.plotLines(points, points2);


        // gets estimated frequency of a word at rank 5, and prints estimated freq of 10 chosen words
        getEstFreq(returnLine(first10000), 5, tenWordRanks, tenWords, wordList);

        // print log graph with line of best fit
        ChartPlotter.plotLines(logGraph(first10000), logBestFit(first10000));

        // work out k and a
        Heapsfhesdf(reviewsDir);
    }
}
