package cs224n.wordaligner;

import cs224n.util.*;
import java.util.List;

/**
 * Simple word alignment baseline model that maps source positions to target 
 * positions along the diagonal of the alignment grid.
 *
 * IMPORTANT: Make sure that you read the comments in the
 * cs224n.wordaligner.WordAligner interface.
 *
 * @author Dan Klein
 * @author Spence Green
 */
public class PMIModel implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
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

    for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
      int a_i = -1;
      double maxPMI = 0.0;
      double currentPMI;
      for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
        currentPMI = sourceTargetCounts.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)) /
          (sourceCounts.getCount(sourceWords.get(srcIndex)) * targetCounts.getCount(targetWords.get(tgtIndex)));
        if (currentPMI > maxPMI) {
          a_i = srcIndex;
          maxPMI = currentPMI;
        }
      }
      alignment.addPredictedAlignment(tgtIndex, a_i);
    }
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    sourceCounts = new Counter<String>();
    targetCounts = new Counter<String>();
    sourceTargetCounts = new CounterMap<String,String>();
    for(SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      for(String source : sourceWords){
        for(String target : targetWords){
          // TODO: Warm-up. Your code here for collecting sufficient statistics.
          sourceCounts.incrementCount(source, 1.0);
          targetCounts.incrementCount(target, 1.0);
          sourceTargetCounts.incrementCount(source, target, 1.0);
        }
      }
    }
  }
}
