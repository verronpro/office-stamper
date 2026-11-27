package pro.verron.officestamper.preset.postprocessors;

/// Deprecated compatibility shim: this type moved to [pro.verron.officestamper.docx.preset.cleanfootnotes.NoteRefsVisitor]
/// in the 4.0 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.preset.cleanfootnotes.NoteRefsVisitor] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class NoteRefsVisitor extends pro.verron.officestamper.docx.preset.cleanfootnotes.NoteRefsVisitor {}
