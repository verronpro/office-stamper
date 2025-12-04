# AsciiDoc Parser — TODO Roadmap (for future AI assistant)

Updated: 2025-12-05 01:04 (local)

Purpose: Track features to implement in the minimal AsciiDoc parser so it can cover a practical subset and map cleanly
to Docx/FX exporters.

Current support (as of today):

- Block-level:
    - Headings: `=`, `==`, … up to `======` at line start
    - Paragraphs: non-empty lines merged; separated by blank lines
- Inline-level:
    - Emphasis: `*bold*`, `_italic_` with correct non-crossing nesting (`*bold _and italic_*`)
    - Escapes for markers: `\*`, `\_`, and backslash `\\`
    - Unbalanced markers are treated as literal text (error tolerant)

Non-goals for now:

- Full AsciiDoctor feature parity
- Complex attribute set processing and extensions

Conventions for this document:

- [ ] unchecked: not started, [*] in progress, [x] done
- Each item includes: short spec, minimal examples, parsing hints, and mapping notes to Docx/FX where relevant

---

## 1. Inline features

1.1 Inline code monospace

- [ ] Spec: Support backtick-delimited inline code `` `code` `` with literal content (no further inline parsing inside).
- Examples: `Use \`ls -la\` to list files.` → Text + CodeSpan("ls -la")
- Parsing: Add regex branch before bold/italic to capture `` `...` `` non-greedily; support escaped backtick `\``.
- Mapping: Docx runs with monospace style; FX uses `Monospaced` font.

1.2 Strong/Emphasis nesting and precedence

- [x] Spec: Allow nested `*` and `_` when non-overlapping. Reject crossing markers.
- Examples: `*bold _and italic_*` → Bold(Text("bold "), Italic("and italic"))
- Parsing: Implemented via stack-based inline parser with explicit frames for BOLD/ITALIC and ROOT.
- Mapping: DOCX/FX renderers recurse with composed styles (bold/italic) over nested inlines.

1.3 Strikethrough

- [ ] Spec: Support `~~strike~~`.
- Examples: `~~deprecated~~ feature` → Strike("deprecated")
- Parsing: Add new token; ensure it doesn't conflict with `*` or `_`.
- Mapping: Docx run with strike property; FX text decoration.

1.4 Superscript/Subscript

- [ ] Spec: `^super^`, `~sub~` (when not part of `~~strike~~`).
- Examples: `x^2^` → Sup("2"); `H~2~O` → Sub("2")
- Parsing: Context-aware tokenization to disambiguate `~` between subscript and strike.
- Mapping: Run properties for vertical alignment.

1.5 Links

- [ ] Spec: Explicit macro: `link:url[Text]`; Autolink: `https://…` → Link.
- Examples: `link:https://example.com[Example]`.
- Parsing: Recognize `link:` prefix and bracketed label; plain URL detection via regex.
- Mapping: Hyperlink in Docx; FX clickable text if supported.

1.6 Images (inline)

- [ ] Spec: `image:target[Alt, width=nn, height=nn]` minimal subset.
- Parsing: Extract target and attributes map (width/height).
- Mapping: Docx image insertion; FX ImageView.

1.7 Inline escapes (generalized)

- [ ] Spec: Backslash escapes for `* _ ~ ^ ` [ ] ( ) { } | \` and backslash itself.
- Parsing: Preprocess escapes into literal tokens before inline parsing.

## 2. Block features

2.1 Thematic break / horizontal rule

- [ ] Spec: Line with three or more `---`, `***`, or `___`.
- Parsing: Detect on a line by itself; flush paragraph buffer.
- Mapping: Docx horizontal rule; FX line separator.

2.2 Unordered lists

- [ ] Spec: Lines starting with `*` or `-` + space; nesting by indentation.
- Examples:
    - `* Item 1`\n`** Item 1.1`\n`* Item 2`
- Parsing: Accumulate list blocks; track nesting level by leading markers/indent.
- Mapping: Docx bulleted list; FX Tree of Text nodes.

2.3 Ordered lists

- [ ] Spec: `1. Item`, `a. Item`, `A. Item`, `i. Item` minimal roman/alpha; restart/continuation not required initially.
- Parsing: Similar to unordered; capture numbering style.
- Mapping: Docx numbered list.

2.4 Checklists (task lists)

- [ ] Spec: `* [ ] todo` / `* [x] done` items.
- Parsing: Detect `[ ]` / `[x]` state in list items.
- Mapping: Docx checkbox symbol; FX prefixed glyph.

2.5 Code blocks (literal)

- [ ] Spec: Fenced with triple backticks ``` or AsciiDoc literal blocks with `....` fences; no syntax highlight
  initially.
- Parsing: Capture raw content; no inline parsing inside.
- Mapping: Docx preformatted style; FX monospace TextArea-like rendering.

2.6 Blockquotes

- [ ] Spec: Lines starting with `>`; merge into a Quote block; support nested paragraphs.
- Mapping: Docx quote style; FX indented container.

