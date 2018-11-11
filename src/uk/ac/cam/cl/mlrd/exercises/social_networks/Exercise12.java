package uk.ac.cam.cl.mlrd.exercises.social_networks;

import uk.ac.cam.cl.mlrd.interfaces.IExercise12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.SyncFailedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class Exercise12 implements IExercise12 {
    private Map<Integer, Boolean> discovered;
    private Stack<Integer> toExplore;

    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph){
        int total = 0;
        for (int source: graph.keySet()){
            for (int target: graph.get(source)) total ++;
        }
        return total/2;
    }

    public int getUndiscoveredNode(Map<Integer, Set<Integer>> graph){
        for (int node: graph.keySet()){
            if (discovered.get(node) == false) return node;
        }
        return -1;
    }

    public Set<Integer> DFS(Map<Integer, Set<Integer>> graph, int start){
        Set<Integer> comp = new HashSet<>();

        toExplore.push(start);
        comp.add(start);

        while (!toExplore.empty()){
            int v = toExplore.pop();
            for (int w: graph.get(v)){
                if (discovered.get(w) == false){
                    toExplore.push(w);
                    comp.add(w);
                    discovered.put(w, true);
                }
            }
        }
        return comp;
    }

    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph){
        List<Set<Integer>> components = new ArrayList<>();

        toExplore = new Stack<>();
        discovered = new HashMap<>();

        for (int v: graph.keySet()){
            discovered.put(v, false);
        }

        while (getUndiscoveredNode(graph) != -1){
            int start = getUndiscoveredNode(graph);
            discovered.put(start, true);
            components.add(DFS(graph, start));
        }

        return components;
    }

    public Map<Integer, Set<Integer>> getBiggestBetweenness(Map<Integer, Map<Integer, Double>> edgeBetweenness){
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        double max = Double.NEGATIVE_INFINITY;

        for (int v: edgeBetweenness.keySet()){
            Map<Integer, Double> vw = edgeBetweenness.get(v);
            for (int w: vw.keySet()){
                if (vw.get(w) > max){
                    max = vw.get(w);
                }
            }
        }


        for (int v: edgeBetweenness.keySet()) {
            Map<Integer, Double> vw = edgeBetweenness.get(v);
            for (int w : vw.keySet()) {
                if ((vw.get(w) == max)) { // plus minus epsilon
                    if (edges.containsKey(v)){
                        edges.get(v).add(w);
                    } else {
                        edges.put(v, new HashSet<>());
                        edges.get(v).add(w);
                    }
                }
            }
        }

        return edges;
    }

    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph){
        // ignore last statement in pseudocode

        Map<Integer, Double> inBetween = new HashMap<>();
        Map<Integer, Map<Integer, Double>> edgeBetweenness = new HashMap<>();

        Stack<Integer> S = new Stack<>();
        List<Integer> Q = new ArrayList<>();

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, List<Integer>> pred = new HashMap<>();
        Map<Integer, Double> sigma = new HashMap<>();
        Map<Integer, Double> delta = new HashMap<>();

        for (int v: graph.keySet()){
            inBetween = new HashMap<>();
            for (int w: graph.keySet()){
                inBetween.put(w, 0.0);
            }
            edgeBetweenness.put(v, inBetween);

            delta.put(v, 0.0);
        }



        for (int s: graph.keySet()){
            // single source shortest path problem
            // initialisation
            //initialize
            pred.clear();
            dist.clear();
            sigma.clear();

            for (int w: graph.keySet()){
                pred.put(w, new ArrayList<>());
                dist.put(w, Double.MAX_VALUE);
                sigma.put(w, 0.0);
            }
            sigma.put(s, 1.0);
            dist.put(s, 0.0);
            Q.add(s);

            while (!Q.isEmpty()){
                int v = Q.remove(0);
                S.push(v);
                for (int w: graph.get(v)){
                    if (dist.get(w).equals(Double.MAX_VALUE)){
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
                    double c1 = (double) (((double) sigma.get(v))/ ((double)sigma.get(w)));
                    double c2 =  (1.0 + delta.get(w));
                    double c = c1 * c2;

                    edgeBetweenness.get(v).put(w, edgeBetweenness.get(v).get(w) + c);

                    delta.put(v, delta.get(v) + c);
                }
            }
        }

        return edgeBetweenness;
    }

    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents){

        while ((getComponents(graph).size() < minimumComponents)){
            Map<Integer, Map<Integer, Double>> edgeBetweenness = getEdgeBetweenness(graph);
            Map<Integer, Set<Integer>> edgeToRemove = getBiggestBetweenness(edgeBetweenness);

            for (int v: edgeToRemove.keySet()){
                graph.get(v).removeAll(edgeToRemove.get(v));
            }

        }
        return getComponents(graph);
    }

}
