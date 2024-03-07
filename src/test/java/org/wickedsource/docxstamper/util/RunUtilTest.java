package org.wickedsource.docxstamper.util;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.utils.IOStreams;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.DefaultTests.getResource;

/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
@DisplayName("Utilities - Docx Run Methods")
class RunUtilTest {

	@Test
	void getTextReturnsTextOfRun() throws Docx4JException {
		var document = loadDocument(Path.of("util","singleRun.docx"));
		var paragraph = (P) document.getMainDocumentPart().getContent().get(0);
		var run = (R) paragraph.getContent().get(0);
		assertEquals("This is the only run of text in this document.", RunUtil.getText(run));
	}

	/**
	 * Loads a document from the given path.
	 *
	 * @param path the path to the document.
	 * @return the loaded document.
	 * @throws org.docx4j.openpackaging.exceptions.Docx4JException if any.
	 * @since 1.6.6
	 */
	public WordprocessingMLPackage loadDocument(Path path) throws Docx4JException {
		var in = getResource(path);
		return WordprocessingMLPackage.load(in);
	}

	@Test
	void getTextReturnsValueDefinedBySetText() throws Docx4JException, IOException {
		var input = loadDocument(Path.of("util","singleRun.docx"));
		var paragraphIn = (P) input.getMainDocumentPart().getContent().get(0);
		var runIn = (R) paragraphIn.getContent().get(0);
		RunUtil.setText(runIn, "The text of this run was changed.");
		var out = IOStreams.getOutputStream();
		input.save(out);
		var in = IOStreams.getInputStream(out);
		var output = WordprocessingMLPackage.load(in);
		var paragraphOut = (P) output.getMainDocumentPart().getContent().get(0);
		var runOut = (R) paragraphOut.getContent().get(0);
		assertEquals("The text of this run was changed.", RunUtil.getText(runOut));
	}

}
