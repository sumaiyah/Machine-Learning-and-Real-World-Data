package uk.ac.cam.cl.mlrd.exercises.markov_models;

import uk.ac.cam.cl.mlrd.interfaces.IExercise9;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise9 implements IExercise9 {
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException{
        // T R A N S I T I O N S
        // ---------------------------------------------------------------------------------------------------------------------------------------
        Map<Feature, Map<Feature, Double>> transitions = new HashMap<>();

        List<HMMDataStore<AminoAcid, Feature>> diceFiles = (sequencePairs);
        transitions = new HashMap<>();
        Map<Feature, Double> totals = new HashMap<>();

        // initialise counts to 0
        for (Feature Feature: Feature.values()){
            Map<Feature, Double> to = new HashMap<>();
            for (Feature inFeature: Feature.values()){
                to.put(inFeature, 0.0);
            }
            transitions.put(Feature, to);
            totals.put(Feature, 0.0);
        }

        for (int index = 0; index<diceFiles.size(); index++){
            HMMDataStore<AminoAcid, Feature> diceSequence = diceFiles.get(index);
            for (int innerIndex = 1; innerIndex<diceSequence.hiddenSequence.size(); innerIndex++){
                Map<Feature, Double> before = transitions.get(diceSequence.hiddenSequence.get(innerIndex-1));
                before.put(diceSequence.hiddenSequence.get(innerIndex), before.get(diceSequence.hiddenSequence.get(innerIndex)) + 1);
                totals.put(diceSequence.hiddenSequence.get(innerIndex-1), totals.get(diceSequence.hiddenSequence.get(innerIndex-1)) + 1);
            }
        }

        for (Feature d: transitions.keySet()){
            Map<Feature, Double> thisDie = transitions.get(d);
            for (Feature e: transitions.get(d).keySet()){
                if (totals.get(d) != 0)
                    thisDie.put(e, thisDie.get(e)/totals.get(d));
            }
            transitions.put(d, thisDie);
        }

        // E M I S S I O N S
        // ---------------------------------------------------------------------------------------------------------------------------------------
        Map<Feature, Map<AminoAcid, Double>> emissions = new HashMap<>();
        Map<Feature, Double> emitTotals = new HashMap<>();

        // initialise counts to 0
        for (Feature Feature: Feature.values()){
            Map<AminoAcid, Double> to = new HashMap<>();
            for (AminoAcid inFeature: AminoAcid.values()){
                if (inFeature != AminoAcid.SEC){
                    to.put(inFeature, 0.0);
                }
            }
            emissions.put(Feature, to);
            emitTotals.put(Feature, 0.0);
        }



        for (int index = 0; index<diceFiles.size(); index++){
            List<Feature> Features = diceFiles.get(index).hiddenSequence;
            List<AminoAcid> AminoAcids = diceFiles.get(index).observedSequence;

            for (int innerIndex = 0; innerIndex < Features.size(); innerIndex++){
                Map<AminoAcid, Double> currentSeq = emissions.get(Features.get(innerIndex));
                currentSeq.put(AminoAcids.get(innerIndex), currentSeq.get(AminoAcids.get(innerIndex)) + 1);
                emitTotals.put(Features.get(innerIndex), emitTotals.get(Features.get(innerIndex)) + 1);
            }
        }

        for (Feature d: emissions.keySet()){
            Map<AminoAcid, Double> thisDie = emissions.get(d);
            for (AminoAcid e: emissions.get(d).keySet()){
                if (emitTotals.get(d) != 0)
                    thisDie.put(e, thisDie.get(e)/emitTotals.get(d));
            }
            emissions.put(d, thisDie);
        }

        HiddenMarkovModel<AminoAcid, Feature> result = new HiddenMarkovModel<>(transitions, emissions);
        return result;
    }

    public Map<Feature, Double> getProbs(int t, Feature current, Set<Feature> previousStates, HiddenMarkovModel<AminoAcid, Feature> model,
                                          List<AminoAcid> observedSequence, List<Map<Feature, Double>> delta){
        Map<Feature, Double> probs = new HashMap<>();

        double transition;
        double omitBF;
        double prevDelta;

        for (Feature d: previousStates){
            transition = model.getTransitions(d).get(current);
            omitBF = model.getEmissions(current).get(observedSequence.get(t));
            prevDelta = delta.get(t-1).get(d); // already log
            probs.put(d, Math.log(transition*omitBF) + prevDelta);
        }
        return probs;
    }

    public Feature getPrev(Map<Feature, Double> probs){
        double max = -Integer.MAX_VALUE;
        Feature maxDice = null;
        for (Feature d: probs.keySet()){
            if (probs.get(d) > max){
                max = probs.get(d);
                maxDice = d;
            }
        }
        return maxDice;
    }

    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence){
        List<Map<Feature, Feature>> psi = new ArrayList<>();
        List<Map<Feature, Double>> delta = new ArrayList<>();
        List<Feature> hiddenSeq = new ArrayList<>();

        Map<Feature, Double> deltaMap = new HashMap<>();
        Map<Feature, Feature> psiMap = new HashMap<>();

        Set<Feature> states = new HashSet<>(model.getTransitionMatrix().keySet());
        states.remove(Feature.START);

        int t = 0;
        deltaMap.put(Feature.START, model.getEmissions(Feature.START).get(observedSequence.get(t)));
        delta.add(deltaMap);
        psiMap.put(Feature.START, Feature.START);
        psi.add(psiMap);

        t++;
        deltaMap = new HashMap<>();
        psiMap = new HashMap<>();
        states.add(Feature.END);
        for (Feature d: states){
            double transProbAiF =  model.getTransitions(Feature.START).get(d);
            double emitProbBF = model.getEmissions(d).get(observedSequence.get(t));
            double currentDelta = delta.get(t-1).get(Feature.START);
            deltaMap.put(d, Math.log(transProbAiF*emitProbBF*currentDelta));
            psiMap.put(d, Feature.START);
        }
        delta.add(deltaMap);
        psi.add(psiMap);

        states.add(Feature.END);
        for (t = 2; t < observedSequence.size(); t++){
            Set<Feature> previous = delta.get(t-1).keySet();
            deltaMap = new HashMap<>();
            psiMap = new HashMap<>();
            for (Feature current: states){
                if (model.getEmissionMatrix().get(current).get(observedSequence.get(t)) > 0){
                    Map<Feature, Double> probs = getProbs(t,current,previous ,model, observedSequence,delta);
                    double maxProb = Collections.max(probs.values());
                    deltaMap.put(current, maxProb);
                    Feature prev = getPrev(probs);
                    psiMap.put(current, prev);
                }
            }
            delta.add(deltaMap);
            psi.add(psiMap);
        }

        // gives you a reversed list
        Feature current = Feature.END;
        for (int i = 0; i < observedSequence.size(); i++){
            hiddenSeq.add(current);
            current = psi.get(observedSequence.size()-1-i).get(current);
        }
        Collections.reverse(hiddenSeq);
        return hiddenSeq;
    }

    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model,
                                                 List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException{
        Map<List<Feature>, List<Feature>> result = new HashMap<>();

        for (HMMDataStore m: testSequencePairs){
            List<AminoAcid> observedSequence = m.observedSequence;
            List<Feature> trueList = m.hiddenSequence;
            List<Feature> predicted = viterbi(model, observedSequence);
            result.put(trueList, predicted);
        }
        return result;
    }

    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap){
        double numCorrectL = 0;
        double numPredL = 0;

        for (List<Feature> trueList: true2PredictedMap.keySet()){
            List<Feature> predList = true2PredictedMap.get(trueList);
            for (int index= 0; index < trueList.size(); index++){
                if ((trueList.get(index) == predList.get(index)) && (predList.get(index) == Feature.MEMBRANE)) numCorrectL += 1;
                if (predList.get(index) == Feature.MEMBRANE) numPredL += 1;
            }
        }
        return (numCorrectL/numPredL);
    }

    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap){
        double numCorrectL = 0;
        double numTrueL = 0;
        for (List<Feature> trueList: true2PredictedMap.keySet()){
            List<Feature> predList = true2PredictedMap.get(trueList);
            for (int index= 0; index < trueList.size(); index++){
                if ((trueList.get(index) == predList.get(index)) && (predList.get(index) == Feature.MEMBRANE)) numCorrectL += 1;
                if (trueList.get(index) == Feature.MEMBRANE) numTrueL += 1;
            }
        }
        return (numCorrectL/numTrueL);
    }

    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap){
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        double result = 2 * ((precision*recall)/(precision+recall));

        return result;
    }

    }


