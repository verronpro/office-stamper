package pro.verron.docxstamper.test;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import pro.verron.docxstamper.api.LoadingOfficeStamper;
import pro.verron.docxstamper.api.OfficeStamperConfiguration;
import pro.verron.docxstamper.preset.OfficeStampers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Common methods to interact with docx documents.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.5
 */
public final class TestDocxStamper<T> {

    private final LoadingOfficeStamper<WordprocessingMLPackage> stamper;
    private WordprocessingMLPackage document;

    /**
     * <p>Constructor for TestDocxStamper.</p>
     *
     * @param config a {@link OfficeStamperConfiguration} object
     * @since 1.6.6
     */
    public TestDocxStamper(OfficeStamperConfiguration config) {
        Function<InputStream, WordprocessingMLPackage> loader = inputStream -> {
            try {
                return WordprocessingMLPackage.load(inputStream);
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
        };
        stamper = new LoadingOfficeStamper<>(loader,
                                             OfficeStampers.docxStamper(config));
    }

    /**
     * Stamps the given template resolving the expressions within the template against the specified context.
     * Returns the resulting document after it has been saved and loaded
     * again to ensure that changes in the DOCX4J
     * object structure were really transported into the XML of the .docx file.
     *
     * @param template a {@link InputStream} object
     * @param context  a T object
     * @return a {@link WordprocessingMLPackage} object
     * @throws IOException     if any.
     * @throws Docx4JException if any.
     * @since 1.6.6
     */
    public WordprocessingMLPackage stampAndLoad(
            InputStream template,
            T context
    ) throws IOException, Docx4JException {
        OutputStream out = IOStreams.getOutputStream();
        stamper.stamp(template, context, out);
        InputStream in = IOStreams.getInputStream(out);
        return WordprocessingMLPackage.load(in);
    }

    /**
     * <p>stampAndLoadAndExtract.</p>
     *
     * @param template a {@link InputStream} object
     * @param context  a T object
     * @return a {@link java.util.List} object
     * @since 1.6.6
     */
    public String stampAndLoadAndExtract(InputStream template, T context) {
        Stringifier stringifier = new Stringifier(() -> document);
        return streamElements(template, context, P.class)
                .map(stringifier::stringify)
                .collect(joining("\n"));
    }

    private <C> Stream<C> streamElements(
            InputStream template,
            T context,
            Class<C> clazz
    ) {
        Stream<C> elements;
        try {
            var out = IOStreams.getOutputStream();
            stamper.stamp(template, context, out);
            var in = IOStreams.getInputStream(out);
            document = WordprocessingMLPackage.load(in);
            var visitor = newCollector(clazz);
            getHeaderPart(document)
                    .ifPresent(hp -> TraversalUtil.visit(hp, visitor));
            TraversalUtil.visit(getMainPart(document), visitor);
            getFooterPart(document)
                    .ifPresent(hp -> TraversalUtil.visit(hp, visitor));
            elements = visitor.elements();
        } catch (Docx4JException | IOException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    private List<Object> getMainPart(WordprocessingMLPackage document) {
        return document.getMainDocumentPart()
                .getContent();
    }

    private Optional<HeaderPart> getHeaderPart(WordprocessingMLPackage document) {
        RelationshipsPart relPart = document.getMainDocumentPart()
                .getRelationshipsPart();
        Relationship rel = relPart.getRelationshipByType(Namespaces.HEADER);
        return Optional.ofNullable(rel)
                .map(r -> (HeaderPart) relPart.getPart(r));
    }

    private Optional<FooterPart> getFooterPart(WordprocessingMLPackage document) {
        RelationshipsPart relPart = document.getMainDocumentPart()
                .getRelationshipsPart();
        Relationship rel = relPart.getRelationshipByType(Namespaces.FOOTER);
        return Optional.ofNullable(rel)
                .map(r -> (FooterPart) relPart.getPart(r));
    }

    private <C> DocxCollector<C> newCollector(Class<C> type) {
        return new DocxCollector<>(type);
    }

    /**
     * <p>stampAndLoadAndExtract.</p>
     *
     * @param template a {@link InputStream} object
     * @param context  a T object
     * @param clazz    a {@link java.lang.Class} object
     * @param <C>      a C class
     * @return a {@link java.util.List} object
     * @since 1.6.6
     */
    public <C> List<String> stampAndLoadAndExtract(
            InputStream template,
            T context,
            Class<C> clazz
    ) {
        Stringifier stringifier = new Stringifier(() -> document);
        return streamElements(template, context, clazz)
                .map(stringifier::extractDocumentRuns)
                .toList();
    }
}
