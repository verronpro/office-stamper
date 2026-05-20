# ADR 001: AsciiDoc Input Schema for DOCX Preview Scenes

## Status

Proposed

## Context

We need a way to describe DOCX preview scenes in AsciiDoc to generate example images for documentation.
These images should simulate various word processors (Microsoft Word, Google Docs, LibreOffice Writer).
The input should be able to describe not just the content but also the visual context (page size, margins, theme) and
interactive elements like comments.

## Decision

We will use a subset of AsciiDoc with specific attributes and macros to define the preview scene.

### 1. Document Attributes for Global Configuration

The following document attributes will be used to configure the preview environment:

- `:preview-theme:`: Target editor theme. Values: `word` (default), `google-docs`, `libreoffice`.
- `:preview-page-size:`: Page size. Values: `A4` (default), `Letter`.
- `:preview-margins:`: Margins in mm. Format: `top,right,bottom,left`. Default: `25,25,25,25`.

### 2. Comment Macro

Comments will be represented using a block macro to allow for metadata:

`comment::[id="...", author="...", date="...", value="...", anchor="..."]`

- `id`: Unique identifier for the comment.
- `author`: Name of the commenter.
- `date`: ISO-8601 date string or descriptive string.
- `value`: The comment text.
- `anchor`: Reference to a labeled range in the text (see below).

### 3. Text Anchors for Comments

To anchor a comment to a specific range of text, the comment block macro will use the attributes `start` and `end`
pairs, representing the block index, and character index of the given comment:

`comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]`

### 4. Placeholder Markers

Placeholders will follow the standard Office-stamper syntax within the AsciiDoc text:

- `${name}` for simple placeholders.

Since these are often literal in the documentation preview, they don't need special AsciiDoc handling other than being
preserved by the parser.

### 5. Supported Content

The parser will support:

- Headings (levels 1-3)
- Paragraphs with inline styling (bold, italic, underline, monospace)
- Unordered and ordered lists
- Simple tables

## Consequences

- The `AsciiDocParser` needs to be updated to recognize these document attributes and the `comment` macro.
- The `AsciiDocModel` needs to be extended to hold these global configurations.
- JavaFX renderers will use these attributes to set up the scene layout and styling.
