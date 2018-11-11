package uk.ac.cam.cl.mlrd.exercises.social_networks;

import uk.ac.cam.cl.mlrd.interfaces.IExercise11;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class Exercise11 implements IExercise11 {
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException{
        Map<Integer, Set<Integer>> loadedGraph = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(graphFile)) {
            reader.lines().forEach(new Consumer<String>() {
                @Override
                public void accept(String line) {
                    String[] values = line.split(" ");
                    int source = Integer.parseInt(values[0]);
                    int target = Integer.parseInt(values[1]);

                    if (!(loadedGraph.containsKey(source)))
                        loadedGraph.put(source, new HashSet<Integer>());
                    loadedGraph.get(source).add(target);

                    // undirected goes both ways
                    for (int t: loadedGraph.get(source)){
                        if (!(loadedGraph.containsKey(t)))
                            loadedGraph.put(t, new HashSet<Integer>());
                        loadedGraph.get(t).add(source);
                    }
                }
            });
        } catch (IOException e) {
            throw new IOException("Can't load the data file.", e);
        }
       // System.out.println(loadedGraph);

        return loadedGraph;
    }

    public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException{
        Map<Integer, Double> betweenness = new HashMap<>();

        Map<Integer, Set<Integer>> graph = loadGraph(graphFile);

        Stack<Integer> S = new Stack<>();
        List<Integer> Q = new ArrayList<>();

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, List<Integer>> pred = new HashMap<>();
        Map<Integer, Double> sigma = new HashMap<>();
        Map<Integer, Double> delta = new HashMap<>();

        for (int w: graph.keySet()){
            betweenness.put(w, 0.0);
        }

        for (int s: graph.keySet()){
            // single source shortest path problem
            // initialisation
            for (int w: graph.keySet()){
                pred.put(w, new ArrayList<>());
                dist.put(w, -1.0);
                sigma.put(w, 0.0);
            }
            sigma.put(s, 1.0);
            dist.put(s, 0.0);
            Q.add(s);

            while (!Q.isEmpty()){
                int v = Q.remove(0);
                S.push(v);
                for (int w: graph.get(v)){
                    if (dist.get(w) == (-1.0)){
                        dist.put(w, dist.get(v) + 1.0);
                        Q.add(w);
                    }

                    if (dist.get(w) == (dist.get(v) + 1.0)){
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        pred.get(w).add(v);
                    }
                }
            }

            // accumulation
            for (int v: graph.keySet()) delta.put(v, 0.0);

            while (!S.isEmpty()){
                int w = S.pop();
                for (int v: pred.get(w)){

                    double t1 = delta.get(v);
                    double t2 = (double) (((double) sigma.get(v))/ ((double)sigma.get(w)));
                    double t3 =  (1.0 + delta.get(w));

                    delta.put(v, (t1 + (t2 * t3)));
                }
                if (w != s) betweenness.put(w, betweenness.get(w) + delta.get(w));
            }
        }

        for (int v: betweenness.keySet()){
            betweenness.put(v, betweenness.get(v)/2);
        }

        return betweenness;
    }

//    public Map<Integer, Double> getNodeBetw0eenness(Path graphFile) throws IOException{
//        Map<Integer, Double> betweenness = new HashMap<>();
//
//        Map<Integer, Set<Integer>> graph = loadGraph(graphFile);
//        int numNodes = graph.keySet().size();
//
//        List<Integer> Q = new ArrayList<>();
//        Stack<Integer> S = new Stack<>();
//        int[] dist = new int[numNodes];
//        List<Integer>[] pred = new List[numNodes];
//        int[] sigma = new int[numNodes];
//        int[] delta = new int[numNodes];
//
//
//        for (int s: graph.keySet()){
//            // initialise
//            for (int v: graph.keySet()){
//                betweenness.put(v, 0.0);
//
//                pred[v] = new ArrayList<>();
//                dist[v] = -1;
//                sigma[v] = 0;
//            }
//
//            dist[s] = 0;
//            sigma[s] = 1;
//            Q.add(s);
//
//            while (!Q.isEmpty()){
//                int v = Q.remove(0);
//                S.push(v);
//
//                for (int w: graph.get(v)){
//                    // path discovery
//                    if (dist[w] == -1){
//                        dist[w] = dist[v] + 1;
//                        Q.add(w);
//                    }
//
//                    //path counting
//                    if (dist[w] == dist[v] + 1){
//                        sigma[w] = sigma[w] + sigma[v];
//                        pred[w].add(v);
//                    }
//                }
//            }
//
//            // accumulation
//            for (int v: graph.keySet()){
//                sigma[v] = 0;
//            }
//
//            while (!S.empty()){
//                int w = S.pop();
//                for (int v: pred[w]){
//                    if (sigma[w] == 0) delta[v] = delta[v] * (1 + delta[w]);
//                    else
//                        delta[v] = delta[v] + (sigma[v]/sigma[w]) * (1 + delta[w]);
//                }
//                if (w != s){
//                    betweenness.put(w, betweenness.get(w) + delta[w]);
//                }
//            }
//        }
//        return betweenness;
//    }

}
