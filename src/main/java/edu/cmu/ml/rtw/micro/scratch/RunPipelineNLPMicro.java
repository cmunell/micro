package edu.cmu.ml.rtw.micro.scratch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Language;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.SerializerDocumentNLPBSON;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.SerializerDocumentNLPHTML;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.SerializerDocumentNLPMicro;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPStanford;
import edu.cmu.ml.rtw.generic.util.FileUtil;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.micro.data.MicroDataTools;
import edu.cmu.ml.rtw.micro.model.annotation.nlp.PipelineNLPMicro;


public class RunPipelineNLPMicro {
	public enum OutputType {
		MICRO,
		BSON, 
		HTML,
		HTML_NELL_ONLY
	}
	
	public static final int DEFAULT_MAX_ANNOTATION_SENTENCE_LENGTH = 30;

	private static OutputType outputType;
	private static int maxThreads;
	private static File outputDataDir;
	private static File inputDataPath;
        private static boolean skipIfOutputExists = false;
        private static Integer numInputs = null;
        private static Integer numOutputs = 0;
        private static boolean kbpFormat = false;
	
	private static int maxAnnotationSentenceLength;
	private static PipelineNLPMicro microPipeline;
	private static MicroDataTools dataTools;
	
	public static void main(String[] args) {		
		if (!parseArgs(args))
			return;

		List<File> files = new ArrayList<File>();
                if (inputDataPath.isFile()) {
                    files.add(inputDataPath);
                } else if (inputDataPath.isDirectory()) {
                    for (File f : inputDataPath.listFiles()) files.add(f);
                } else {
                    throw new RuntimeException("Error: Input soecification '" + inputDataPath + "' is niether a file nor a directory!");
                }
                
                final PipelineNLPStanford stanfordPipeline = new PipelineNLPStanford(maxAnnotationSentenceLength);
                if (kbpFormat) {
                    // These settings used for 2016 KBP (up to and including LDC2015E77)
                    Properties props = new Properties();
                    props.put("clean.xmltags","HEADLINE|POSTER|TURN|P|TEXT|headline|quote|post");
                    props.put("clean.sentenceendingtags","P|HEADLINE|TEXT|POSTDATE|POST|POSTER|headline|post");
                    props.put("clean.allowflawedxml","true");
                    props.put("clean.datetags","datetime|date|dateline");
                    props.put("clean.docIdtags","id|   id|DOCID");
                    props.put("clean.docTypetags","type|DOCTYPE");
                    props.put("clean.turntags","turn");
                    props.put("clean.speakertags","speaker|POSTER|AUTHOR");
                    props.put("clean.docAnnotations","docID=doc[id],doctype=doc[type],docsourcetype=doctype[source]");
                    stanfordPipeline.initialize(null, null, null, null, props, true);
                } else {
                    stanfordPipeline.initialize();
                }

                ThreadMapper<File, Boolean> threads = new ThreadMapper<File, Boolean>(new ThreadMapper.Fn<File, Boolean>() {

                        public Boolean apply(File inFile) {
                            try {
                                String outFileName;
                                if (outputType == OutputType.MICRO) {
                                    outFileName = inFile.getName() + ".json";
                                } else if (outputType == OutputType.HTML || outputType == OutputType.HTML_NELL_ONLY) {
                                    outFileName = inFile.getName() + ".html";
                                } else {
                                    outFileName = inFile.getName() + ".bson";
                                }
                                File outFile = new File(outputDataDir, outFileName);
                                if (skipIfOutputExists) {
                                    if (outFile.isFile() && outFile.length() > 0) {
                                        dataTools.getOutputWriter().debugWriteln("Skipping " + inFile + " due to pre-existing output file " + outFile);
                                        return true;
                                    }
                                }

                                dataTools.getOutputWriter().debugWriteln("Processing " + inFile + "...");
				PipelineNLPMicro threadMicroPipeline = new PipelineNLPMicro(microPipeline);
				PipelineNLP pipeline = stanfordPipeline.weld(threadMicroPipeline);

                                // Not clear what's to do exactly with document ID construction.
                                // But for KBP, it's expedient to base it on the file name because
                                // the files are named by the document ID exactly.
                                String docID = inFile.getName();
                                if (kbpFormat) {
                                    docID = docID.replaceAll(".xml", "");
                                }
                                DocumentNLPMutable document = new DocumentNLPInMemory(dataTools, docID, FileUtil.readFile(inFile));
                                pipeline.run(document);
				
                                if (outputType == OutputType.MICRO) {
                                    SerializerDocumentNLPMicro microSerial = new SerializerDocumentNLPMicro(dataTools);
                                    FileUtil.writeFile(outFile, microSerial.serializeToString(document));
                                } else if (outputType == OutputType.BSON) {
                                    SerializerDocumentNLPBSON bsonSerial = new SerializerDocumentNLPBSON(dataTools);
                                    FileUtil.writeFile(outFile, bsonSerial.serializeToString(document));
                                } else if (outputType == OutputType.HTML) {
                                    SerializerDocumentNLPHTML htmlSerial = new SerializerDocumentNLPHTML(dataTools);
                                    FileUtil.writeFile(outFile, htmlSerial.serializeToString(document));
                                } else if (outputType == OutputType.HTML_NELL_ONLY) {
                                    throw new RuntimeException("TODO: NELL-Only mode");
				}
				
                                synchronized (numOutputs) { 
                                    numOutputs++;
                                }
				return true;
                            } catch (Throwable e) {
                                StringWriter errors = new StringWriter();
                                e.printStackTrace(new PrintWriter(errors));
                                String trace = errors.toString();
                                dataTools.getOutputWriter().debugWriteln("ERROR: caught exception while processing " + inFile + ": " + e.getMessage() + "\n" + trace);
                                throw new RuntimeException("apply(" + inFile + ")", e);
                            }
			}
		});

                numInputs = files.size();
		List<Boolean> results = threads.run(files, maxThreads);
		for (Boolean result : results)
			if (!result)
				dataTools.getOutputWriter().debugWriteln("ERROR: Failed to run document through pipeline.");
		
		dataTools.getOutputWriter().debugWriteln("Finished running documents through pipeline.");
                dataTools.getOutputWriter().debugWriteln("Processed " + numInputs + " input files and generated " + numOutputs + " output files.");
	}
	
