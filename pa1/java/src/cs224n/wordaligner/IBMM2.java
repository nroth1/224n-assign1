package cs224n.wordaligner;

import cs224n.util.*;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A word alignment model based on IBM Model 2.
 *
 * @author Brandon Ewonus
 * @author Nat Roth
 */
public class IBMM2 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;

  /** Counters for collecting sufficient statistics from the training data. */
  private CounterMap<String, String> countTargetSource; // c(e_j^(k), f_i^(k))
  //  private Counter<String> countSource; // c(f_i^(k))
  private CounterMap<Integer,Pair<Integer, Pair<Integer, Integer>>> countPosition; // c(j|i,n,m)
  private double targetSize;
  //private CounterMap<Integer,Pair<Integer,Integer>> countTargetLengthSourceLength; // c(i,n,m)

  /** Model parameters to estimate. */
  private CounterMap<String, String> t; // t(e|f)
  private CounterMap<Integer,Pair<Integer, Pair<Integer, Integer>>> q; // q(j|i,n,m)

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
        double probMatch = t.getCount(sourceWords.get(j),targetWords.get(i));
        if(probMatch > maxMatch){
          maxMatch = probMatch;
          a_i = j;
        }
      }
      //if alignment wasn't with null.
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
   * @param sourceWord
   * @param targetWord
   * @param sourceList
   * @param cache
   * @return delta
   */
  private double getDelta(String sourceWord, String targetWord, List<String> sourceList, HashMap<String,Double> cache){

    Pair<Integer, Pair<Integer, Integer>> sourceLengthPair =
      new Pair<Integer, Pair<Integer, Integer>>(
        i,
        new Pair<Integer, Integer>(numSourceWords, numTargetWords));

    double numerator = t.getCount(sourceWord, targetWord) * q.getCount(sourceWord, new Pair<Integer, >);

    
    if(cache.containsKey(targetWord)){
      return numerator/cache.get(targetWord);
    }
    double denominator = 0.0;
    for(int i = 0; i < sourceList.size();i++){
      if( t == null){
        denominator +=  1/targetSize;
      }else{
        denominator += t.getCount(sourceList.get(i),targetWord);
      }
    }
    cache.put(targetWord,denominator);
    //System.out.print("---"+denominator);

    return numerator/denominator;
  }

  public void train(List<SentencePair> trainingPairs) {

    // Run IBMM1 to initialize t parameters
    IBMM1 ibmm1 = new IBMM1();
    ibmm1.train(trainingPairs);
    t = ibmm1.getT();

    //initializeT(getCorpus(trainingPairs,false),getCorpus(trainingPairs,true));
    // TODO: determine better convergence criteria
    int T = 25; // max iterations, equals 10 for now
    for (int iteration = 0; iteration < T; iteration++) {
      System.out.println(""+iteration);
      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();
      //countSource = new Counter<String>();
      countPosition = new CounterMap<Integer,Pair<Integer, Pair<Integer, Integer>>>();
      //countTargetLengthSourceLength = new CounterMap<Integer, Integer>();

      // loop through all sentence pairs
      int numSentencePairs = trainingPairs.size();
      long start1 = System.currentTimeMillis();

      for (int k = 0; k < numSentencePairs; k++) {
        List<String> sourceWords = trainingPairs.get(k).getSourceWords();
        //add null 'word'
        sourceWords.add(null);

        List<String> targetWords = trainingPairs.get(k).getTargetWords();
        HashMap<String,Double> cache = new HashMap<String,Double>();
        int numSourceWords = sourceWords.size();
        int numTargetWords = targetWords.size();
        for (int i = 0; i < numSourceWords; i++) { // loop through source words
          for (int j = 0; j < numTargetWords; j++) { // loop through target words
            double delta = getDelta(sourceWords.get(i), targetWords.get(j), sourceWords, cache);
            //System.out.println(sourceWords.get(i));
            countTargetSource.incrementCount(sourceWords.get(i),targetWords.get(j), delta);
            //countSource.incrementCount(sourceWords.get(i), delta);
            Pair<Integer,Pair<Integer,Integer>> sourceLengthPair = new Pair<Integer,Pair<Integer,Integer>>(i,new Pair<Integer,Integer>(numSourceWords,numTargetWords));
            countPosition.incrementCount(j, sourceLengthPair, delta);
            //countTargetLengthSourceLength.incrementCount(numTargetWords, numSourceWords, delta);
          }
        }
        //remove null word
        sourceWords.remove(sourceWords.size()-1);
      }
//      long end1 = System.currentTimeMillis();
//      System.out.println(""+(start1-end1));

      //Normalize t(e|f) setting it equal to c(e,f)/c(f)
//      long start = System.currentTimeMillis();
      t = Counters.conditionalNormalize(countTargetSource);
//      long end = System.currentTimeMillis();
//      System.out.println(""+(start-end));

      // set q parameters -- conditional normalization of c(j|i,l,m)
      q = Counters.conditionalNormalize(countPosition);
    }
  }
}
