package edu.stanford.nlp.mt.decoder.feat;

import java.util.List;
import java.lang.String;

import edu.stanford.nlp.mt.util.FeatureValue;
import edu.stanford.nlp.mt.util.Featurizable;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.mt.decoder.feat.RuleFeaturizer;
import edu.stanford.nlp.util.Generics;

/**
 * A rule featurizer.
 */
public class Punc implements RuleFeaturizer<IString, String> {
  
  @Override
  public void initialize() {
    // Do any setup here.
  }

  @Override
  public List<FeatureValue<String>> ruleFeaturize(
      Featurizable<IString, String> f) {

    int countPeriod = 0;
    int countQuote = 0;
    int countComma = 0;
    //count punctuation in target
    for (int i = 0; i < f.targetPhrase.size(); i++) {
      if (f.targetPhrase.get(i).toString().equalsIgnoreCase(".")) countPeriod++;
      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("\'")) countQuote++;
      if (f.targetPhrase.get(i).toString().equalsIgnoreCase(",")) countComma++;
    }
    //count punctuation in source
    for (int i = 0; i < f.sourcePhrase.size(); i++) {
      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(".")) countPeriod--;
      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase("\'")) countQuote--;
      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(",")) countComma--;
    }

    //set mastercount to 1 if everything is the same, and 0 elsewise. 
    int masterCount = 1;
    if (countPeriod != 0) masterCount *= 0;
    if (countQuote != 0) masterCount *= 0;
    if (countComma != 0) masterCount *= 0;
   
    List<FeatureValue<String>> features = Generics.newLinkedList();
    features.add(new FeatureValue<String>("Punc", masterCount));
    return features;
  }

  @Override
  public boolean isolationScoreOnly() {
    return false;
  }
}
