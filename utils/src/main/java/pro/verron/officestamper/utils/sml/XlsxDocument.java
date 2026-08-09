package pro.verron.officestamper.utils.sml;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.wml.Document;

import java.io.OutputStream;

public class XlsxDocument implements Document {

    private final SpreadsheetMLPackage mlPackage;

    public XlsxDocument(SpreadsheetMLPackage mlPackage) {
        this.mlPackage = mlPackage;
    }

    @Override
    public void save(OutputStream outputStream) {
        try {
            mlPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    public SpreadsheetMLPackage getPackage() {
        return mlPackage;
    }
}
