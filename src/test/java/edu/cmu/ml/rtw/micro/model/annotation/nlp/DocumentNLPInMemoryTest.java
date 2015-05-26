package edu.cmu.ml.rtw.micro.model.annotation.nlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import edu.cmu.ml.rtw.micro.data.MicroDataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.micro.DocumentAnnotation;


public class DocumentNLPInMemoryTest extends TestCase {

  // This was what I used when developing a little demo.  It's not really a test at this point...
  public void testHtmlOutput() throws IOException {
    MicroDataTools dataTools = new MicroDataTools();
    // NOTE: as of 5/26/2015, the annotator and slot names in this demo output are out of date, and
    // so not all of the annotations show up in the output.
    String annotationFile = "src/test/resources/micro-demo-output.json";
    String originalTextFile = "src/test/resources/micro-demo-input.txt";
    String originalText = "";
    BufferedReader reader = new BufferedReader(new FileReader(originalTextFile));
    String line;
    while ((line = reader.readLine()) != null) {
      originalText += line + "\n";
    }
    reader.close();
    DocumentAnnotation annotation = DocumentAnnotation.fromFile(annotationFile).get(0);
    DocumentNLP document = new DocumentNLPInMemory(dataTools);
    document.fromMicroAnnotation(annotation, originalText);
    String htmlString = document.toHtmlString(dataTools.getNellAnnotationTypesNLP());

    // TODO(matt): test something here, instead of outputting to file
    FileWriter writer = new FileWriter("src/test/resources/micro-demo-output.html");
    writer.write(htmlString);
    writer.close();
  }
}
