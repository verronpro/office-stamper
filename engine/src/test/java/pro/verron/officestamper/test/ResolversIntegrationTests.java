package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.DocxFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.Resolvers.*;

@DisplayName("Resolvers Integration Tests")
class ResolversIntegrationTests extends OfficeStamperTestBase {

    @MethodSource("factories")
    @DisplayName("Should resolve LocalTime with custom formatter")
    @ParameterizedTest(name = "Should resolve LocalTime with custom formatter ({argumentSetName})")
    void isoTimeWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("HH 'hours' mm 'minutes'");
        var config = standard().setResolvers(List.of(isoTime(formatter)));
        var context = factory.date(LocalTime.of(14, 30));
        var template = DocxFactory.makeWordResource("Time: ${date}");
        var expected = "Time: 14 hours 30 minutes\n\n";
        testStamper(config, context, template, expected, true);
    }

    @MethodSource("factories")
    @DisplayName("Should resolve LocalDate with custom formatter")
    @ParameterizedTest(name = "Should resolve LocalDate with custom formatter ({argumentSetName})")
    void isoDateWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var config = standard().setResolvers(List.of(isoDate(formatter)));
        var context = factory.date(LocalDate.of(2024, 5, 17));
        var template = DocxFactory.makeWordResource("Date: ${date}");
        var expected = "Date: 17/05/2024\n\n";
        testStamper(config, context, template, expected, true);
    }

    @MethodSource("factories")
    @DisplayName("Should resolve LocalDateTime with custom formatter")
    @ParameterizedTest(name = "Should resolve LocalDateTime with custom formatter ({argumentSetName})")
    void isoDateTimeWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm");
        var config = standard().setResolvers(List.of(isoDateTime(formatter)));
        var context = factory.date(LocalDateTime.of(2024, 5, 17, 14, 30));
        var template = DocxFactory.makeWordResource("DateTime: ${date}");
        var expected = "DateTime: 2024-05-17 at 14:30\n\n";
        testStamper(config, context, template, expected, true);
    }

    @MethodSource("factories")
    @DisplayName("Should resolve legacy Date with custom formatter")
    @ParameterizedTest(name = "Should resolve legacy Date with custom formatter ({argumentSetName})")
    void legacyDateWithFormatter(ContextFactory factory) {
        var formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        var config = standard();
        config.setResolvers(List.of(legacyDate(formatter)));
        var context = factory.date(Date.from(LocalDate.of(2024, 5, 17)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
        var template = DocxFactory.makeWordResource("Legacy Date: ${date}");
        var expected = "Legacy Date: 2024.05.17\n\n";
        testStamper(config, context, template, expected, true);
    }
}
