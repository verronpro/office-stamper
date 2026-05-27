package pro.verron.officestamper.asciidoc.core;

/// Marker interface for document blocks.
public sealed interface Block
        permits QuoteBlock, Break, CodeBlock, CommentBlock, Heading, ImageBlock, MacroBlock, OpenBlock, OrderedList,
        Paragraph, Table, UnorderedList {
    int size();
}
