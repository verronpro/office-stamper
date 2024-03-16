package pro.verron.docxstamper.test;

import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.preset.Configurations;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static pro.verron.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 * @author Thomas Oster
 */
class PlaceholderReplacementInTextBoxesTest {
	@Test
    void expressionReplacementInTextBoxesTest() {
		var context = new Name("Bart Simpson");
		var template = getResource(Path.of("ExpressionReplacementInTextBoxesTest" +
										   ".docx"));
		var configuration = Configurations.standard()
				.setFailOnUnresolvedExpression(false);
		var stamper = new TestDocxStamper<Name>(configuration);
		var actual = stamper.stampAndLoadAndExtract(template, context, Anchor.class);
		List<String> expected = List.of(
				"❬Bart Simpson❘color=auto❭",
				"❬${foo}❘color=auto❭"
		);
		assertIterableEquals(expected, actual);
	}

	public record Name(String name) {
	}
}
