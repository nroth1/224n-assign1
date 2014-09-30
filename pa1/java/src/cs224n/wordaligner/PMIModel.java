package cs224n.wordaligner;

import cs224n.util.*;
import java.util.List;

/**
 * A word alignment model based on pointwise mutual information (PMI).
 *
 * @author Brandon Ewonus
 * @author Nat Roth
 */
public class PMIModel implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  /** Counters for collecting sufficient statistics from the training data. */
  private double totalSentences;
  private Counter<String> sourceCounts;
  private Counter<String> targetCounts;
  private CounterMap<String,String> sourceTargetCounts;

  /**
   * Uses the inference algorithm for Eq.1 in the assignment handout
   * to predict alignments based on the counts collected from train().
   *
   * @param sentencePair The sentence pair to align.
   * @return alignment
   */
  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    List<String> sourceWords = sentencePair.getSourceWords();
    List<String> targetWords = sentencePair.getTargetWords();
    int numSourceWords = sourceWords.size();
    int numTargetWords = targetWords.size();

    for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
      // Alignment a_i is initially set to -1 (corresponding to null alignment)
      int a_i = -1;
      // We always have c(null, e_i) / (c(null), c(e_i)) == 1.0/totalSentences
      double maxPMI = 1.0/totalSentences;
      double currentPMI;
      for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
        currentPMI = sourceTargetCounts.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)) /
          (sourceCounts.getCount(sourceWords.get(srcIndex)) * targetCounts.getCount(targetWords.get(tgtIndex)));

// sanity check!
//        System.out.println("joint count " + sourceTargetCounts.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)));
//        System.out.println("source count " + sourceCounts.getCount(sourceWords.get(srcIndex)));
//        System.out.println("target count " + targetCounts.getCount(targetWords.get(tgtIndex)));
//        System.out.println("PMI: " + sourceWords.get(srcIndex) + " " + targetWords.get(tgtIndex) + currentPMI + "\n");

        // Update a_i and maxPMI
        if (currentPMI > maxPMI) { // TODO: tiebreakers?
          a_i = srcIndex;
          maxPMI = currentPMI;
        }
      }
      // If a_i > -1, then e_i is not aligned to null, so we add the predicted alignment
      if (a_i > -1) {
        alignment.addPredictedAlignment(tgtIndex, a_i);
      }
    }
    return alignment;
  }

  /**
   * Collects sufficient statistics from the training data.
   *
   * @param trainingPairs
   */
  public void train(List<SentencePair> trainingPairs) {
    totalSentences = trainingPairs.size();
    sourceCounts = new Counter<String>();
    targetCounts = new Counter<String>();
    sourceTargetCounts = new CounterMap<String,String>();

    // Loop through training pairs
    for(SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();

      // Need to add target word counts exactly once
      boolean allTargetsAdded = false;

      for(String source : sourceWords) {
        sourceCounts.incrementCount(source, 1.0);
        for(String target : targetWords){
          if (!allTargetsAdded) {
            targetCounts.incrementCount(target, 1.0);
          }
          sourceTargetCounts.incrementCount(source, target, 1.0);
        }
        allTargetsAdded = true;
      }
    }
  }
}
