package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import java.util.List;

//import org.junit.Test;


import org.junit.Test;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Language;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.micro.Annotation;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPStanford;
import edu.cmu.ml.rtw.micro.data.MicroDataTools;

public class PipelineNLPMicroTest {
	@Test
	public void testDocument() {
		PipelineNLPStanford stanfordPipe = new PipelineNLPStanford();
		PipelineNLPMicro microPipe = new PipelineNLPMicro();
		PipelineNLP stanfordMicroPipe = stanfordPipe.weld(microPipe);
		DocumentNLP document = new DocumentNLPInMemory(new MicroDataTools(), 
													   "Test document", 
													   "I baked a cake in the oven.  Barack Obama helped because I was " +
													   "the deciding vote in the next presidential election.",
													   Language.English, stanfordMicroPipe);
	
		List<Annotation> annotations = document.toMicroAnnotation().getAllAnnotations();
		for (Annotation annotation : annotations)
			System.out.println(annotation.toJsonString());
	
	}
}
