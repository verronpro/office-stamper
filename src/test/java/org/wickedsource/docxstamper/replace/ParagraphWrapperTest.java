package org.wickedsource.docxstamper.replace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.RunUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.util.ParagraphUtil.create;

/**
 * @author Tom Hombergs
 * @author Joseph Verron
 */
@DisplayName("Utilities - Paragraph Wrapper")
class ParagraphWrapperTest {

	@Test
	void getTextReturnsAggregatedText() {
		ParagraphWrapper aggregator = loremIpsum();
		assertEquals("lorem ipsum", aggregator.getText());
	}

	private ParagraphWrapper loremIpsum() {
		return new ParagraphWrapper(create("lorem", " ", "ipsum"));
	}

	@Test
	void getRunsReturnsAddedRuns() {
		ParagraphWrapper aggregator = loremIpsum();
		assertEquals(3, aggregator.getRuns().size());
		assertEquals("lorem", RunUtil.getText(aggregator.getRuns().get(0)));
		assertEquals(" ", RunUtil.getText(aggregator.getRuns().get(1)));
		assertEquals("ipsum", RunUtil.getText(aggregator.getRuns().get(2)));
	}
}
