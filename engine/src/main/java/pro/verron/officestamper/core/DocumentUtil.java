package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.*;
import java.util.stream.Stream;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/**
 * Utility class to retrieve elements from a document.
 *
 * @author Joseph Verron
 * @author DallanMC
 * @version ${version}
 * @since 1.4.7
 */
public class DocumentUtil {

    private DocumentUtil() {
        throw new OfficeStamperException("Utility classes shouldn't be instantiated");
    }

    public static <T> Stream<T> streamObjectElements(DocxPart source, Class<T> elementClass) {
        ClassFinder finder = new ClassFinder(elementClass);
        TraversalUtil.visit(source.part(), finder);
        return finder.results.stream()
                             .map(elementClass::cast);
    }

    /**
     * Retrieve the last element from an object.
     *
     * @param subDocument the object to get the last element from
     *
     * @return the last element
     */
    public static List<Object> allElements(WordprocessingMLPackage subDocument) {
        return subDocument.getMainDocumentPart()
                          .getContent();
    }

    /**
     * Recursively walk through a source to find embedded images and import them in the target document.
     *
     * @param source source document containing image files.
     * @param target target document to add image files to.
     *
     * @return a {@link Map} object
     */
    public static Map<R, R> walkObjectsAndImportImages(WordprocessingMLPackage source, WordprocessingMLPackage target) {
        return walkObjectsAndImportImages(source.getMainDocumentPart(), source, target);
    }

    /**
     * Recursively walk through source accessor to find embedded images and import the target document.
     *
     * @param container source container to walk.
     * @param source    source document containing image files.
     * @param target    target document to add image files to.
     *
     * @return a {@link Map} object
     */
    public static Map<R, R> walkObjectsAndImportImages(
            ContentAccessor container,
            WordprocessingMLPackage source,
            WordprocessingMLPackage target
    ) {
        Map<R, R> replacements = new HashMap<>();
        for (Object obj : container.getContent()) {
            Queue<Object> queue = new ArrayDeque<>();
            queue.add(obj);

            while (!queue.isEmpty()) {
                Object currentObj = queue.remove();

                if (currentObj instanceof R currentR && isImageRun(currentR)) {
                    var docxImageExtractor = new DocxImageExtractor(source);
                    var imageData = docxImageExtractor.getRunDrawingData(currentR);
                    var maxWidth = docxImageExtractor.getRunDrawingMaxWidth(currentR);
                    var imagePart = tryCreateImagePart(target, imageData);
                    var runWithImage = newRun(maxWidth, imagePart, "dummyFileName", "dummyAltText");
                    replacements.put(currentR, runWithImage);
                }
                else if (currentObj instanceof ContentAccessor contentAccessor)
                    queue.addAll(contentAccessor.getContent());
            }
        }
        return replacements;
    }

    /**
     * Check if a run contains an embedded image.
     *
     * @param run the run to analyze
     *
     * @return true if the run contains an image, false otherwise.
     */
    private static boolean isImageRun(R run) {
        return run.getContent()
                  .stream()
                  .filter(JAXBElement.class::isInstance)
                  .map(JAXBElement.class::cast)
                  .map(JAXBElement::getValue)
                  .anyMatch(Drawing.class::isInstance);
    }

    private static BinaryPartAbstractImage tryCreateImagePart(WordprocessingMLPackage destDocument, byte[] imageData) {
        try {
            return BinaryPartAbstractImage.createImagePart(destDocument, imageData);
        } catch (Exception e) {
            throw new OfficeStamperException(e);
        }
    }

    /**
     * Finds the smallest common parent between two objects.
     *
     * @param o1 the first object
     * @param o2 the second object
     *
     * @return the smallest common parent of the two objects
     *
     * @throws OfficeStamperException if there is an error finding the common parent
     */
    public static ContentAccessor findSmallestCommonParent(Object o1, Object o2) {
        if (depthElementSearch(o1, o2) && o2 instanceof ContentAccessor contentAccessor)
            return findInsertableParent(contentAccessor);
        else if (o2 instanceof Child child) return findSmallestCommonParent(o1, child.getParent());
        else throw new OfficeStamperException();
    }

    /**
     * Recursively searches for an element in a content tree.
     *
     * @param searchTarget the element to search for
     * @param content      the content tree to search in
     *
     * @return true if the element is found, false otherwise
     */
    public static boolean depthElementSearch(Object searchTarget, Object content) {
        content = XmlUtils.unwrap(content);
        if (searchTarget.equals(content)) {
            return true;
        }
        else if (content instanceof ContentAccessor contentAccessor) {
            for (Object object : contentAccessor.getContent()) {
                Object unwrappedObject = XmlUtils.unwrap(object);
                if (searchTarget.equals(unwrappedObject) || depthElementSearch(searchTarget, unwrappedObject)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ContentAccessor findInsertableParent(Object searchFrom) {
        return switch (searchFrom) {
            case Tc tc -> tc;
            case Body body -> body;
            case Child child -> findInsertableParent(child.getParent());
            default -> throw new OfficeStamperException("Unexpected parent " + searchFrom.getClass());
        };
    }
}
