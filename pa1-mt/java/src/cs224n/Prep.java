package edu.stanford.nlp.mt.decoder.feat;

import java.util.List;
import java.lang.String;
import java.util.Arrays;
import java.util.ArrayList;

import edu.stanford.nlp.mt.util.FeatureValue;
import edu.stanford.nlp.mt.util.Featurizable;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.mt.decoder.feat.RuleFeaturizer;
import edu.stanford.nlp.util.Generics;

/**
 * A rule featurizer.
 */
public class Prep implements RuleFeaturizer<IString, String> {
  
  @Override
  public void initialize() {
    // Do any setup here.
  }

  @Override
  public List<FeatureValue<String>> ruleFeaturize(
      Featurizable<IString, String> f) {

    // TODO: Return a list of features for the rule. Replace these lines
    // with your own feature.
    List<String> preps = new ArrayList<String>(Arrays.asList("of", "in", "to", "for", "with", "on", "at", "from", "by", "by", "about", "as", "into", "like", "through", "after", "over", "between", "out", "against", "during", "without", "before", "under", "around", "among"));
//    int in = 0;
//    int  = 0;
//    int countComma = 0;
//    int countQuote = 0;
//    int countQuestionMark = 0;
//    int leftPar = 0;
//    int rightPar = 0;
//    int semi = 0;
//    int colon = 0;
//    int dollar = 0;
    int masterCount = 0;
    for (int i = 0; i < f.targetPhrase.size(); i++) {
      if (preps.contains(f.targetPhrase.get(i).toString())) masterCount++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("\"")) countQuote++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase(",")) countComma++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("\'")) countApostrophe++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("(")) leftPar++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase(")")) rightPar++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("?")) countQuestionMark++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase(";")) semi++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase(":")) colon++;
//      if (f.targetPhrase.get(i).toString().equalsIgnoreCase("$")) dollar++;

    }
//    for (int i = 0; i < f.sourcePhrase.size(); i++) {
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(".")) after--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase("\'")) countApostrophe--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(",")) countComma--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase("\"")) countQuote--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase("(")) leftPar--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(")")) rightPar--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase("?")) countQuestionMark--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(";")) semi--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase(":")) colon--;
//      if (f.sourcePhrase.get(i).toString().equalsIgnoreCase("$")) dollar--;
//    }
//    int masterCount = 1;
//    if (after != 0) masterCount *= 0;
//    if (countQuote != 0) masterCount *= 0;
//    if (countComma != 0) masterCount *= 0;
//    if (countApostrophe != 0) masterCount *= 0;
//    if (countQuestionMark != 0) masterCount *= 0;
//    if (leftPar != 0) masterCount *= 0;
//    if (rightPar != 0) masterCount *= 0;
//    if (semi != 0) masterCount *= 0;
//    if (colon != 0) masterCount *= 0;
//    if (dollar != 0) masterCount *= 0;

//    int masterCount = 0;
//    String punc = ".,();:%$!\'\"<>#";
//    if (f.sourcePhrase.size() == 1 && f.targetPhrase.size() == 1) {
//      if (punc.contains(f.sourcePhrase.get(0).toString().trim())) {
//        if (f.sourcePhrase.get(0).toString().equalsIgnoreCase(f.targetPhrase.get(0).toString())) masterCount = 1;
//      }
//    }
    
    List<FeatureValue<String>> features = Generics.newLinkedList();
    features.add(new FeatureValue<String>("Prep", masterCount > 0 ? 1.0 : 0.0));
    return features;
  }

  @Override
  public boolean isolationScoreOnly() {
    return false;
  }
}
