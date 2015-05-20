package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.ppa.predict.PPADisambiguator;
import edu.cmu.ml.rtw.AnnotationVerb;

public class PipelineNLPMicro extends PipelineNLP {

  public PipelineNLPMicro() {
    super();

    /*
     * Initialize micro-readers here
     */
    NELLMentionCategorizer mentionCategorizer = new NELLMentionCategorizer();
    AnnotationVerb annotationVerb = new AnnotationVerb();
    PPADisambiguator ppa = new PPADisambiguator();

    /*
     * Add micro-readers to the pipeline here
     */
    addAnnotator(mentionCategorizer.produces(), mentionCategorizer);
    addAnnotator(annotationVerb.produces(), annotationVerb);
    addAnnotator(ppa.produces(), ppa);
  }
}
