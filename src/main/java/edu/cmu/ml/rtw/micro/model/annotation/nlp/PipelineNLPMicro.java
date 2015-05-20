package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.micro.model.annotator.semparse.SemparseAnnotatorSentence;

public class PipelineNLPMicro extends PipelineNLP {
	public PipelineNLPMicro() {
		super();
		
		/*
		 * Initialize micro-readers here
		 */
		NELLMentionCategorizer mentionCategorizer = new NELLMentionCategorizer();
    SemparseAnnotatorSentence semanticParser = SemparseAnnotatorSentence.fromSerializedModels("src/main/resources/parser.ser", "src/main/resources/supertagger.ser");

		/*
		 * Add micro-readers to the pipeline here
		 */
		addAnnotator(mentionCategorizer.produces(), mentionCategorizer);
		addAnnotator(semanticParser.produces(), semanticParser);
	}
}
