package edu.cmu.ml.rtw.micro.scratch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import edu.cmu.ml.rtw.micro.cat.data.annotation.nlp.NELLMentionCategorizer;
import edu.cmu.ml.rtw.micro.data.MicroDataTools;
import edu.cmu.ml.rtw.micro.model.annotation.nlp.PipelineNLPMicro;


public class RunPipelineNLPMicro {
	public enum OutputType {
		MICRO,
		JSON, 
		HTML,
		HTML_NELL_ONLY
	}
	
	public static final int DEFAULT_MAX_ANNOTATION_SENTENCE_LENGTH = 30;

	private static OutputType outputType;
	private static int maxThreads;
	private static File outputDataDir;
	private static File inputDataPath;
	
	private static int maxAnnotationSentenceLength;
	private static PipelineNLPMicro microPipeline;
	private static MicroDataTools dataTools;
	
	public static void main(String[] args) {		
		if (!parseArgs(args))
			return;
		
		final DocumentSetNLP<DocumentNLPInMemory> documentSet = DocumentSetNLP.loadFromTextPathThroughPipeline("", Language.English, inputDataPath.getAbsolutePath(), new DocumentNLPInMemory(dataTools), true);
		List<DocumentSetNLP<DocumentNLPInMemory>> documentSets = documentSet.makePartition(maxThreads, new Random(1), documentSet);
		final PipelineNLPStanford stanfordPipeline = new PipelineNLPStanford(maxAnnotationSentenceLength);
		stanfordPipeline.initialize();
		
		ThreadMapper<DocumentSetNLP<DocumentNLPInMemory>, Boolean> threads = new ThreadMapper<DocumentSetNLP<DocumentNLPInMemory>, Boolean>(new ThreadMapper.Fn<DocumentSetNLP<DocumentNLPInMemory>, Boolean>() {
			public Boolean apply(DocumentSetNLP<DocumentNLPInMemory> documents) {
				PipelineNLPStanford threadStanfordPipeline= new PipelineNLPStanford(stanfordPipeline);
				PipelineNLPMicro threadMicroPipeline = new PipelineNLPMicro(microPipeline);
				PipelineNLP pipeline = threadStanfordPipeline.weld(threadMicroPipeline);
				
				for (String documentName : documents.getDocumentNames()) {				
					dataTools.getOutputWriter().debugWriteln("Processing file " + documentName + "...");
					
					File outputFile = new File(outputDataDir, documentName);
				
					DocumentNLP inputDocument = documentSet.getDocumentByName(documentName, false);
					DocumentNLP outputDocument = new DocumentNLPInMemory(dataTools, documentName, inputDocument.getOriginalText(), Language.English, pipeline, null, true);
	
					if (outputType == OutputType.MICRO) {
						outputDocument.toMicroAnnotation().writeToFile(outputFile.getAbsolutePath());
					} else if (outputType == OutputType.JSON) {
						if (!outputDocument.saveToJSONFile(outputFile.getAbsolutePath()))
							return false;
					} else if (outputType == OutputType.HTML) {
						if (!outputDocument.saveToHtmlFile(outputFile.getAbsolutePath()))
							return false;
					} else if (outputType == OutputType.HTML_NELL_ONLY) {
						if (!outputDocument.saveToHtmlFile(outputFile.getAbsolutePath(), dataTools.getNellAnnotationTypesNLP()))
							return false;
					}
				}
				
				return true;
			}
		});
		
		List<Boolean> results = threads.run(documentSets, maxThreads);
		for (Boolean result : results)
			if (!result)
				dataTools.getOutputWriter().debugWriteln("ERROR: Failed to run document through pipeline.");
		
		dataTools.getOutputWriter().debugWriteln("Finished running documents through pipeline.");
	}
	
	private static boolean parseArgs(String[] args) {
		OutputWriter output = new OutputWriter();
		OptionParser parser = new OptionParser();
		
		parser.accepts("outputType").withRequiredArg()
			.describedAs("JSON, MICRO, HTML, HTML_NELL_ONLY determines whether output data is stored as json object, micro-reading format, or html")
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
		parser.accepts("maxAnnotationSentenceLength").withRequiredArg()
			.describedAs("Maximum length of sentences that are considered when parsing the document")
			.ofType(Integer.class)
			.defaultsTo(DEFAULT_MAX_ANNOTATION_SENTENCE_LENGTH);
		parser.accepts("outputDebugFile").withRequiredArg()
			.describedAs("Optional path to debug output file")
			.ofType(File.class);
		
		parser.accepts("nounPhraseMentionModelThreshold").withRequiredArg()
			.describedAs("The context dependent mention categorization models assign categories to a " +
						 "noun-phrase when NELL's confidence about the noun-phrase's category is below this " +
						 "threshold.")
			.ofType(Double.class)
			.defaultsTo(NELLMentionCategorizer.DEFAULT_MENTION_MODEL_THRESHOLD);
		
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
		
		microPipeline = new PipelineNLPMicro((Double)options.valueOf("nounPhraseMentionModelThreshold"), disabledAnnotators);
	
		return true;
	}
}
