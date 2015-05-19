package edu.cmu.ml.rtw.micro.scratch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentSetNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Language;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPStanford;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.cmu.ml.rtw.micro.data.MicroDataTools;
import edu.cmu.ml.rtw.micro.model.annotation.nlp.PipelineNLPMicro;


public class RunPipelineNLPMicro {
	public enum OutputType {
		MICRO,
		JSON
	}
	
	public static final int DEFAULT_MIN_ANNOTATION_SENTENCE_LENGTH = 2;
	public static final int DEFAULT_MAX_ANNOTATION_SENTENCE_LENGTH = 30;
	
	private static OutputType outputType;
	private static int maxThreads;
	private static File outputDataDir;
	private static File inputDataPath;
	
	private static PipelineNLPStanford stanfordPipeline;
	private static PipelineNLPMicro microPipeline;
	private static MicroDataTools dataTools;
	
	public static void main(String[] args) {		
		if (!parseArgs(args))
			return;
		
		final DocumentSetNLP<DocumentNLPInMemory> documentSet = DocumentSetNLP.loadFromTextPathThroughPipeline("", Language.English, inputDataPath.getAbsolutePath(), new DocumentNLPInMemory(dataTools));
		
		ThreadMapper<String, Boolean> threads = new ThreadMapper<String, Boolean>(new ThreadMapper.Fn<String, Boolean>() {
			public Boolean apply(String documentName) {
				File outputFile = new File(outputDataDir, documentName);
				
				dataTools.getOutputWriter().debugWriteln("Processing file " + documentName + "...");
				
				PipelineNLPStanford threadStanfordPipeline = new PipelineNLPStanford(stanfordPipeline);
				PipelineNLP pipeline = threadStanfordPipeline.weld(microPipeline);
				
				DocumentNLP inputDocument = documentSet.getDocumentByName(documentName, false);
				DocumentNLP outputDocument = new DocumentNLPInMemory(dataTools, documentName, inputDocument.getOriginalText(), Language.English, pipeline);

				if (outputType == OutputType.MICRO) {
					outputDocument.toMicroAnnotation().writeToFile(outputFile.getAbsolutePath());
				} else if (outputType == OutputType.JSON) {
					if (!outputDocument.saveToJSONFile(outputFile.getAbsolutePath()))
						return false;
				}
				
				return true;
			}
		});
		
		List<Boolean> results = threads.run(documentSet.getDocumentNames(), maxThreads);
		for (Boolean result : results)
			if (!result)
				dataTools.getOutputWriter().debugWriteln("ERROR: Failed to run document through pipeline.");
		
		dataTools.getOutputWriter().debugWriteln("Finished running documents through pipeline.");
	}
	
	private static boolean parseArgs(String[] args) {
		OutputWriter output = new OutputWriter();
		OptionParser parser = new OptionParser();
		
		parser.accepts("outputType").withRequiredArg()
			.describedAs("JSON, or MICRO determines whether output data is stored as json object or micro-reading format")
			.defaultsTo("JSON");
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
		parser.accepts("minAnnotationSentenceLength").withRequiredArg()
			.describedAs("Minimum length of sentences that are considered when parsing the document")
			.ofType(Integer.class)
			.defaultsTo(DEFAULT_MIN_ANNOTATION_SENTENCE_LENGTH);
		parser.accepts("maxAnnotationSentenceLength").withRequiredArg()
			.describedAs("Maximum length of sentences that are considered when parsing the document")
			.ofType(Integer.class)
			.defaultsTo(DEFAULT_MAX_ANNOTATION_SENTENCE_LENGTH);
		parser.accepts("outputDebugFile").withRequiredArg()
			.describedAs("Optional path to debug output file")
			.ofType(File.class);
		
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
		
		stanfordPipeline = new PipelineNLPStanford((Integer)options.valueOf("minAnnotationSentenceLength"), (Integer)options.valueOf("maxAnnotationSentenceLength"));
		stanfordPipeline.initialize();
		
		if (options.has("outputDebugFile")) {
			dataTools.getOutputWriter().setDebugFile((File)options.valueOf("outputDebugFile"), false);
		}
		
		microPipeline = new PipelineNLPMicro();
	
		return true;
	}
}
