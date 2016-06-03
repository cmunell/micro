package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Language;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.SerializerDocumentNLPMicro;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.micro.Annotation;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPStanford;
import edu.cmu.ml.rtw.micro.data.MicroDataTools;

public class PipelineNLPMicroTest {

  @Test
  public void testDocument() {
    try {
    PipelineNLPStanford stanfordPipe = new PipelineNLPStanford(30);
    stanfordPipe.initialize();

    List<PipelineNLPMicro.Annotator> disabledAnnotators = new ArrayList<PipelineNLPMicro.Annotator>();
    if (false) {  // These are for debugging
        disabledAnnotators.add(PipelineNLPMicro.Annotator.NP_CATEGORIZER);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.CONTEXTLESS_NP_CATEGORIZER);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.VERB_ANNOTATOR);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.SEMANTIC_PARSER);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.HDP_PARSER);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.PPA_DISAMBIGUATOR);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.NOMINALRELATIONS);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.REGEX_EXTRACTOR);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.EVENT_EXTRACTOR);
        disabledAnnotators.add(PipelineNLPMicro.Annotator.OPINION_EXTRACTOR);
    }
    PipelineNLPMicro microPipe = new PipelineNLPMicro(disabledAnnotators);
    PipelineNLP stanfordMicroPipe = stanfordPipe.weld(microPipe);
    SerializerDocumentNLPMicro microSerial = new SerializerDocumentNLPMicro(new MicroDataTools());
    System.out.println("Annotating Test Document 1");
    DocumentNLPMutable document = new DocumentNLPInMemory(new MicroDataTools(), "Test document 1",
        "I baked a cake in the oven.  Barack Obama helped because I was " + "the deciding vote in the next United States presidential election.");

    stanfordMicroPipe.run(document);
    List<Annotation> annotations = microSerial.serialize(document).getAllAnnotations();
    for (Annotation annotation : annotations)
      System.out.println(annotation.toJsonString());

    System.out.println("Annotating Test Document 2");
    document = new DocumentNLPInMemory(new MicroDataTools(), "Test document 2", "Ernest Hemingway was born in the United States in 1899.");

    stanfordMicroPipe.run(document);
    annotations = microSerial.serialize(document).getAllAnnotations();
    for (Annotation annotation : annotations)
      System.out.println(annotation.toJsonString());

    System.out.println("Annotating Test Document 3, PPA examples");
    document = new DocumentNLPInMemory(new MicroDataTools(), "Test document 3",
        "John  ate salad with a fork. Bob cooked rice in March 1945. Google bought  20% of  Youtube.");

    stanfordMicroPipe.run(document);
    annotations = microSerial.serialize(document).getAllAnnotations();
    for (Annotation annotation : annotations)
      System.out.println(annotation.toJsonString());

    System.out.println("Annotating Test Document 4, SVO (ish) examples");
    document = new DocumentNLPInMemory(new MicroDataTools(), "Test document 4",
        "Butterflies look like moths. Vegetables protect against heart attack.");

    stanfordMicroPipe.run(document);
    annotations = microSerial.serialize(document).getAllAnnotations();
    for (Annotation annotation : annotations)
      System.out.println(annotation.toJsonString());

    System.out.println("Annotating Test Document 5,  NOMinals examples");
    document = new DocumentNLPInMemory(new MicroDataTools(), "Test document 5",
        "Chinese  businessman Kenny Huang borrowed money. World Cup captain Philipp Lahm scored. CNN   host  Rick Sanchez reported.");

    stanfordMicroPipe.run(document);
    annotations = microSerial.serialize(document).getAllAnnotations();
    for (Annotation annotation : annotations)
      System.out.println(annotation.toJsonString());
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
  }
}
