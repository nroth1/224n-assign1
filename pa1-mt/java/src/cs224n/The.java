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
public class The implements RuleFeaturizer<IString, String> {
  
  @Override
  public void initialize() {
    // Do any setup here.
  }

  @Override
  public List<FeatureValue<String>> ruleFeaturize(
      Featurizable<IString, String> f) {

    // TODO: Return a list of features for the rule. Replace these lines
    // with your own feature.
    int count = 0;
    for (int i = 0; i < f.targetPhrase.size(); ++i) {
      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("the")) count++;
    }
    List<FeatureValue<String>> features = Generics.newLinkedList();
    features.add(new FeatureValue<String>("The", (double) count / (double) f.targetPhrase.size()));
    return features;
  }

  @Override
  public boolean isolationScoreOnly() {
    return false;
  }
}
