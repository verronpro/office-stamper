package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;

public class GoogleDocsSupportTest {
    @DisplayName("Google Docs support integration test (conditional + repeated paragraphs)")
    @Test
    void conditionalRepeatedParagraphs_createdByGoogleDocs() {
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsGoogleDocs.docx"));
        var expected = """
                == ❬Conditional and repeated paragraph test with document generated by Google Docs❘{rtl=false}❭
                <spacing={after=0,line=276,lineRule=auto}>
                
                []❬This block is shown❘{rtl=false}❭
                ❬List items:❘{rtl=false}❭<ind=0>
                []❬item 1❘{rtl=false}❭<ind=720><rPr={u=none}>
                []❬item 2❘{rtl=false}❭<ind=720><rPr={u=none}>
                """;

        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, new Object() {
            @SuppressWarnings("unused")
            public final boolean showBlock = true;
            @SuppressWarnings("unused")
            public final List<String> items = Arrays.asList("item 1", "item 2");
        });
        assertEquals(expected, actual);
    }
}
