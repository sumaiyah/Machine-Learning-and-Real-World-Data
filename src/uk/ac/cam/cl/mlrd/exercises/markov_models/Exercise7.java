package uk.ac.cam.cl.mlrd.exercises.markov_models;

import uk.ac.cam.cl.mlrd.interfaces.IExercise7;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Exercise7 implements IExercise7 {
    static final Path dataDirectory = Paths.get("data/dice_dataset");

    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException{
        // T R A N S I T I O N S
        // ---------------------------------------------------------------------------------------------------------------------------------------
        Map<DiceType, Map<DiceType, Double>> transitions = new HashMap<>();

        List<HMMDataStore<DiceRoll, DiceType>> diceFiles = HMMDataStore.loadDiceFiles(sequenceFiles);
        transitions = new HashMap<>();
        Map<DiceType, Double> totals = new HashMap<>();

        // initialise counts to 0
        for (DiceType diceType: DiceType.values()){
            Map<DiceType, Double> to = new HashMap<>();
            for (DiceType inDiceType: DiceType.values()){
                to.put(inDiceType, 0.0);
            }
            transitions.put(diceType, to);
            totals.put(diceType, 0.0);
        }

        for (int index = 0; index<diceFiles.size(); index++){
            HMMDataStore<DiceRoll, DiceType> diceSequence = diceFiles.get(index);
            for (int innerIndex = 1; innerIndex<diceSequence.hiddenSequence.size(); innerIndex++){
                Map<DiceType, Double> before = transitions.get(diceSequence.hiddenSequence.get(innerIndex-1));
                before.put(diceSequence.hiddenSequence.get(innerIndex), before.get(diceSequence.hiddenSequence.get(innerIndex)) + 1);
                totals.put(diceSequence.hiddenSequence.get(innerIndex-1), totals.get(diceSequence.hiddenSequence.get(innerIndex-1)) + 1);
            }
        }

        for (DiceType d: transitions.keySet()){
            Map<DiceType, Double> thisDie = transitions.get(d);
            for (DiceType e: transitions.get(d).keySet()){
                if (totals.get(d) != 0)
                thisDie.put(e, thisDie.get(e)/totals.get(d));
            }
            transitions.put(d, thisDie);
        }

        // E M I S S I O N S
        // ---------------------------------------------------------------------------------------------------------------------------------------
        Map<DiceType, Map<DiceRoll, Double>> emissions = new HashMap<>();
        Map<DiceType, Double> emitTotals = new HashMap<>();

        // initialise counts to 0
        for (DiceType diceType: DiceType.values()){
            Map<DiceRoll, Double> to = new HashMap<>();
            for (DiceRoll inDiceType: DiceRoll.values()){
                to.put(inDiceType, 0.0);
            }
            emissions.put(diceType, to);
            emitTotals.put(diceType, 0.0);
        }

        for (int index = 0; index<diceFiles.size(); index++){
            List<DiceType> diceTypes = diceFiles.get(index).hiddenSequence;
            List<DiceRoll> diceRolls = diceFiles.get(index).observedSequence;

            for (int innerIndex = 0; innerIndex<diceTypes.size(); innerIndex++){
                Map<DiceRoll, Double> currentSeq = emissions.get(diceTypes.get(innerIndex));
                currentSeq.put(diceRolls.get(innerIndex), currentSeq.get(diceRolls.get(innerIndex)) + 1);
                emitTotals.put(diceTypes.get(innerIndex), emitTotals.get(diceTypes.get(innerIndex)) + 1);
            }
        }

        for (DiceType d: emissions.keySet()){
            Map<DiceRoll, Double> thisDie = emissions.get(d);
            for (DiceRoll e: emissions.get(d).keySet()){
                if (emitTotals.get(d) != 0)
                    thisDie.put(e, thisDie.get(e)/emitTotals.get(d));
            }
            emissions.put(d, thisDie);
        }
        HiddenMarkovModel<DiceRoll, DiceType> result = new HiddenMarkovModel<>(transitions, emissions);
        return result;
    }
}
