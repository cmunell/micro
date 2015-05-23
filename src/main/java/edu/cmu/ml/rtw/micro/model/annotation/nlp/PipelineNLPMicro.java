package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.micro.hdp.HDPParser;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.micro.model.annotator.semparse.SemparseAnnotatorSentence;
import edu.cmu.ml.rtw.ppa.predict.PPADisambiguator;
import edu.cmu.ml.rtw.AnnotationVerb;

public class PipelineNLPMicro extends PipelineNLP {
	public PipelineNLPMicro() {
		super();

		/*
		 * Initialize micro-readers here
		 */
		NELLMentionCategorizer mentionCategorizer = new NELLMentionCategorizer();
		SemparseAnnotatorSentence semanticParser = SemparseAnnotatorSentence.fromSerializedModels(SemparseAnnotatorSentence.PARSER_MODEL_PATH, SemparseAnnotatorSentence.SUPERTAGGER_MODEL_PATH);
		AnnotationVerb annotationVerb = new AnnotationVerb();
		PPADisambiguator ppa = new PPADisambiguator();
		HDPParser hdpParser = HDPParser.getInstance();
		
		/*
		 * Add micro-readers to the pipeline here
		 */
		addAnnotator(mentionCategorizer.produces(), mentionCategorizer);
		addAnnotator(semanticParser.produces(), semanticParser);
		addAnnotator(annotationVerb.produces(), annotationVerb);
		addAnnotator(ppa.produces(), ppa);
		addAnnotator(hdpParser.produces(), hdpParser);
	}
	
	public PipelineNLPMicro(PipelineNLPMicro pipeline) {
		this.annotationOrder = pipeline.annotationOrder;
		this.annotators = pipeline.annotators;
		this.document = pipeline.document;
	}
}
