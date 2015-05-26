package edu.cmu.ml.rtw.micro.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.ml.rtw.AnnotationVerb;
import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.AnnotationTypeNLPCat;
import edu.cmu.ml.rtw.micro.hdp.HDPParser;
import edu.cmu.ml.rtw.micro.model.annotator.semparse.SemparseAnnotatorSentence;
import edu.cmu.ml.rtw.ppa.predict.PPADisambiguator;

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

  /**
   * Return all NELL-specific annotation types.  This is used when we want to just show the NELL
   * annotations in a visualization, for instance.
   */
  public Collection<AnnotationTypeNLP<?>> getNellAnnotationTypesNLP() {
    List<AnnotationTypeNLP<?>> annotationTypes = new ArrayList<AnnotationTypeNLP<?>>();
    annotationTypes.add(AnnotationTypeNLPCat.NELL_CATEGORY);
    annotationTypes.add(AnnotationVerb.NELL_VERB);
    annotationTypes.add(SemparseAnnotatorSentence.LOGICAL_FORM_ANNOTATION_TYPE);
    annotationTypes.add(PPADisambiguator.PPA_DISAMBIG);
    return annotationTypes;
  }
}
