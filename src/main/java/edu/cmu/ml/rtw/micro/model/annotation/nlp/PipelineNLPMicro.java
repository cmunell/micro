package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ml.rtw.contextless.ContextlessNPCategorizer;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.micro.hdp.HDPParser;
import edu.cmu.ml.rtw.micro.cat.data.CatDataTools;
import edu.cmu.ml.rtw.micro.cat.data.annotation.CategoryList;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.micro.sem.model.annotation.nlp.SemparseAnnotatorSentence;
import edu.cmu.ml.rtw.nominals.NominalsReader;
import edu.cmu.ml.rtw.ppa.predict.PPADisambiguator;
import edu.cmu.ml.rtw.users.ssrivastava.RegexExtractor;
import edu.cmu.ml.rtw.AnnotationVerb;
import edu.cmu.ml.rtw.micro.event.EventExtractor;
import edu.cmu.ml.rtw.micro.opinion.OpinionExtractor;

public class PipelineNLPMicro extends PipelineNLP {
	public enum Annotator {
		NP_CATEGORIZER,
		VERB_ANNOTATOR,
		SEMANTIC_PARSER,
		HDP_PARSER,
		PPA_DISAMBIGUATOR,
		NOMINALRELATIONS,
                REGEX_EXTRACTOR,
                EVENT_EXTRACTOR,
                OPINION_EXTRACTOR,
                CONTEXTLESS_NP_CATEGORIZER
	}
	
	public PipelineNLPMicro() {
		this(NELLMentionCategorizer.DEFAULT_MENTION_MODEL_THRESHOLD, null);
	}
	
	public PipelineNLPMicro(List<Annotator> disabledAnnotators) {
		this(NELLMentionCategorizer.DEFAULT_MENTION_MODEL_THRESHOLD, disabledAnnotators);
	}
	
	public PipelineNLPMicro(double nounPhraseMentionModelThreshold) {
		this(nounPhraseMentionModelThreshold, null);
	}
	
	public PipelineNLPMicro(double nounPhraseMentionModelThreshold, List<Annotator> disabledAnnotators) {
		super();

		if (disabledAnnotators == null)
			disabledAnnotators = new ArrayList<Annotator>();
		
		/*
		 * Initialize and add micro-readers here
		 */
		if (disabledAnnotators.contains(Annotator.NP_CATEGORIZER) && false)  //bkdb
			throw new IllegalArgumentException("Cannot disable noun-phrase categorizer.");

                if (!disabledAnnotators.contains(Annotator.NP_CATEGORIZER)) {
                    NELLMentionCategorizer mentionCategorizer = new NELLMentionCategorizer(
                            new CategoryList(CategoryList.Type.ALL_NELL_CATEGORIES, new CatDataTools()), 
                            nounPhraseMentionModelThreshold, NELLMentionCategorizer.DEFAULT_LABEL_TYPE, 
                            20);
                    addAnnotator(mentionCategorizer.produces(), mentionCategorizer);
                }
		
	   if (!disabledAnnotators.contains(Annotator.CONTEXTLESS_NP_CATEGORIZER)) {
	      ContextlessNPCategorizer contextlessNPCategorizer = new ContextlessNPCategorizer();
	      addAnnotator(contextlessNPCategorizer.produces(), contextlessNPCategorizer);
	    }

		if (!disabledAnnotators.contains(Annotator.SEMANTIC_PARSER)) {
			SemparseAnnotatorSentence semanticParser = SemparseAnnotatorSentence.fromSerializedModels(SemparseAnnotatorSentence.PARSER_MODEL_PATH, SemparseAnnotatorSentence.SUPERTAGGER_MODEL_PATH);
			addAnnotator(semanticParser.produces(), semanticParser);		
		}
		
		if (!disabledAnnotators.contains(Annotator.VERB_ANNOTATOR)) {
			AnnotationVerb annotationVerb = new AnnotationVerb();
			addAnnotator(annotationVerb.produces(), annotationVerb);
		}
		
		if (!disabledAnnotators.contains(Annotator.PPA_DISAMBIGUATOR)) {
			PPADisambiguator ppa = new PPADisambiguator();	
			addAnnotator(ppa.produces(), ppa);
		}
		
		if (!disabledAnnotators.contains(Annotator.HDP_PARSER)) {
			HDPParser hdpParser = HDPParser.getInstance();
			addAnnotator(hdpParser.produces(), hdpParser);
		}

    if (!disabledAnnotators.contains(Annotator.NOMINALRELATIONS)) {
      NominalsReader nominalsRelationExtractor = new NominalsReader();
      addAnnotator(nominalsRelationExtractor.produces(), nominalsRelationExtractor);
    }

		if (!disabledAnnotators.contains(Annotator.REGEX_EXTRACTOR)) {
			RegexExtractor regex = new RegexExtractor();
			addAnnotator(regex.produces(), regex);
		}

		if (!disabledAnnotators.contains(Annotator.EVENT_EXTRACTOR)) {
			EventExtractor event = EventExtractor.getInstance();
			addAnnotator(event.produces(), event);
		}

		if (!disabledAnnotators.contains(Annotator.OPINION_EXTRACTOR)) {
                    OpinionExtractor opinion = OpinionExtractor.getInstance();
			addAnnotator(opinion.produces(), opinion);
		}
	}
	
	public PipelineNLPMicro(PipelineNLPMicro pipeline) {
		this.annotationOrder = pipeline.annotationOrder;
		this.annotators = pipeline.annotators;
	}
}
