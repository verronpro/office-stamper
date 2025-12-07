package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jspecify.annotations.NonNull;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.asciidoc.AsciiDocCompiler;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.WmlFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static pro.verron.officestamper.utils.WmlFactory.*;


/// A utility class for testing.
/// Provides methods for retrieving InputStreams from specified resource paths.
/// Typically used for accessing test resources.
public class TestUtils {

    private static final Pattern TABLE_PATTERN = compile("^\\|===$");
    private static final Pattern CELL_PATTERN = compile("^\\|.*");
    private static final Pattern EMPTY_PATTERN = compile("^$");
    private static final Pattern CRS_PATTERN = compile("^(.*?)<([0-9]+)\\|>.*");
    private static final Pattern TAB_PATTERN = compile("^(.*?)\\|TAB\\|.*");
    private static final Pattern CR_PATTERN = compile("^(.*?)<([0-9]+)\\|(.+?)>.*");
    private static final Pattern CRE_PATTERN = compile("^(.*?)<\\|([0-9]+)>.*");

    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    ///
    /// @return an InputStream for the specified resource
    public static InputStream getResource(String path) {
        return getResource(Path.of(path));
    }


    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    ///
    /// @return an InputStream for the specified resource
    public static InputStream getResource(Path path) {
        try {
            var testRoot = Path.of("..", "test", "sources");
            var resolve = testRoot.resolve(path);
            return Files.newInputStream(resolve);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeResource(String content) {
        var aPackage = makeDocx(content);
        OutputStream outputStream;
        try {
            outputStream = IOStreams.getOutputStream();
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
        try {
            aPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
        return IOStreams.getInputStream(outputStream);
    }


    public static @NonNull WordprocessingMLPackage makeDocx(String content) {
        var aPackage = newWord();
        var mainDocumentPart = aPackage.getMainDocumentPart();
        var parent = new AtomicReference<ContentAccessor>(mainDocumentPart);
        content.lines()
               .forEach(line -> {
                   var p1 = new P();
                   while (!line.isEmpty()) {
                       var tableMatcher = TABLE_PATTERN.matcher(line);
                       var cellMatcher = CELL_PATTERN.matcher(line);
                       var emptyMatcher = EMPTY_PATTERN.matcher(line);
                       var tabMatcher = TAB_PATTERN.matcher(line);
                       var crsMatcher = CRS_PATTERN.matcher(line);
                       var creMatcher = CRE_PATTERN.matcher(line);
                       var crMatcher = CR_PATTERN.matcher(line);
                       if (tableMatcher.matches()) {
                           var accessor = parent.get();
                           if (accessor instanceof Tc tc) parent.set((ContentAccessor) tc.getParent());
                           if (accessor instanceof Tr tr) parent.set((ContentAccessor) tr.getParent());
                           if (accessor instanceof Tbl tbl) parent.set((ContentAccessor) tbl.getParent());
                           else {
                               var newTbl = newTbl();
                               var newRow = newRow();
                               newTbl.getContent()
                                     .add(newRow);
                               parent.get()
                                     .getContent()
                                     .add(newTbl);
                               parent.set(newRow);
                           }
                           return;
                       }
                       else if (cellMatcher.matches()) {
                           var newCell = newCell();
                           var accessor = parent.get();
                           if (accessor instanceof Tc tc) parent.set((ContentAccessor) tc.getParent());
                           parent.get()
                                 .getContent()
                                 .add(newCell);
                           parent.set(newCell);
                           line = line.substring(1);
                       }
                       else if (emptyMatcher.matches()) {
                           var accessor = parent.get();
                           if (accessor instanceof Tc tc) parent.set((ContentAccessor) tc.getParent());
                           if (accessor instanceof Tr tr) parent.set((ContentAccessor) tr.getParent());
                           if (accessor instanceof Tbl tbl) {
                               var row = newRow();
                               tbl.getContent()
                                  .add(row);
                               parent.set(row);
                               return;
                           }
                       }
                       else if (tabMatcher.matches()) {
                           var next = tabMatcher.group(1);
                           var text = newText(next);
                           p1.getContent()
                             .add(newRun(List.of(text, new R.Tab())));
                           line = line.substring(next.length() + "|TAB|".length());
                       }
                       else if (crsMatcher.matches()) {
                           var next = crsMatcher.group(1);
                           var strId = crsMatcher.group(2);
                           var id = new BigInteger(strId);
                           var text = newText(next);
                           var commentRangeStart = new CommentRangeStart();
                           commentRangeStart.setId(id);
                           p1.getContent()
                             .add(newRun(text));
                           p1.getContent()
                             .add(commentRangeStart);
                           line = line.substring(next.length() + 1 + strId.length() + 2);
                       }
                       else if (creMatcher.matches()) {
                           var next = creMatcher.group(1);
                           var strId = creMatcher.group(2);
                           var text = newText(next);
                           text.setValue(next);
                           var id = new BigInteger(strId);
                           var commentRangeEnd = new CommentRangeEnd();
                           commentRangeEnd.setId(id);
                           p1.getContent()
                             .add(newRun(text));

                           p1.getContent()
                             .add(commentRangeEnd);
                           line = line.substring(next.length() + 2 + strId.length() + 1);
                       }
                       else if (crMatcher.matches()) {
                           var next = crMatcher.group(1);
                           var strId = crMatcher.group(2);
                           var strComment = crMatcher.group(3);
                           var text = newText(next);
                           var id = new BigInteger(strId);
                           var commentReference = new R.CommentReference();
                           commentReference.setId(id);
                           var comment = WmlFactory.newComment(id, strComment);
                           try {
                               mainDocumentPart.getCommentsPart()
                                               .getContents()
                                               .getComment()
                                               .add(comment);
                           } catch (Docx4JException e) {
                               throw new OfficeStamperException(e);
                           }
                           var run = newRun(List.of(text, commentReference));
                           p1.getContent()
                             .add(run);
                           line = line.substring(next.length() + 1 + strId.length() + 1 + strComment.length() + 1);
                       }
                       else {
                           var run = newRun(line);
                           p1.getContent()
                             .add(run);
                           line = "";
                       }
                   }
                   parent.get()
                         .getContent()
                         .add(p1);
               });
        return aPackage;
    }

    /// Creates an in-memory DOCX from an AsciiDoc source string using the AsciiDoc module and returns it as an
    /// InputStream suitable for tests. The provided string must be valid AsciiDoc. Headings ("= Title"), sections,
    /// paragraphs, hard line breaks using trailing '+' syntax, and literal placeholders like "${name}" are preserved.
    /// Tabs can be represented with the tab character ("\t"). Custom comment macro support in AsciiDoc input:
    ///  - comment:ID\[start] and comment:ID\[end] to delimit a Word comment range
    ///  - comment:ID\[ref] to insert a Word comment reference
    ///  - comment:ID\["content"] or comment:ID\[content, "content"] to define the comment's content
    public static InputStream makeAsciiDocResource(String asciidoc) {
        WordprocessingMLPackage aPackage = makeAsciiDocDocx(asciidoc);
        OutputStream outputStream;
        try {
            outputStream = IOStreams.getOutputStream();
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
        try {
            aPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
        return IOStreams.getInputStream(outputStream);
    }

    public static WordprocessingMLPackage makeAsciiDocDocx(String asciidoc) {
        return AsciiDocCompiler.toDocx(asciidoc);
    }

    static Image getImage(Path path) {
        try {
            return new Image(getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Image getImage(Path path, int size) {
        try {
            return new Image(getResource(path), size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
