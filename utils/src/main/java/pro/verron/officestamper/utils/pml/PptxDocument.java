package pro.verron.officestamper.utils.pml;

import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.ObjectFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.Pptx4jException;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils;
import pro.verron.officestamper.utils.wml.Document;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class PptxDocument implements Document {
    private final PresentationMLPackage mlPackage;

    public PptxDocument(PresentationMLPackage mlPackage) {
        this.mlPackage = mlPackage;
    }

    /// Loads a PowerPoint document from the provided input stream.
    ///
    /// @param is the input stream containing the [PptxDocument] document data
    /// @return a [PptxDocument] representing the loaded document
    /// @throws UtilsException if there is an error loading the document
    public static PptxDocument load(InputStream is) {
        var load = OpenpackagingUtils.loadPowerpoint(is);
        return new PptxDocument(load);
    }

    @Override
    public void save(OutputStream outputStream) {
        try {
            mlPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    public List<Slide> getSlides() {
        try {
            var mainPart = mlPackage.getMainPresentationPart();
            var slideParts = mainPart.getSlideParts();
            return slideParts.stream().map(Slide::new).toList();
        } catch (Pptx4jException e) {
            throw new RuntimeException(e);
        }
    }

    public PresentationMLPackage getPackage() {
        return mlPackage;
    }

    public static class Slide {
        private final SlidePart part;

        public Slide(SlidePart part) {
            this.part = part;
        }

        public List<Shape> getShapes() {
            return PowerpointCollector.collect(part, org.pptx4j.pml.Shape.class).stream().map(Shape::new).toList();
        }

        public Part getPart() {
            return part;
        }

        public static class Shape {
            private final org.pptx4j.pml.Shape shape;

            public Shape(org.pptx4j.pml.Shape shape) {
                this.shape = shape;
            }

            public void setBlipFill(CTBlipFillProperties blipFill) {
                var factory = new ObjectFactory();
                var properties = shape.getSpPr() == null ? factory.createCTShapeProperties() : shape.getSpPr();
                properties.setBlipFill(blipFill);
                shape.setSpPr(properties);
            }

            public List<Paragraph> getParagraphs() {
                if (shape.getTxBody() == null) return emptyList();
                List<CTTextParagraph> paragraphs = new ArrayList<>(shape.getTxBody().getP());
                return paragraphs.stream().map(Paragraph::new).toList();
            }

            public void setTxBody(CTTextBody textBody) {
                shape.setTxBody(textBody);
            }

            public class Paragraph {
                private final CTTextParagraph paragraph;

                public Paragraph(CTTextParagraph paragraph) {
                    this.paragraph = paragraph;
                }

                public CTTextParagraph getParagraph() {
                    return paragraph;
                }
            }
        }
    }
}
