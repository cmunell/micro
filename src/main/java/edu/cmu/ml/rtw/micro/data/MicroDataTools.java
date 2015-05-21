package edu.cmu.ml.rtw.micro.data;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.AnnotationTypeNLPCat;
import edu.cmu.ml.rtw.micro.hdp.HDPParser;
import edu.cmu.ml.rtw.micro.model.annotator.semparse.SemparseAnnotatorSentence;
import edu.cmu.ml.rtw.ppa.predict.PPADisambiguator;
import edu.cmu.ml.rtw.AnnotationVerb;

public class MicroDataTools extends DataTools {

  public MicroDataTools() {
    this(new OutputWriter());
  }

  public MicroDataTools(OutputWriter outputWriter) {
    super(outputWriter);

    /* Add micro-reading annotation types here */
    this.addAnnotationTypeNLP(AnnotationTypeNLPCat.NELL_CATEGORY);
    this.addAnnotationTypeNLP(AnnotationVerb.NELL_VERB);
    this.addAnnotationTypeNLP(SemparseAnnotatorSentence.LOGICAL_FORM_ANNOTATION_TYPE);
    this.addAnnotationTypeNLP(PPADisambiguator.PPA_DISAMBIG);
    this.addAnnotationTypeNLP(HDPParser.SEMANTIC_PARSE);
  }
}
