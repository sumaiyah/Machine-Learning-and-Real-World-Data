package uk.ac.cam.cl.mlrd.exercises.social_networks;

import edu.stanford.nlp.util.ArrayUtils;
import uk.ac.cam.cl.mlrd.interfaces.IExercise10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;


public class Exercise10 implements IExercise10 {

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
        System.out.println(loadedGraph);

        return loadedGraph;
    }

    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph){
        Map<Integer, Integer> connectives = new HashMap<>();
        for (int i: graph.keySet()){
            connectives.put(i, graph.get(i).size());
        }
        return connectives;
    }

    public int getLeastConnectedNode(Map<Integer, Set<Integer>> graph){
        int source = graph.keySet().iterator().next();
        for (int node: graph.keySet()){
            if (graph.get(node).size() < graph.get(source).size()) source = node;
        }
        return source;
    }

    private int bfs(Map<Integer, Set<Integer>> graph, int source){
        List<Integer> BFS_queue = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        BFS_queue.add(source);
        int diameter = 0;

        int currentNeighbours = BFS_queue.size(); // 1
        int nextNeighbours = 0;

        while (BFS_queue.size() > 0){
            int currentNode = BFS_queue.remove(0);
            currentNeighbours --;
            visited.add(currentNode);
            Set<Integer> neighbours = graph.get(currentNode);
            //if (neighbours == null) neighbours = new HashSet<Integer>();
            for (int neighbour: neighbours){
                if (!visited.contains(neighbour)){
                    BFS_queue.add(neighbour);
                    nextNeighbours ++;
                }
            }
            if (currentNeighbours == 0){
                currentNeighbours = nextNeighbours;
                nextNeighbours = 0;
                diameter ++;
            }
        }

        return diameter;
    }

    public int getDiameter(Map<Integer, Set<Integer>> graph){
        int source = getLeastConnectedNode(graph);
        return bfs(graph, source);
    }

//    private int bfs2(Map<Integer, Set<Integer>> graph, int source){
////        LinkedList<Integer> BFS_queue = new LinkedList<Integer>();
////        Set<Integer> visited = new HashSet<>();
////        BFS_queue.add(source);
////        int diameter = 0;
////
////        while (BFS_queue.size() > 0){
////            int currentNode = BFS_queue.remove(0);
////            visited.add(currentNode);
////            for (int neighbour: graph.get(currentNode)){
////                if (!visited.contains(neighbour)){
////                    BFS_queue.add(neighbour);
////                }
////            }
////        }
//        LinkedList<Integer> BFS_queue = new LinkedList<Integer>();
//        int numNodes = graph.keySet().size();
//        boolean [] visited = new boolean[numNodes];
//        int [] bfs_distances = new int[numNodes];
//
//        // distances all unknown
//        for( int i = 0; i < numNodes; i++ )
//            bfs_distances[i] = -1;
//
//        int startNode = graph.keySet().iterator().next();
//        startNode = getLeastConnectedNode(graph);
//        BFS_queue.add(startNode);
//        visited[startNode] = true;
//        bfs_distances[startNode] = 0;
//
//        while( !BFS_queue.isEmpty() ) {
//            int currentNode = BFS_queue.remove();
//            for (Integer adjacentNode: graph.get(currentNode)){
//                if (!visited[adjacentNode]){
//                    BFS_queue.add(adjacentNode);
//                    visited[adjacentNode] = true;
//                    bfs_distances[adjacentNode] = bfs_distances[currentNode] + 1;
//                }
//                //bfs_distances[adjacentNode] = bfs_distances[currentNode] + 1;
//            }
//        }
//
//        int max = 0;
//        for (int i: bfs_distances){
//            if (i>max) max = i;
//        }
//        return max;
//    }
}