	private static boolean parseArgs(String[] args) {
		OutputWriter output = new OutputWriter();
		OptionParser parser = new OptionParser();
		
		parser.accepts("outputType").withRequiredArg()
			.describedAs("BSON, MICRO, HTML, HTML_NELL_ONLY determines whether output data is stored as bson object, micro-reading json format, or html")
			.defaultsTo("BSON");
		parser.accepts("maxThreads").withRequiredArg()
			.describedAs("Maximum number of concurrent threads to use when annotating files")
			.ofType(Integer.class)
			.defaultsTo(1);
		parser.accepts("inputDataPath").withRequiredArg()
			.describedAs("Path to input file or directory containing text files on which to run")
			.ofType(File.class);
		parser.accepts("outputDataDir").withRequiredArg()
			.describedAs("Path to directory where output should be stored")
			.ofType(File.class);
		parser.accepts("maxAnnotationSentenceLength").withRequiredArg()
			.describedAs("Maximum length of sentences that are considered when parsing the document")
			.ofType(Integer.class)
			.defaultsTo(DEFAULT_MAX_ANNOTATION_SENTENCE_LENGTH);
		parser.accepts("outputDebugFile").withRequiredArg()
			.describedAs("Optional path to debug output file")
			.ofType(File.class);
                parser.accepts("skipIfOutputExists").withRequiredArg()
                        .describedAs("Skip input documents for which the output file already exists")
                        .ofType(Boolean.class)
                        .defaultsTo(false);
                parser.accepts("kbpFormat").withRequiredArg()
                        .describedAs("Assume input is in KBP XML format rather than plaintext")
                        .ofType(Boolean.class)
                        .defaultsTo(false);
                        
		parser.accepts("nounPhraseMentionModelThreshold").withRequiredArg()
			.describedAs("The context dependent mention categorization models assign categories to a " +
						 "noun-phrase when NELL's confidence about the noun-phrase's category is below this " +
						 "threshold.")
			.ofType(Double.class)
			.defaultsTo(NELLMentionCategorizer.DEFAULT_MENTION_MODEL_THRESHOLD);
		
		parser.accepts("disableCat").withRequiredArg()
			.describedAs("Disable micro-cat")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableHdp").withRequiredArg()
			.describedAs("Disable hdp parser")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableVerb").withRequiredArg()
			.describedAs("Disable verb annotator")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableSem").withRequiredArg()
			.describedAs("Disable semantic parser")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disablePpa").withRequiredArg()
			.describedAs("Disable prepositional phrase attachment")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableNominal").withRequiredArg()
			.describedAs("Disable nominals")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableRegex").withRequiredArg()
			.describedAs("Disable regex")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableEvent").withRequiredArg()
			.describedAs("Disable event extractor")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableOpinion").withRequiredArg()
			.describedAs("Disable opinion extractor")
			.ofType(Boolean.class)
			.defaultsTo(false);
		parser.accepts("disableOOCCat").withRequiredArg()
			.describedAs("Disable micro-ooccat")
			.ofType(Boolean.class)
			.defaultsTo(false);
		
		parser.accepts("help").forHelp();
		
		OptionSet options = parser.parse(args);
		
		if (options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				return false;
			}
			return false;
		}
		
