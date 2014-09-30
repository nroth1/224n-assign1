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

  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private double totalSentences;
  private Counter<String> sourceCounts;
  private Counter<String> targetCounts;
  private CounterMap<String,String> sourceTargetCounts;

  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below.
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();
    List<String> sourceWords = sentencePair.getSourceWords();
    List<String> targetWords = sentencePair.getTargetWords();
    int numSourceWords = sourceWords.size();
    int numTargetWords = targetWords.size();


    sourceWords.add(null);

    for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
      int a_i = -1;
//      double maxPMI = 1.0/totalSentences; // double maxPMI = 0.0;
      double maxPMI = 0.0;
      double currentPMI;
      for (int srcIndex = 0; srcIndex < numSourceWords + 1; srcIndex++) {
        currentPMI = sourceTargetCounts.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)) /
          (sourceCounts.getCount(sourceWords.get(srcIndex)) * targetCounts.getCount(targetWords.get(tgtIndex)));
        System.out.println("joint count " + sourceTargetCounts.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)));
        System.out.println("source count " + sourceCounts.getCount(sourceWords.get(srcIndex)));
        System.out.println("target count " + targetCounts.getCount(targetWords.get(tgtIndex)));
        System.out.println("PMI: " + sourceWords.get(srcIndex) + " " + targetWords.get(tgtIndex) + currentPMI + "\n");
        if (currentPMI > maxPMI) { // TODO: tiebreakers?
          a_i = srcIndex;
          maxPMI = currentPMI;
        }
      }
      if (sourceWords.get(a_i) != null) {
//      if (a_i > -1) {
        alignment.addPredictedAlignment(tgtIndex, a_i);
      }
    }
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    totalSentences = trainingPairs.size();
    sourceCounts = new Counter<String>();
    targetCounts = new Counter<String>();
    sourceTargetCounts = new CounterMap<String,String>();
    for(SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
//      if (sourceWords.size() < targetWords.size()) {
        sourceWords.add(null);
//      }
      boolean allTargetsAdded = false;
      for(String source : sourceWords) {
        sourceCounts.incrementCount(source, 1.0);
        for(String target : targetWords){
          // TODO: Warm-up. Your code here for collecting sufficient statistics.
          if (!allTargetsAdded) {
            targetCounts.incrementCount(target, 1.0);
          }
          sourceTargetCounts.incrementCount(source, target, 1.0);
        }
        allTargetsAdded = true;
      }
//      for(String target : targetWords) {
//        targetCounts.incrementCount(target, 1.0);
//      }
      sourceWords.remove(sourceWords.size() - 1);
    }
  }
}
