package pro.verron.officestamper.utils.sml;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Sheet;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.iterator.ResetableIterator;

import java.util.*;
import java.util.function.Supplier;

import static org.docx4j.XmlUtils.unwrap;

public class XlsxIterator
        implements ResetableIterator<Object> {

    private static final Logger log = LoggerFactory.getLogger(XlsxIterator.class);
    private final Supplier<Iterator<?>> supplier;
    private final SpreadsheetMLPackage spreadsheet;
    private Queue<Iterator<?>> iteratorQueue;
    private @Nullable Object next;

    public XlsxIterator(SpreadsheetMLPackage spreadsheet) {
        this.spreadsheet = spreadsheet;
        try {
            supplier = spreadsheet.getWorkbookPart()
                                  .getContents()
                                  .getSheets()
                                  .getSheet()::iterator;
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    @Override
    public void reset() {
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Object next() {
        if (next == null) throw new NoSuchElementException("No more elements to iterate");

        var result = next;
        next = null;
        switch (result) {
            case ContentAccessor contentAccessor -> {
                var content = contentAccessor.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtRun sdtRun -> {
                var sdtContent = sdtRun.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtBlock sdtBlock -> {
                var sdtContent = sdtBlock.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(content.iterator());
            }
            case Sheet sheet -> {
                List<Row> content;
                try {
                    var sheetId = sheet.getId();
                    var relationshipsPart = spreadsheet.getWorkbookPart()
                                                       .getRelationshipsPart();
                    var part = relationshipsPart.getPart(sheetId);
                    content = ((WorksheetPart) part).getContents()
                                                    .getSheetData()
                                                    .getRow();
                } catch (Docx4JException e) {
                    throw new UtilsException(e);
                }
                iteratorQueue.add(content.iterator());
            }
            case Row row -> {
                var content = row.getC();
                iteratorQueue.add(content.iterator());
            }
            default -> log.debug("Unknown type: {}", result.getClass());
        }
        while (!iteratorQueue.isEmpty() && next == null) {
            var nextIterator = iteratorQueue.poll();
            if (nextIterator.hasNext()) {
                next = unwrap(nextIterator.next());
                iteratorQueue.add(nextIterator);
            }
        }
        return result;
    }
}