		output.debugWriteln("Loading data tools (gazetteers etc)...");
		dataTools = new MicroDataTools(output);
		output.debugWriteln("Finished loading data tools.");
		
		outputType = OutputType.valueOf(options.valueOf("outputType").toString());
		maxThreads = (Integer)options.valueOf("maxThreads");
		
		if (options.has("inputDataPath")) {
			inputDataPath = (File)options.valueOf("inputDataPath");
		} else {
			dataTools.getOutputWriter().debugWriteln("ERROR: Missing 'inputDataPath' argument.");
			return false;
		}
		
		if (options.has("outputDataDir")) {
			outputDataDir = (File)options.valueOf("outputDataDir");
		} else {
			dataTools.getOutputWriter().debugWriteln("ERROR: Missing 'outputDataDir' argument.");
			return false;
		}
		
		maxAnnotationSentenceLength = (Integer)options.valueOf("maxAnnotationSentenceLength");
		
		if (options.has("outputDebugFile")) {
			dataTools.getOutputWriter().setDebugFile((File)options.valueOf("outputDebugFile"), false);
		}
		
		List<PipelineNLPMicro.Annotator> disabledAnnotators = new ArrayList<PipelineNLPMicro.Annotator>();
		if ((Boolean)options.valueOf("disableCat"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.NP_CATEGORIZER);
		if ((Boolean)options.valueOf("disableHdp"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.HDP_PARSER);
		if ((Boolean)options.valueOf("disableVerb"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.VERB_ANNOTATOR);
		if ((Boolean)options.valueOf("disableSem"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.SEMANTIC_PARSER);
		if ((Boolean)options.valueOf("disablePpa"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.PPA_DISAMBIGUATOR);
		if ((Boolean)options.valueOf("disableNominal"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.NOMINALRELATIONS);
		if ((Boolean)options.valueOf("disableRegex"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.REGEX_EXTRACTOR);
		if ((Boolean)options.valueOf("disableEvent"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.EVENT_EXTRACTOR);
		if ((Boolean)options.valueOf("disableOpinion"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.OPINION_EXTRACTOR);
		if ((Boolean)options.valueOf("disableOOCCat"))
			disabledAnnotators.add(PipelineNLPMicro.Annotator.CONTEXTLESS_NP_CATEGORIZER);
		
		microPipeline = new PipelineNLPMicro((Double)options.valueOf("nounPhraseMentionModelThreshold"), disabledAnnotators);
	
		return true;
	}
}
