package cs224n.wordaligner;

import cs224n.util.*;

import java.util.HashMap;
import java.util.List;

/**
 * A word alignment model based on IBM Model 1.
 *
 * @author Brandon Ewonus
 * @author Nat Roth
 */
public class IBMM1 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  /**
   * Stores counts of 4-tuples (targetIndex, sourceIndex, numSourceWords, numTargetWords)
   */
  private CounterMap<Integer, Pair<Integer, Pair<Integer, Integer>>> countPosition;

  /**
   * Model parameters to estimate. Note that q is immaterial to the
   * inference algorithm with IBMM1, but we compute it anyway.
   */
  private CounterMap<String, String> t; // t(e|f)
  private CounterMap<Integer, Pair<Integer, Pair<Integer, Integer>>> q; // q(j|i,n,m)

  /**
   * Retrieve the t parameters. IBMM2 uses the t values trained from IBMM1 as
   * its initial estimate of the t parameters.
   */
  protected CounterMap<String, String> getT() {
    return t;
  }

  /**
   * Uses the IBMM1 EM inference algorithm to predict alignments
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
    for(int i = 0; i < targetWords.size();i++){
    	double maxMatch = 0.0;
    	int a_i = -1;
    	for(int j = 0; j < sourceWords.size();j++){
    		double probMatch = t.getCount(sourceWords.get(j), targetWords.get(i));
    		if(probMatch > maxMatch){
    			maxMatch = probMatch;
    			a_i = j;
    		}
    	}
    	// if alignment wasn't with null.
    	if(sourceWords.get(a_i) != null){
    		alignment.addPredictedAlignment(i, a_i);
    	}
    }
    sourceWords.remove(sourceWords.size()-1);    
    return alignment;
  }

  /**
   * Return delta. Cache denominator to improve performance.
   *
   * @param sourceWord (French word)
   * @param targetWord (English word)
   * @param sourceList List of source words in the current training sentence pair
   * @param cache Stores denominators used in the delta computation
   * @return delta
   */
  private double getDelta(String sourceWord, String targetWord, List<String> sourceList, HashMap<String, Double> cache) {
    // if null, delta is (number of source words)^(-1)
    if (t == null) {
      return 1.0 / sourceList.size();
    }

    double numerator = t.getCount(sourceWord, targetWord);
    if (cache.containsKey(targetWord)) {
      return numerator / cache.get(targetWord);
    }

    double denominator = 0.0;
    for (String word : sourceList) {
      denominator += t.getCount(word, targetWord);
    }
    cache.put(targetWord, denominator);

    return numerator / denominator;
  }

  /**
   * Learn the model parameters from the collection of parallel sentences.
   *
   * @param trainingPairs The sentence pairs for training the aligner
   */
  public void train(List<SentencePair> trainingPairs) {

    // stores counts of (targetWord, sourceWord)
    CounterMap<String, String> countTargetSource;
    t = null;

    int T = 25; // max iterations
    for (int iteration = 0; iteration < T; iteration++) {
      System.out.println(""+iteration);

      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();
      countPosition = new CounterMap<Integer, Pair<Integer, Pair<Integer, Integer>>>();

      // loop through all sentence pairs
      for (SentencePair trainingPair : trainingPairs) {
        List<String> sourceWords = trainingPair.getSourceWords();
        // add null 'word'
        sourceWords.add(null);

        List<String> targetWords = trainingPair.getTargetWords();
	      HashMap<String, Double> cache = new HashMap<String, Double>();
        int numSourceWords = sourceWords.size();
        int numTargetWords = targetWords.size();
        for (int i = 0; i < numSourceWords; i++) { // loop through source words (french)
          for (int j = 0; j < numTargetWords; j++) { // loop through target words (english)
            double delta = getDelta(sourceWords.get(i), targetWords.get(j), sourceWords, cache);
            countTargetSource.incrementCount(sourceWords.get(i), targetWords.get(j), delta);
            Pair<Integer, Pair<Integer, Integer>> sourceLengthPair =
              new Pair<Integer, Pair<Integer, Integer>>(
                i,
                new Pair<Integer, Integer>(numSourceWords, numTargetWords));
            countPosition.incrementCount(j, sourceLengthPair, delta);
          }
        }
        // remove null word
        sourceWords.remove(sourceWords.size() - 1);
      }

      // Normalize t(e|f) setting it equal to c(e,f)/c(f)
      t = Counters.conditionalNormalize(countTargetSource);
    }

    // set q parameters -- once at end, conditional normalization of c(j|i,l,m)
    q = Counters.conditionalNormalize(countPosition);
  }
}
