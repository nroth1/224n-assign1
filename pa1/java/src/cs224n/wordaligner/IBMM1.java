package cs224n.wordaligner;

import cs224n.util.*;
import java.util.List;

/**
 * A word alignment model
 *
 * @author Brandon Ewonus
 * @author Nat Roth
 */
public class IBMM1 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String, String> countTargetSource; // c(e_j^(k), f_i^(k))
  private Counter<String> countTarget; // c(e_j^(k))
  private CounterMap<Pair<Integer, Integer>, Pair<Integer, Integer>> countPosition; // c(j|i,l,m)
  private CounterMap<Integer, Integer> countTargetLengthSourceLength; // c(i,l,m)

  // model paramaters to estimate
  private CounterMap<String, String> t; // t(f|e)
  private CounterMap<Pair<Integer, Integer>, Pair<Integer, Integer>> q; // q(j|i,l,m)


  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below.
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {

    for (SentencePair pair : trainingPairs) {
      // TODO: initialize deltas (for first iteration); requires prior initialization of t parameters
    }

    int T = 10; // max iterations, equals 10 for now
    for (int t = 0; t < T; t++) {
      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();
      countTarget = new Counter<String>();
      countPosition = new CounterMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
      countTargetLengthSourceLength = new CounterMap<Integer, Integer>();


      // loop through all sentence pairs
      int numSentencePairs = trainingPairs.size();
      for (int k = 0; k < numSentencePairs; k++) {
        List<String> sourceWords = trainingPairs.get(k).getSourceWords();
        List<String> targetWords = trainingPairs.get(k).getTargetWords();
        int numSourceWords = sourceWords.size();
        int numTargetWords = targetWords.size();
        for (int i = 0; i < numSourceWords; i++) { // loop through source words
          for (int j = 0; j < numTargetWords; j++) { // loop through target words
            int delta = 1; // TODO: use actual delta
            countTargetSource.incrementCount(targetWords.get(j), sourceWords.get(i), delta);
            countTarget.incrementCount(targetWords.get(j), delta);
            Pair<Integer, Integer> positionPair = new Pair<Integer, Integer>(j, i);
            Pair<Integer, Integer> lengthsPair = new Pair<Integer, Integer>(numTargetWords, numSourceWords);
            countPosition.incrementCount(positionPair, lengthsPair, delta);
            countTargetLengthSourceLength.incrementCount(numTargetWords, numSourceWords, delta);
          }
        }
      }

      // TODO: update t (and q?), then deltas

    }
  }
}
