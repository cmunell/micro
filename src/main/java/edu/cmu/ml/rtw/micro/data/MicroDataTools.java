package edu.cmu.ml.rtw.micro.data;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.AnnotationTypeNLPCat;
<<<<<<< HEAD
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
    this.addAnnotationTypeNLP(PPADisambiguator.PPA_DISAMBIG);
  }
=======
import edu.cmu.ml.rtw.micro.model.annotator.semparse.SemparseAnnotatorSentence;
import edu.cmu.ml.rtw.AnnotationVerb;

public class MicroDataTools extends DataTools {
	public MicroDataTools() {
		this(new OutputWriter());
	}
	
	public MicroDataTools(OutputWriter outputWriter) {
		super(outputWriter);		
		
		/* Add micro-reading annotation types here */
		this.addAnnotationTypeNLP(AnnotationTypeNLPCat.NELL_CATEGORY);
    this.addAnnotationTypeNLP(SemparseAnnotatorSentence.LOGICAL_FORM_ANNOTATION_TYPE);
		this.addAnnotationTypeNLP(AnnotationVerb.NELL_VERB);
	}
>>>>>>> refs/remotes/origin/master
}
