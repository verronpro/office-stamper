package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.DocxFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.asciidoc.compiler.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.preset.Resolvers.*;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;

@DisplayName("Resolvers Integration Tests") class ResolversIntegrationTests {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("Object-based", objectContextFactory()),
                argumentSet("Map-based", mapContextFactory()));
    }

    @MethodSource("factories")
    @DisplayName("Should resolve LocalTime with custom formatter")
    @ParameterizedTest(name = "Should resolve LocalTime with custom formatter ({argumentSetName})")
    void isoTimeWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("HH 'hours' mm 'minutes'");
        var config = standard().setResolvers(List.of(isoTime(formatter)));
        var template = DocxFactory.makeWordResource("Time: ${date}");
        var context = factory.date(LocalTime.of(14, 30));
        var stamper = docxPackageStamper(config);

        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped, true);

        assertEquals("Time: 14 hours 30 minutes\n\n", actual);
    }

    @MethodSource("factories")
    @DisplayName("Should resolve LocalDate with custom formatter")
    @ParameterizedTest(name = "Should resolve LocalDate with custom formatter ({argumentSetName})")
    void isoDateWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var config = standard();
        config.setResolvers(List.of(isoDate(formatter)));
        var template = DocxFactory.makeWordResource("Date: ${date}");
        var context = factory.date(LocalDate.of(2024, 5, 17));
        var stamper = docxPackageStamper(config);

        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped, true);

        assertEquals("Date: 17/05/2024\n\n", actual);
    }

    @MethodSource("factories")
    @DisplayName("Should resolve LocalDateTime with custom formatter")
    @ParameterizedTest(name = "Should resolve LocalDateTime with custom formatter ({argumentSetName})")
    void isoDateTimeWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm");
        var config = standard();
        config.setResolvers(List.of(isoDateTime(formatter)));
        var template = DocxFactory.makeWordResource("DateTime: ${date}");
        var context = factory.date(LocalDateTime.of(2024, 5, 17, 14, 30));
        var stamper = docxPackageStamper(config);

        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped, true);

        assertEquals("DateTime: 2024-05-17 at 14:30\n\n", actual);
    }

    @MethodSource("factories")
    @DisplayName("Should resolve legacy Date with custom formatter")
    @ParameterizedTest(name = "Should resolve legacy Date with custom formatter ({argumentSetName})")
    void legacyDateWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        var config = standard();
        config.setResolvers(List.of(legacyDate(formatter)));
        var template = DocxFactory.makeWordResource("Legacy Date: ${date}");

        LocalDate localDate = LocalDate.of(2024, 5, 17);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault())
                                       .toInstant());

        var context = factory.date(date);

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped, true);
        assertEquals("Legacy Date: 2024.05.17\n\n", actual);
    }
}
