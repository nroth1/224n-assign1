package cs224n.wordaligner;

import cs224n.util.*;
import java.util.List;

/**
 * A word alignment model based on IBM Model 1.
 *
 * @author Brandon Ewonus
 * @author Nat Roth
 */
public class IBMM1 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  /** Counters for collecting sufficient statistics from the training data. */
  private CounterMap<String, String> countTargetSource; // c(e_j^(k), f_i^(k))
  private Counter<String> countSource; // c(f_i^(k))
  private CounterMap<Pair<Integer, Integer>, Pair<Integer, Integer>> countPosition; // c(j|i,n,m)
  private CounterMap<Integer, Integer> countTargetLengthSourceLength; // c(i,n,m)

  /** Model parameters to estimate. */
  private CounterMap<String, String> t; // t(e|f)
  private CounterMap<Pair<Integer, Integer>, Pair<Integer, Integer>> q; // q(j|i,n,m)

  /**
   * Uses the IBMM1 EM inference algorithm to predict alignments
   * based on the sufficient statistics collected from train().
   *
   * @param sentencePair The sentence pair to align.
   * @return alignment
   */
  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    return alignment;
  }

  /**
   * Return delta. // TODO: compute deltas
   *
   * @param sourceWord
   * @param targetWord
   * @param numTargetWords
   * @return delta
   */
  private double getDelta(String sourceWord, String targetWord, int numTargetWords){
	  double numerator = t.getCount(sourceWord, targetWord);
	  return 1.0;
  }
  
  public void train(List<SentencePair> trainingPairs) {

    t = new CounterMap<String, String>();

    for (SentencePair pair : trainingPairs) {
      // TODO: initialize deltas (for first iteration); based on uniform initialization of t parameters

    }

    // TODO: determine better convergence criteria
    int T = 10; // max iterations, equals 10 for now
    for (int iteration = 0; iteration < T; iteration++) {
      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();
      countSource = new Counter<String>();
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
            double delta = getDelta(sourceWords.get(i), targetWords.get(j), numTargetWords);
            countTargetSource.incrementCount(targetWords.get(j), sourceWords.get(i), delta);
            countSource.incrementCount(targetWords.get(j), delta);
            Pair<Integer, Integer> positionPair = new Pair<Integer, Integer>(j, i);
            Pair<Integer, Integer> lengthsPair = new Pair<Integer, Integer>(numTargetWords, numSourceWords);
            countPosition.incrementCount(positionPair, lengthsPair, delta);
            countTargetLengthSourceLength.incrementCount(numTargetWords, numSourceWords, delta);
          }
        }
      }

      // TODO: update t parameters
      for (String target : countTargetSource.keySet()) { // set of target words (english)
        for (String source : countTargetSource.getCounter(target).keySet()) { // set of source words corresponding to current target (french)
          double updatedValue = countTargetSource.getCount(target, source) / countSource.getCount(source);
          t.setCount(target, source, updatedValue);
        }
      }

      // TODO: update deltas based on new t

    }

    // TODO: set q parameters
    q = new CounterMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();

  }
}
