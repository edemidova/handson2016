package uk.soton.examples.nlp;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class NLPParserDemo {

	StanfordCoreNLP pipeline;

	public NLPParserDemo() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		props.setProperty("ssplit.isOneSentence", "false");

		pipeline = new StanfordCoreNLP(props);
		pipeline.addAnnotator(new TimeAnnotator("sutime", new Properties()));
	
	}

	public void parseText(String text, String documentdate) {
		Annotation document = new Annotation(text);
		document.set(CoreAnnotations.DocDateAnnotation.class, documentdate);
		// run all annotators on this text
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		Morphology m = new Morphology();
		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);

				System.out.println(word + "(POS: " + pos + ", NER: " + ne + ", LEMMA: " + m.lemma(word, pos) + ")");
			}
			List<CoreMap> timexAnnsAll = sentence.get(TimeAnnotations.TimexAnnotations.class);
			for (CoreMap cm : timexAnnsAll) {
				List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
				Temporal temporal = cm.get(TimeExpression.Annotation.class).getTemporal();
				System.out.println(cm + " [from char offset "
						+ tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) + " to "
						+ tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']'
						+ " --> " + temporal.toISOString() + "(" + temporal.toString() + ")");
			}
			System.out.println("--");

			/*
			 * // this is the parse tree of the current sentence Tree tree =
			 * sentence.get(TreeAnnotation.class);
			 * System.out.println(traverse(tree));
			 * 
			 * // this is the Stanford dependency graph of the current sentence
			 * SemanticGraph dependencies =
			 * sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			 * System.out.println(dependencies);
			 */
		}
	}

	public String traverse(Tree tree) {

		StringBuilder sb = new StringBuilder();
		List<Tree> leaves = tree.getLeaves();

		for (Tree leaf : leaves) {
			Tree parent = leaf.parent(tree);
			sb.append(leaf.label().value() + "_" + parent.label().value() + " ");
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		String text = " Barack Obama. He is a president of the United States. Next year there will be another president. This Friday I am free. Anyone around know where a good place to get hair colored at?  Mod Salon!  Kate and Co! Get Kelsey to do it What are the price ranges?  My place  Walmart you can do it yourself unless you want to pay something crazy Depends on how long your hair is and what you want done, if you look on Mods website it will give you an idea  Capri! Ask for an advanced student. Good prices with wonderful results  College hill barbers, ask for jenny  Posh on university-Cedar Loo area";
		try {
			text = ArticleExtractor.INSTANCE.getText(new URL("http://www.bbc.co.uk/news/business-37220701"));
			NLPParserDemo demo = new NLPParserDemo();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//"2015-04-01"
			demo.parseText(text, sdf.format(new Date()));

		} catch (BoilerpipeProcessingException e) {
			
			e.printStackTrace();
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		}

	}
}
