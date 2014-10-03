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

  /** Counters for collecting sufficient statistics from the training data. */
  private CounterMap<String, String> countTargetSource; // c(e_j^(k), f_i^(k))
  //  private Counter<String> countSource; // c(f_i^(k))
  private CounterMap<Pair<Integer, Pair<Integer, Integer>>,Integer> countPosition; // c(j|i,n,m)
  private double targetSize;
  //private CounterMap<Integer,Pair<Integer,Integer>> countTargetLengthSourceLength; // c(i,n,m)

  /** Model parameters to estimate. */
  private CounterMap<String, String> t; // t(e|f)
  private CounterMap<Pair<Integer, Pair<Integer, Integer>>,Integer> q; // q(j|i,n,m)

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
    	  Pair<Integer, Pair<Integer, Integer>> sourceLengthPair = new Pair<Integer, Pair<Integer, Integer>>(
	    			i,
	    		    new Pair<Integer, Integer>(sourceWords.size(), targetWords.size()));  
        double probMatch = t.getCount(sourceWords.get(j),targetWords.get(i)) * q.getCount(sourceLengthPair,j);
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
  private double getDelta(String sourceWord, String targetWord,int sourceIndex,int targetIndex, List<String> sourceList,List<String> targetList, HashMap<String,Double> cache){
	  int numSourceWords = sourceList.size();
	  int numTargetWords = targetList.size();
	  Pair<Integer, Pair<Integer, Integer>> sourceLengthPair =
      new Pair<Integer, Pair<Integer, Integer>>(
        targetIndex,
        new Pair<Integer, Integer>(numSourceWords, numTargetWords));

    double numerator = t.getCount(sourceWord, targetWord) * q.getCount(sourceLengthPair,sourceIndex);
    //System.out.println("---"+ q.getCount(sourceLengthPair,sourceIndex));
    
    //if(cache.containsKey(targetWord)){
    //  return numerator/cache.get(targetWord);
    //}
    double denominator = 0.0;
    for(int i = 0; i < sourceList.size();i++){
      if( t == null){
        denominator +=  1/targetSize;
      }else{
    	new Pair<Integer, Pair<Integer, Integer>>(
    			targetIndex,
    		    new Pair<Integer, Integer>(numSourceWords, numTargetWords));
        denominator += t.getCount(sourceList.get(i),targetWord) * q.getCount(sourceLengthPair,i);
      }
    }
    cache.put(targetWord,denominator);
    

    return numerator/denominator;
  }

  
  private CounterMap<Pair<Integer, Pair<Integer, Integer>>,Integer> randInitQ(List<SentencePair> trainingPairs){
	  q = new CounterMap<Pair<Integer, Pair<Integer, Integer>>,Integer>();
	  for(SentencePair sp:trainingPairs){
		  int numSourceWords = sp.getSourceWords().size()+1;
		  int numTargetWords = sp.getTargetWords().size();
		  for(int i = 0;i<numTargetWords;i++){
			  //init pair (really triple, with target and length pair
			  Pair<Integer, Pair<Integer, Integer>> lengthPair = new Pair<Integer, Pair<Integer, Integer>>(
		    			i,
		    		    new Pair<Integer, Integer>(numSourceWords, numTargetWords));
			  //loop over source words
			  for(int j = 0;j<numSourceWords;j++){
				  if(q.getCount(lengthPair, j) == 0.0){
					  q.incrementCount(lengthPair, j, new Random().nextDouble());
				  }
			  }
		  }
	  }
	  return Counters.conditionalNormalize(q);
  }
  
  public void train(List<SentencePair> trainingPairs) {

    // Run IBMM1 to initialize t parameters
    IBMM1 ibmm1 = new IBMM1();
    ibmm1.train(trainingPairs);
    t = ibmm1.getT();
    q = randInitQ(trainingPairs);

    //initializeT(getCorpus(trainingPairs,false),getCorpus(trainingPairs,true));
    // TODO: determine better convergence criteria
    int T = 25; // max iterations, equals 10 for now
    for (int iteration = 0; iteration < T; iteration++) {
      System.out.println(""+iteration);
      // set all counts c to zero
      countTargetSource = new CounterMap<String, String>();
      //countSource = new Counter<String>();
      countPosition = new CounterMap<Pair<Integer, Pair<Integer, Integer>>,Integer>();
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
          for (int j = 0; j < numTargetWords; j++) {
        	// loop through target words
            double delta = getDelta(sourceWords.get(i), targetWords.get(j),i,j, sourceWords,targetWords, cache);
            //System.out.println(sourceWords.get(i));
            countTargetSource.incrementCount(sourceWords.get(i),targetWords.get(j), delta);
            //countSource.incrementCount(sourceWords.get(i), delta);
            Pair<Integer,Pair<Integer,Integer>> sourceLengthPair = new Pair<Integer,Pair<Integer,Integer>>(j,new Pair<Integer,Integer>(numSourceWords,numTargetWords));
            countPosition.incrementCount(sourceLengthPair, i, delta);
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
