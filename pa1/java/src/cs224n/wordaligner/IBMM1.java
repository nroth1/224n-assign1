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
   * Model parameters to estimate. Note that q is immaterial to the
   * inference algorithm with IBMM1, so we don't bother to compute it.
   */
  private CounterMap<String, String> t; // t(e|f)

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

    int T = 15; // max iterations
    for (int iteration = 0; iteration < T; iteration++) {
      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();

      // loop through all sentence pairs
      for (SentencePair trainingPair : trainingPairs) {
        List<String> sourceWords = trainingPair.getSourceWords();
        // add null 'word'
        sourceWords.add(null);

        List<String> targetWords = trainingPair.getTargetWords();
	      HashMap<String, Double> cache = new HashMap<String, Double>();
        for (String sourceWord : sourceWords) { // loop through source words (french)
          for (String targetWord : targetWords) { // loop through target words (english)
            double delta = getDelta(sourceWord, targetWord, sourceWords, cache);
            countTargetSource.incrementCount(sourceWord, targetWord, delta);
          }
        }
        // remove null word
        sourceWords.remove(sourceWords.size() - 1);
      }

      // Normalize t(e|f) setting it equal to c(e,f)/c(f)
      t = Counters.conditionalNormalize(countTargetSource);
    }
  }
}
