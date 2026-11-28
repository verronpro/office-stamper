package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStampers.docxStamper;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.toAsciidoc;

/// Pins the hook resolution order contract: hooks are re-discovered in document order after
/// every mutation, so the OUTERMOST hook runs first and nested hooks run afterwards inside the
/// (possibly re-arranged) content — including inside copies made by an enclosing repeat.
@DisplayName("Nested hooks resolution order") class NestedHooksTest {

    /// A repeat comment wraps a displayIf comment which wraps a placeholder, all on one
    /// paragraph. The repeat runs first (document order), copies carry re-discovered nested
    /// hooks keyed to each item branch, and each copy's displayIf then keeps or removes the
    /// paragraph before its placeholder resolves.
    @Test void repeatContainingDisplayIfContainingPlaceholder() {
        var stamper = docxStamper(Flavor.get().standard());
        var template = makeWordResource("""
                comment::2[start="0,3", end="0,3", value="displayParagraphIf(visible)"]
                comment::1[start="0,0", end="0,7", value="repeatParagraph(items)"]
                ${name}
                """);
        var stamped = stamper.stamp(template, new Catalog());
        var actual = toAsciidoc(stamped);
        var expected = """
                A

                """;
        assertEquals(expected, actual);
    }

    /// Paragraph context: each repeated item is a branch root resolved reflectively.
    record Item(String name, boolean visible) {}

    static final class Catalog {
        public List<Item> getItems() {
            return List.of(new Item("A", true), new Item("B", false));
        }
    }
}
