package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/// Extracts images from a docx document.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.4.7
public class DocxImageExtractor {

    private final WordprocessingMLPackage wordprocessingMLPackage;

    /// Creates a new image extractor for the given docx document.
    ///
    /// @param wordprocessingMLPackage the docx document to extract images from.
    public DocxImageExtractor(WordprocessingMLPackage wordprocessingMLPackage) {
        this.wordprocessingMLPackage = wordprocessingMLPackage;
    }

    /// Extract an inline graphic from a drawing.
    ///
    /// @param drawing the drawing containing the graphic.
    private static Graphic getInlineGraphic(Drawing drawing) {
        var anchorOrInline = drawing.getAnchorOrInline();
        if (anchorOrInline.isEmpty()) throw new OfficeStamperException("Anchor or Inline is empty !");
        if (anchorOrInline.getFirst() instanceof Inline inline) return inline.getGraphic();
        throw new OfficeStamperException("Don't know how to process anchor !");
    }

    private static Pic getPic(R run) {
        for (Object runContent : run.getContent()) {
            if (!(runContent instanceof JAXBElement<?> runElement)) break;
            if (!(runElement.getValue() instanceof Drawing drawing)) break;
            Graphic graphic = getInlineGraphic(drawing);
            return graphic.getGraphicData()
                          .getPic();
        }
        throw new OfficeStamperException("Run drawing not found !");
    }

    private String getImageRelPartName(String imageRelId) {
        // TODO: find a better way to find image rel part name in source part store
        return wordprocessingMLPackage.getMainDocumentPart()
                                      .getRelationshipsPart()
                                      .getPart(imageRelId)
                                      .getPartName()
                                      .getName()
                                      .substring(1);
    }

    private long getImageSize(String imageRelPartName) {
        try {
            return wordprocessingMLPackage.getSourcePartStore()
                                          .getPartSize(imageRelPartName);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private InputStream getImageStream(String imageRelPartName) {
        try {
            return wordprocessingMLPackage.getSourcePartStore()
                                          .loadPart(imageRelPartName);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    /// Extract an image bytes from an embedded image run.
    ///
    /// @param run run containing the embedded drawing.
    byte[] getRunDrawingData(R run) {
        String imageRelId = getPic(run).getBlipFill()
                                       .getBlip()
                                       .getEmbed();
        String imageRelPartName = getImageRelPartName(imageRelId);
        long size = getImageSize(imageRelPartName);
        InputStream stream = getImageStream(imageRelPartName);
        return streamToByteArray(size, stream);
    }

    /// Converts an InputStream to a byte array.
    ///
    /// @param size expected size of the byte array.
    /// @param is   input stream to read data from.
    ///
    /// @return the data from the input stream.
    private static byte[] streamToByteArray(long size, InputStream is) {
        if (size > Integer.MAX_VALUE) throw new OfficeStamperException("Image size exceeds maximum allowed (2GB)");

        int intSize = (int) size;
        byte[] data = new byte[intSize];
        int numRead = tryRead(is, data);
        return Arrays.copyOfRange(data, 0, numRead);
    }

    private static int tryRead(InputStream is, byte[] data) {
        try {
            return is.read(data);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    /// Extract the name of the image from an embedded image run.
    ///
    /// @param run run containing the embedded drawing.
    ///
    /// @return a [String] object
    public String getRunDrawingFilename(R run) {
        return getPic(run).getNvPicPr()
                          .getCNvPr()
                          .getName();
    }

    /// Extract the content type of the image from an embedded image run.
    ///
    /// @param run run containing the embedded drawing.
    ///
    /// @return a [String] object
    public String getRunDrawingAltText(R run) {
        return getPic(run).getNvPicPr()
                          .getCNvPr()
                          .getDescr();
    }

    /// Extract the width of the image from an embedded image run.
    ///
    /// @param run run containing the embedded drawing.
    ///
    /// @return a [Integer] object
    public Integer getRunDrawingMaxWidth(R run) {
        return (int) getPic(run).getSpPr()
                                .getXfrm()
                                .getExt()
                                .getCx();
    }
}
