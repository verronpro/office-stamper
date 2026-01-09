package pro.verron.officestamper.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.DocxFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

@DisplayName("Date Formatting features") class DateFormatTests {

    static Stream<Arguments> factories() {
        return Stream.of(//
                argumentSet("Object-based", objectContextFactory()),//
                argumentSet("Map-based", mapContextFactory())//
        );
    }

    @BeforeAll
    static void beforeAll() {
        Locale.setDefault(Locale.KOREA);
    }

    @AfterAll
    static void afterAll() {
        Locale.setDefault(Locale.ROOT);
    }


    @DisplayName("Should allow to format dates")
    @MethodSource("factories")
    @ParameterizedTest(name = "Should allow to format dates ({argumentSetName})")
    void features(ContextFactory factory) {
        var config = standard();
        var template = DocxFactory.makeWordResource("""
                ISO Date: 2000-01-12+02:00
                
                ISO Datetime: 2000-01-12T23:34:45.000000567+02:00[UTC+02:00]
                
                ISO Time: 23:34:45.000000567+02:00
                
                ISO Instant: 2000-01-12T21:34:45.000000567Z
                
                ISO Basic Date: 20000112+0200
                
                ISO Ordinal Date: 2000-012+02:00
                
                ISO Week Date: 2000-W02-3+02:00
                
                RFC 1123 Datetime: Wed, 12 Jan 2000 23:34:45 +0200
                
                ISO Offset Date: 2000-01-12+02:00
                
                ISO Offset Datetime: 2000-01-12T23:34:45.000000567+02:00
                
                ISO Offset Time: 23:34:45.000000567+02:00
                
                ISO Zoned Datetime: 2000-01-12T23:34:45.000000567+02:00[UTC+02:00]
                
                ISO Localized Date (DEFAULT): 2000-01-12
                
                ISO Localized Date (FULL): 2000년 1월 12일 수요일
                
                ISO Localized Date (LONG):2000년 1월 12일
                
                ISO Localized Date (MEDIUM):2000. 1. 12.
                
                ISO Localized Date (SHORT):00. 1. 12.
                
                ISO Localized Time (DEFAULT): 23:34:45.000000567
                
                ISO Localized Time (FULL): 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Time (LONG): 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Time (MEDIUM): 오후 11:34:45
                
                ISO Localized Time (SHORT): 오후 11:34
                
                Julian Calendar (Default Locale): AD2000012
                
                Julian Calendar (French Locale): ap. J.-C.2000012
                
                Julian Calendar (English Locale): AD2000012
                
                Julian Calendar (Chinese Locale): 公元2000012
                
                ISO Localized Datetime (DEFAULT): 2000-01-12T23:34:45.000000567
                
                ISO Localized Datetime (FULL): 2000년 1월 12일 수요일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (LONG): 2000년 1월 12일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (MEDIUM): 2000. 1. 12. 오후 11:34:45
                
                ISO Localized Datetime (SHORT): 00. 1. 12. 오후 11:34
                
                ISO Localized Datetime (FULL, FULL): 2000년 1월 12일 수요일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (FULL, LONG): 2000년 1월 12일 수요일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (FULL, MEDIUM): 2000년 1월 12일 수요일 오후 11:34:45
                
                ISO Localized Datetime (FULL, SHORT): 2000년 1월 12일 수요일 오후 11:34
                
                ISO Localized Datetime (LONG, FULL): 2000년 1월 12일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (LONG, LONG): 2000년 1월 12일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (LONG, MEDIUM): 2000년 1월 12일 오후 11:34:45
                
                ISO Localized Datetime (LONG, SHORT): 2000년 1월 12일 오후 11:34
                
                ISO Localized Datetime (MEDIUM, FULL): 2000. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (MEDIUM, LONG): 2000. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (MEDIUM, MEDIUM): 2000. 1. 12. 오후 11:34:45
                
                ISO Localized Datetime (MEDIUM, SHORT): 2000. 1. 12. 오후 11:34
                
                ISO Localized Datetime (SHORT, FULL): 00. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (SHORT, LONG): 00. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (SHORT, MEDIUM): 00. 1. 12. 오후 11:34:45
                
                ISO Localized Datetime (SHORT, SHORT): 00. 1. 12. 오후 11:34
                
                """);
        var context = factory.date(ZonedDateTime.of(2000, 1, 12, 23, 34, 45, 567, ZoneId.of("UTC+2")));
        var stamper = docxPackageStamper(config);
        var expected = """
                ISO Date: 2000-01-12+02:00
                
                ISO Datetime: 2000-01-12T23:34:45.000000567+02:00[UTC+02:00]
                
                ISO Time: 23:34:45.000000567+02:00
                
                ISO Instant: 2000-01-12T21:34:45.000000567Z
                
                ISO Basic Date: 20000112+0200
                
                ISO Ordinal Date: 2000-012+02:00
                
                ISO Week Date: 2000-W02-3+02:00
                
                RFC 1123 Datetime: Wed, 12 Jan 2000 23:34:45 +0200
                
                ISO Offset Date: 2000-01-12+02:00
                
                ISO Offset Datetime: 2000-01-12T23:34:45.000000567+02:00
                
                ISO Offset Time: 23:34:45.000000567+02:00
                
                ISO Zoned Datetime: 2000-01-12T23:34:45.000000567+02:00[UTC+02:00]
                
                ISO Localized Date (DEFAULT): 2000-01-12
                
                ISO Localized Date (FULL): 2000년 1월 12일 수요일
                
                ISO Localized Date (LONG):2000년 1월 12일
                
                ISO Localized Date (MEDIUM):2000. 1. 12.
                
                ISO Localized Date (SHORT):00. 1. 12.
                
                ISO Localized Time (DEFAULT): 23:34:45.000000567
                
                ISO Localized Time (FULL): 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Time (LONG): 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Time (MEDIUM): 오후 11:34:45
                
                ISO Localized Time (SHORT): 오후 11:34
                
                Julian Calendar (Default Locale): AD2000012
                
                Julian Calendar (French Locale): ap. J.-C.2000012
                
                Julian Calendar (English Locale): AD2000012
                
                Julian Calendar (Chinese Locale): 公元2000012
                
                ISO Localized Datetime (DEFAULT): 2000-01-12T23:34:45.000000567
                
                ISO Localized Datetime (FULL): 2000년 1월 12일 수요일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (LONG): 2000년 1월 12일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (MEDIUM): 2000. 1. 12. 오후 11:34:45
                
                ISO Localized Datetime (SHORT): 00. 1. 12. 오후 11:34
                
                ISO Localized Datetime (FULL, FULL): 2000년 1월 12일 수요일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (FULL, LONG): 2000년 1월 12일 수요일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (FULL, MEDIUM): 2000년 1월 12일 수요일 오후 11:34:45
                
                ISO Localized Datetime (FULL, SHORT): 2000년 1월 12일 수요일 오후 11:34
                
                ISO Localized Datetime (LONG, FULL): 2000년 1월 12일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (LONG, LONG): 2000년 1월 12일 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (LONG, MEDIUM): 2000년 1월 12일 오후 11:34:45
                
                ISO Localized Datetime (LONG, SHORT): 2000년 1월 12일 오후 11:34
                
                ISO Localized Datetime (MEDIUM, FULL): 2000. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (MEDIUM, LONG): 2000. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (MEDIUM, MEDIUM): 2000. 1. 12. 오후 11:34:45
                
                ISO Localized Datetime (MEDIUM, SHORT): 2000. 1. 12. 오후 11:34
                
                ISO Localized Datetime (SHORT, FULL): 00. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (SHORT, LONG): 00. 1. 12. 오후 11시 34분 45초 UTC+02:00
                
                ISO Localized Datetime (SHORT, MEDIUM): 00. 1. 12. 오후 11:34:45
                
                ISO Localized Datetime (SHORT, SHORT): 00. 1. 12. 오후 11:34
                
                """;
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
        assertEquals(expected, actual);
    }
}
