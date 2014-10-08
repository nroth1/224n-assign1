package edu.stanford.nlp.mt.decoder.feat;

import java.util.List;

import edu.stanford.nlp.mt.util.FeatureValue;
import edu.stanford.nlp.mt.util.Featurizable;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.mt.decoder.feat.RuleFeaturizer;
import edu.stanford.nlp.util.Generics;

/**
 * A rule featurizer.
 */
public class LengthDiff implements RuleFeaturizer<IString, String> {
  
  @Override
  public void initialize() {
    // Do any setup here.
  }

  @Override
  public List<FeatureValue<String>> ruleFeaturize(
      Featurizable<IString, String> f) {

    // TODO: Return a list of features for the rule. Replace these lines
    // with your own feature.
    List<FeatureValue<String>> features = Generics.newLinkedList();
    int diff = Math.abs(f.targetPhrase.size() - f.sourcePhrase.size());
    int category = 0;
    if (diff == 0) category = 0;
    if (diff > 1) category = 2;
    if (diff > 2) category = 3;
    if (diff <= 1 && diff > 0) category = 1;
//    features.add(new FeatureValue<String>("LengthDiff", Math.abs(f.targetPhrase.size() - f.sourcePhrase.size())));
    features.add(new FeatureValue<String>("LengthDiff", category));
    return features;
  }

  @Override
  public boolean isolationScoreOnly() {
    return false;
  }
}
