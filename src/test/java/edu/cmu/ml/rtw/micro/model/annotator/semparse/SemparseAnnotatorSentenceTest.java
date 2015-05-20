package edu.cmu.ml.rtw.micro.model.annotator.semparse;

import java.util.List;

import org.junit.Test;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Language;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.micro.Annotation;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPExtendable;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPStanford;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.generic.data.DataTools;

public class SemparseAnnotatorSentenceTest {

	@Test
	public void testDocument() {
    PipelineNLPStanford pipelineStanford = new PipelineNLPStanford();
    PipelineNLPExtendable pipelineExtendable = new PipelineNLPExtendable();

    pipelineExtendable.extend(new NELLMentionCategorizer());
    SemparseAnnotatorSentence semanticParser = SemparseAnnotatorSentence.fromSerializedModels("src/main/resources/parser.ser", "src/main/resources/supertagger.ser");
    pipelineExtendable.extend(semanticParser);
    PipelineNLP pipeline = pipelineStanford.weld(pipelineExtendable);
    DataTools dataTools = new DataTools();
    dataTools.addAnnotationTypeNLP(SemparseAnnotatorSentence.LOGICAL_FORM_ANNOTATION_TYPE);
    DocumentNLP document = new DocumentNLPInMemory(dataTools, 
                                                   "Test document", 
                                                   "Barack Obama is the president of the United States. Madonna who was born in Bay City, Michigan. " +
                                                   "Larry Page founded Google. Google was founded by Larry Page and Sergey Brin.",
                                                   Language.English, pipeline);
    List<Annotation> annotations = document.toMicroAnnotation().getAllAnnotations();
    for (Annotation annotation : annotations) {
      System.out.println(annotation.toJsonString());
    }
  }
}