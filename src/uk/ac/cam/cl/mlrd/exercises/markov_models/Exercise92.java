package uk.ac.cam.cl.mlrd.exercises.markov_models;

import uk.ac.cam.cl.mlrd.interfaces.IExercise9;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exercise92 implements IExercise9 {

    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {
        Map<Feature, Map<Feature, Double>> transitionMat = new HashMap<Feature, Map<Feature, Double>>();
        Map<Feature, Map<AminoAcid, Double>> emissionMat = new HashMap<Feature, Map<AminoAcid, Double>>();
        Map<Feature, Integer> transFrom = new HashMap<Feature, Integer>();
        Map<Feature, Map<Feature, Integer>> transTo = new HashMap<Feature, Map<Feature, Integer>>();
        Map<Feature, Integer> emissCount = new HashMap<Feature, Integer>();
        Map<Feature, Map<AminoAcid, Integer>> emissObs = new HashMap<Feature, Map<AminoAcid, Integer>>();
        List<AminoAcid> allObs = new LinkedList<AminoAcid>();
        Set<Feature> finalStates = new HashSet<Feature>();
        for (HMMDataStore<AminoAcid, Feature> sequence: sequencePairs) {
            List<AminoAcid> observations = sequence.observedSequence;
            List<Feature> states = sequence.hiddenSequence;
            allObs.addAll(observations);
            for (int i = 0; i < states.size(); i++) {
                Feature currentState = states.get(i);
                Feature nextState;
                try {
                    nextState = states.get(i + 1);
                } catch (IndexOutOfBoundsException e) {
                    nextState = null;
                }
                AminoAcid currentObservation = observations.get(i);
                if (nextState != null) {
                    if (finalStates.contains(currentState)) finalStates.remove(currentState);
                    transFrom.put(currentState, transFrom.getOrDefault(currentState, 0) + 1);
                    Map<Feature, Integer> transCount = transTo.get(currentState);
                    if (transCount == null) {
                        transCount = new HashMap<Feature, Integer>();
                        transCount.put(nextState, 1);
                    } else transCount.put(nextState, transCount.getOrDefault(nextState, 0) + 1);
                    transTo.put(currentState, transCount);
                } else finalStates.add(currentState);
                emissCount.put(currentState, emissCount.getOrDefault(currentState, 0) + 1);
                Map<AminoAcid, Integer> emissObsCount = emissObs.get(currentState);
                if (emissObsCount == null) {
                    emissObsCount = new HashMap<AminoAcid, Integer>();
                    emissObsCount.put(currentObservation, 1);
                } else emissObsCount.put(currentObservation, emissObsCount.getOrDefault(currentObservation, 0) + 1);
                emissObs.put(currentState, emissObsCount);
            }
        }
        for (Feature state: transTo.keySet()) {
            Map<Feature, Double> A = new HashMap<Feature, Double>();
            Map<Feature, Integer> transition = transTo.get(state);
            for (Feature transState: transition.keySet()) A.put(transState, (double) transition.get(transState) / transFrom.get(state));
            transitionMat.put(state, A);
        }
        for (Feature state: emissObs.keySet()) {
            Map<AminoAcid, Double> B = new HashMap<AminoAcid, Double>();
            Map<AminoAcid, Integer> emission = emissObs.get(state);
            for (AminoAcid emissState: emission.keySet()) B.put(emissState, (double) emission.get(emissState) / emissCount.get(state));
            emissionMat.put(state, B);
        }
        for (Feature state: finalStates) transitionMat.put(state, null);
        for (Feature state: transitionMat.keySet()) {
            for (Feature otherStates: transitionMat.keySet()) {
                Map<Feature, Double> A = transitionMat.get(otherStates);
                if (A == null) {
                    A = new HashMap<Feature, Double>();
                    A.put(state, 0.0);
                } else if (!A.containsKey(state)) A.put(state, 0.0);
                transitionMat.put(otherStates, A);
            }
        }
        for (AminoAcid observation: allObs) {
            for (Feature state: emissionMat.keySet()) {
                Map<AminoAcid, Double> B = emissionMat.get(state);
                if (B == null) {
                    B = new HashMap<AminoAcid, Double>();
                    B.put(observation, 0.0);
                } else if (!B.containsKey(observation)) B.put(observation, 0.0);
                emissionMat.put(state, B);
            }
        }
        return new HiddenMarkovModel<AminoAcid, Feature>(transitionMat, emissionMat);
    }

    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        int sequenceSize = observedSequence.size();
        if (sequenceSize == 0) return new LinkedList<Feature>();
        List<Feature> predictedModel = new LinkedList<Feature>();
        predictedModel.add(Feature.START);
        if (sequenceSize == 1) return predictedModel;
        Map<Feature, Map<Feature, Double>> A = new HashMap<Feature, Map<Feature, Double>>(model.getTransitionMatrix());
        Map<Feature, Map<AminoAcid, Double>> B = new HashMap<Feature, Map<AminoAcid, Double>>(model.getEmissionMatrix());
        List<Map<Feature, Feature>> previousStates = new LinkedList<Map<Feature,Feature>>();
        List<Map<Feature, Double>> stateProbability = new LinkedList<Map<Feature, Double>>();
        Set<Feature> states = new HashSet<Feature>(model.getHiddenStates());
        previousStates.add(new HashMap<Feature, Feature>());
        stateProbability.add(new HashMap<Feature, Double>());
        predictedModel.remove(0);
        predictedModel.add(Feature.END);
        states.remove(Feature.START);
        for (int t = 1; t < sequenceSize; t++) {
            Map<Feature, Feature> prevStates = new HashMap<Feature, Feature>();
            Map<Feature, Double> stateProbs = new HashMap<Feature, Double>();
            AminoAcid observation = observedSequence.get(t);
            for (Feature state: states) {
                if (B.get(state).get(observation) > 0) {
                    double maxProb = 2;
                    Feature maxState = null;
                    for (Feature prevState: states) {
                        if (prevState != Feature.START && prevState != Feature.END) {
                            if (t == 1) {
                                prevStates.put(state, Feature.START);
                                stateProbs.put(state, Math.log(A.get(Feature.START).get(state) * B.get(state).get(observation)));
                            } else {
                                double prevProb = stateProbability.get(t - 1).get(prevState);
                                double tranProb = A.get(prevState).get(state);
                                double emitProb = B.get(state).get(observation);
                                if (maxProb == 2) {
                                    maxProb = prevProb + Math.log(tranProb) + Math.log(emitProb);
                                    maxState = prevState;
                                } else if (prevProb + Math.log(tranProb) + Math.log(emitProb) > maxProb) {
                                    maxProb = prevProb + Math.log(tranProb) + Math.log(emitProb);
                                    maxState = prevState;
                                }
                            }
                        }
                    }
                    if (t > 1) {
                        prevStates.put(state, maxState);
                        stateProbs.put(state, maxProb);
                    }
                }
            }
            previousStates.add(prevStates);
            stateProbability.add(stateProbs);
        }
        for (int t = sequenceSize - 1; t > 0; t--) {
            predictedModel.add(previousStates.get(t).get(predictedModel.get(predictedModel.size() - 1)));
        }
        Collections.reverse(predictedModel);
        return predictedModel;
    }

    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> predictions = new HashMap<List<Feature>, List<Feature>>();
        for (HMMDataStore<AminoAcid, Feature> currentSP: testSequencePairs) {
            predictions.put(currentSP.hiddenSequence, this.viterbi(model, currentSP.observedSequence));
        }
        return predictions;
    }

    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        int correct = 0;
        int total = 0;
        for (List<Feature> trueStates: true2PredictedMap.keySet()) {
            List<Feature> predStates = true2PredictedMap.get(trueStates);
            for (int i = 0; i < trueStates.size(); i++) {
                if (trueStates.get(i) == Feature.MEMBRANE && predStates.get(i) == Feature.MEMBRANE) {
                    correct++;
                    total++;
                } else if (predStates.get(i) == Feature.MEMBRANE) {
                    total++;
                }
            }
        }
        return (double) correct / total;
    }

    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        int correct = 0;
        int total = 0;
        for (List<Feature> trueStates: true2PredictedMap.keySet()) {
            List<Feature> predStates = true2PredictedMap.get(trueStates);
            for (int i = 0; i < trueStates.size(); i++) {
                if (trueStates.get(i) == Feature.MEMBRANE && predStates.get(i) == Feature.MEMBRANE) {
                    correct++;
                    total++;
                } else if (trueStates.get(i) == Feature.MEMBRANE) {
                    total++;
                }
            }
        }
        return (double) correct / total;
    }

    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double prec = this.precision(true2PredictedMap);
        double rec = this.recall(true2PredictedMap);
        return (double) 2 * (prec * rec) / (prec + rec);
    }

}
