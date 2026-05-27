package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// Represents an inline macro in an AsciiDoc document.
/// An inline macro is a specialized inline element with a name, an identifier,
/// and a list of string values that represent its content.
///
/// @param name the name of the macro, describing its purpose or type
/// @param id   an identifier associated with the macro, often used for reference
/// @param list a list of strings representing the components of the macro's content
public record MacroInline(String name, String id, List<String> list)
        implements Inline {

    @Override
    public String text() {
        return String.join("", list);
    }
}