2.7 Admonitions (minimal)

- [ ] Spec: `NOTE:`, `TIP:`, `WARNING:`, `CAUTION:`, `IMPORTANT:` as single-line or preceding a paragraph.
- Parsing: Detect label prefix; wrap next paragraph(s) until blank line.
- Mapping: Docx styled box/icon; FX colored pane.

2.8 Tables (simple)

- [ ] Spec: Pipe tables with header row `|===` is AsciiDoc, but for minimal subset accept Markdown-like `| a | b |` rows
  and a separator line.
- Parsing: Row split on `|`; trim; simple alignment.
- Mapping: Docx table creation; FX TableView-like.

2.9 Headings: IDs and anchors

- [ ] Spec: Optional anchor above: `[[id]]` or inline `{#id}` style; prefer `[[id]]`.
- Parsing: Capture `id` and attach to Heading.
- Mapping: Docx bookmarks; FX anchors for navigation.

2.10 Hard line breaks

- [ ] Spec: Two trailing spaces at end of line or explicit `+` line; within paragraph create `LineBreak`.
- Parsing: Preserve when joining paragraph buffer.

## 3. Attributes and subs

3.1 Document attributes (minimal)

- [ ] Spec: `:attr-name: value` at doc head; used for substitution `{attr-name}` in text.
- Parsing: First contiguous block of such lines before content; store in model.
- Mapping: Apply substitutions during parsing of text blocks.

3.2 Attribute substitution inlines

- [ ] Spec: Replace `{name}` with attribute value; escape with `\{` if needed.
- Parsing: After attributes known, run substitution before inline parsing, respecting escapes.

3.3 Built-in attributes

- [ ] Spec: Support `{empty}`, `{sp}`, `{nbsp}`, `{apos}`, `{quot}` minimally.
- Mapping: Map to literal characters.

## 4. Cross-references and footnotes

4.1 Cross-references

- [ ] Spec: `<<id,Text>>` link to a heading anchor.
- Parsing: Resolve against collected heading IDs; if missing, leave as text.
- Mapping: Docx internal hyperlink/bookmark.

4.2 Footnotes (inline)

- [ ] Spec: `footnote:[Text]` creates numbered footnote.
- Parsing: Accumulate footnote definitions per document; order of appearance.
- Mapping: Docx footnote; FX tooltip or superscript number.

## 5. Delimited blocks (minimal)

5.1 Example block

- [ ] Spec: `====` fence; content parsed as block elements; title optional.

5.2 Sidebar block

- [ ] Spec: `****` fence; content parsed as block elements.

5.3 Open block

- [ ] Spec: `--` fence; acts as generic container; useful for admonitions with multiple paragraphs.

## 6. Titles and roles

6.1 Block titles

- [ ] Spec: Optional line starting with `.` directly above a block: `.Title`.
- Parsing: Attach to next block; store title.

6.2 Roles (styles)

- [ ] Spec: `[.role]` on the line above a block; single role string initially.
- Mapping: Docx style name; FX CSS class.

## 7. Parser/Model infrastructure tasks

7.1 Inline parser refactor

- [x] Replace regex-only approach with tokenization + stack to handle nesting and precedence robustly.

7.2 Whitespace and line-join rules

- [ ] Preserve single line breaks when needed; collapse multi-spaces except in code/literal.

7.3 Error tolerance

- [x] Unbalanced delimiters become literal text; ensure no crash on malformed input.

7.4 Model extensions

- [ ] Add model nodes: CodeSpan, Strike, Sup, Sub, Link, Image, ThematicBreak, List(ListItem), Quote, CodeBlock,
  Admonition, Table, Anchor, LineBreak, AttributeSet, CrossRef, Footnote, BlockTitle, Role.

## 8. Exporter integration notes

- [ ] Update `AsciiDocToDocx` to render new nodes (lists, code, links, images, tables, admonitions, hr, quotes,
  footnotes, anchors).
- [ ] Update `AsciiDocToFx` for UI rendering (may limit scope if complex).
- [ ] Ensure `AsciiDocCompiler` plumbing for attributes and cross-ref resolution.

## 9. Testing plan

- [ ] Unit tests per node type with Arrange-Act-Assert structure.
- [ ] Golden tests: parse input → model JSON snapshot (or toString) for stability.
- [ ] Round-trip: parse → Docx generation smoke test for a composite document.

## 10. Milestones (suggested order)

M1 Core inline: code, improved nesting, links — small surface; unlocks most prose.
M2 Lists (unordered/ordered) and line breaks.
M3 Code blocks and blockquotes.
M4 Thematic break and block titles.
M5 Admonitions and roles.
M6 Images and tables.
M7 Attributes + substitutions.
M8 Cross-refs and footnotes.

Notes for implementer AI:

- Keep lines <= 120 chars; follow Office-stamper Java Style Guide.
- Prefer incremental PRs per milestone; update this file with status marks.
- Avoid overengineering; aim for robust minimal subset first.
