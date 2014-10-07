package cs224n.wordaligner;

import cs224n.util.*;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Random;

/**
 * A word alignment model based on IBM Model 2.
 *
 * @author Brandon Ewonus
 * @author Nat Roth
 */
public class IBMM2 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  /** Model parameters to estimate. */
  private CounterMap<String, String> t; // t(e|f)
  private CounterMap<Pair<Integer, Pair<Integer, Integer>>, Integer> q; // q(j|i,n,m)

  /**
   * Uses the IBMM2 EM inference algorithm to predict alignments
   * based on the sufficient statistics collected from train().
   *
   * @param sentencePair The sentence pair to align.
   * @return alignment
   */
  public Alignment align(SentencePair sentencePair) {

    Alignment alignment = new Alignment();
    List<String> targetWords = sentencePair.getTargetWords();
    List<String> sourceWords = sentencePair.getSourceWords();
    sourceWords.add(null);
    for (int i = 0; i < targetWords.size(); i++) {
      double maxMatch = 0.0;
      int a_i = -1;
      for (int j = 0; j < sourceWords.size(); j++) {
    	  Pair<Integer, Pair<Integer, Integer>> sourceLengthPair = new Pair<Integer, Pair<Integer, Integer>>(
          i,
          new Pair<Integer, Integer>(sourceWords.size(), targetWords.size()));
        double probMatch = t.getCount(sourceWords.get(j), targetWords.get(i)) * q.getCount(sourceLengthPair, j);
        if (probMatch > maxMatch) {
          maxMatch = probMatch;
          a_i = j;
        }
      }
      //if alignment wasn't with null.
      if (sourceWords.get(a_i) != null) {
        alignment.addPredictedAlignment(i, a_i);
      }
    }
    sourceWords.remove(sourceWords.size() - 1);
    return alignment;
  }

  /**
   * Return delta. Cache denominator to improve performance.
   *
   * @param sourceWord
   * @param targetWord
   * @param sourceList
   * @param cache
   * @return delta
   */
  private double getDelta(
    String sourceWord,
    String targetWord,
    int sourceIndex,
    int targetIndex,
    List<String> sourceList,
    List<String> targetList,
    HashMap<Pair<Integer, Pair<Integer, Integer>>,
    Double> cache)
  {
	  int numSourceWords = sourceList.size();
	  int numTargetWords = targetList.size();
	  Pair<Integer, Pair<Integer, Integer>> sourceLengthPair =
      new Pair<Integer, Pair<Integer, Integer>>(
        targetIndex,
        new Pair<Integer, Integer>(numSourceWords, numTargetWords));

    double numerator = t.getCount(sourceWord, targetWord) * q.getCount(sourceLengthPair,sourceIndex);
    if (cache.containsKey(sourceLengthPair)) {
      return numerator/cache.get(sourceLengthPair);
    }

    double denominator = 0.0;
    for (int i = 0; i < sourceList.size(); i++) {
    	new Pair<Integer, Pair<Integer, Integer>>(
    			targetIndex,
    		    new Pair<Integer, Integer>(numSourceWords, numTargetWords));
        denominator += t.getCount(sourceList.get(i), targetWord) * q.getCount(sourceLengthPair, i);
    }
    cache.put(sourceLengthPair, denominator);

    return numerator/denominator;
  }

  /**
   * Randomly initialize q parameters to random doubles in [0.0, 1.0]
   *
   * @param trainingPairs
   * @return q
   */
  private CounterMap<Pair<Integer, Pair<Integer, Integer>>, Integer> randInitQ(List<SentencePair> trainingPairs) {
	  q = new CounterMap<Pair<Integer, Pair<Integer, Integer>>, Integer>();
	  for (SentencePair sp : trainingPairs) {
		  int numSourceWords = sp.getSourceWords().size()+1;
		  int numTargetWords = sp.getTargetWords().size();
		  for (int i = 0; i < numTargetWords; i++) {
			  // init pair (really triple, with target and length pair)
			  Pair<Integer, Pair<Integer, Integer>> lengthPair = new Pair<Integer, Pair<Integer, Integer>>(
          i,
          new Pair<Integer, Integer>(numSourceWords, numTargetWords));
			  // loop over source words
			  for (int j = 0; j < numSourceWords; j++) {
				  if (q.getCount(lengthPair, j) == 0.0) {
					  q.incrementCount(lengthPair, j, new Random().nextDouble());
				  }
			  }
		  }
	  }
	  return Counters.conditionalNormalize(q);
  }

  /**
   * Learn the model parameters from the collection of parallel sentences.
   *
   * @param trainingPairs The sentence pairs for training the aligner
   */
  public void train(List<SentencePair> trainingPairs) {

    // Run IBMM1 to initialize t parameters
    IBMM1 ibmm1 = new IBMM1();
    ibmm1.train(trainingPairs);
    t = ibmm1.getT();
    q = randInitQ(trainingPairs);

    // Counters for collecting sufficient statistics from the training data
    CounterMap<String, String> countTargetSource;
    CounterMap<Pair<Integer, Pair<Integer, Integer>>, Integer> countPosition;

    int T = 50; // max iterations
    for (int iteration = 0; iteration < T; iteration++) {
      System.out.println(""+iteration);

      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();
      countPosition = new CounterMap<Pair<Integer, Pair<Integer, Integer>>, Integer>();

      // loop through all sentence pairs
      for (SentencePair trainingPair : trainingPairs) {
        List<String> sourceWords = trainingPair.getSourceWords();
        //add null 'word'
        sourceWords.add(null);
        List<String> targetWords = trainingPair.getTargetWords();
        HashMap<Pair<Integer, Pair<Integer, Integer>>, Double> cache = new HashMap<Pair<Integer, Pair<Integer, Integer>>, Double>();
        int numSourceWords = sourceWords.size();
        int numTargetWords = targetWords.size();
        for (int i = 0; i < numSourceWords; i++) { // loop through source words (french)
          for (int j = 0; j < numTargetWords; j++) { // loop through target words (english)
            double delta = getDelta(sourceWords.get(i), targetWords.get(j), i, j, sourceWords, targetWords, cache);
            countTargetSource.incrementCount(sourceWords.get(i),targetWords.get(j), delta);
            Pair<Integer, Pair<Integer, Integer>> sourceLengthPair =
              new Pair<Integer,Pair<Integer,Integer>>(
                j,
                new Pair<Integer, Integer>(numSourceWords, numTargetWords));
            countPosition.incrementCount(sourceLengthPair, i, delta);
          }
        }
        //remove null word
        sourceWords.remove(sourceWords.size() - 1);
      }

      //Normalize t(e|f) setting it equal to c(e,f)/c(f)
      t = Counters.conditionalNormalize(countTargetSource);

      // set q parameters -- conditional normalization of c(j|i,l,m)
      q = Counters.conditionalNormalize(countPosition);
    }
  }
}
