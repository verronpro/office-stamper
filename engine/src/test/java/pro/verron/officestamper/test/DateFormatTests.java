package pro.verron.officestamper.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.makeResource;

@DisplayName("Custom functions") class DateFormatTests {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @BeforeAll
    static void beforeAll() {
        Locale.setDefault(Locale.KOREA);
    }

    @AfterAll
    static void afterAll() {
        Locale.setDefault(Locale.ROOT);
    }

    @DisplayName("Should works with variables, multiline text, in comment content, inside comment, and in repetitions.")
    @MethodSource("factories")
    @ParameterizedTest
    void features(ContextFactory factory) {
        var config = standard();
        var template = makeResource("""
                ISO Date: ${fdate(date)}
                ISO Datetime: ${fdatetime(date)}
                ISO Time: ${ftime(date)}
                
                ISO Instant: ${finstant(date)}
                ISO Basic Date: ${fbasicdate(date)}
                ISO Ordinal Date: ${fordinaldate(date)}
                ISO Week Date: ${fweekdate(date)}
                RFC 1123 Datetime: ${f1123datetime(date)}
                
                ISO Offset Date: ${foffsetdate(date)}
                ISO Offset Datetime: ${foffsetdatetime(date)}
                ISO Offset Time: ${foffsettime(date)}
                
                ISO Zoned Datetime: ${fzoneddatetime(date)}
                
                ISO Localized Date (DEFAULT): ${flocaldate(date)}
                ISO Localized Date (FULL): ${flocaldate(date, "FULL")}
                ISO Localized Date (LONG):${flocaldate(date, "LONG")}
                ISO Localized Date (MEDIUM):${flocaldate(date, "MEDIUM")}
                ISO Localized Date (SHORT):${flocaldate(date, "SHORT")}
                
                ISO Localized Time (DEFAULT): ${flocaltime(date)}
                ISO Localized Time (FULL): ${flocaltime(date, "FULL")}
                ISO Localized Time (LONG): ${flocaltime(date, "LONG")}
                ISO Localized Time (MEDIUM): ${flocaltime(date, "MEDIUM")}
                ISO Localized Time (SHORT): ${flocaltime(date, "SHORT")}
                
                Julian Calendar (Default Locale): ${fpattern(date, "GyyyyDDD")}
                Julian Calendar (French Locale): ${fpattern(date, "GyyyyDDD", "FR")}
                Julian Calendar (English Locale): ${fpattern(date, "GyyyyDDD", "EN")}
                Julian Calendar (Chinese Locale): ${fpattern(date, "GyyyyDDD", "ZH")}
                
                ISO Localized Datetime (DEFAULT): ${flocaldatetime(date)}
                ISO Localized Datetime (FULL): ${flocaldatetime(date, "FULL")}
                ISO Localized Datetime (LONG): ${flocaldatetime(date, "LONG")}
                ISO Localized Datetime (MEDIUM): ${flocaldatetime(date, "MEDIUM")}
                ISO Localized Datetime (SHORT): ${flocaldatetime(date, "SHORT")}
                
                ISO Localized Datetime (FULL, FULL): ${flocaldatetime(date, "FULL", "FULL")}
                ISO Localized Datetime (FULL, LONG): ${flocaldatetime(date, "FULL", "LONG")}
                ISO Localized Datetime (FULL, MEDIUM): ${flocaldatetime(date, "FULL", "MEDIUM")}
                ISO Localized Datetime (FULL, SHORT): ${flocaldatetime(date, "FULL", "SHORT")}
                ISO Localized Datetime (LONG, FULL): ${flocaldatetime(date, "LONG", "FULL")}
                ISO Localized Datetime (LONG, LONG): ${flocaldatetime(date, "LONG", "LONG")}
                ISO Localized Datetime (LONG, MEDIUM): ${flocaldatetime(date, "LONG", "MEDIUM")}
                ISO Localized Datetime (LONG, SHORT): ${flocaldatetime(date, "LONG", "SHORT")}
                ISO Localized Datetime (MEDIUM, FULL): ${flocaldatetime(date, "MEDIUM", "FULL")}
                ISO Localized Datetime (MEDIUM, LONG): ${flocaldatetime(date, "MEDIUM", "LONG")}
                ISO Localized Datetime (MEDIUM, MEDIUM): ${flocaldatetime(date, "MEDIUM", "MEDIUM")}
                ISO Localized Datetime (MEDIUM, SHORT): ${flocaldatetime(date, "MEDIUM", "SHORT")}
                ISO Localized Datetime (SHORT, FULL): ${flocaldatetime(date, "SHORT", "FULL")}
                ISO Localized Datetime (SHORT, LONG): ${flocaldatetime(date, "SHORT", "LONG")}
                ISO Localized Datetime (SHORT, MEDIUM): ${flocaldatetime(date, "SHORT", "MEDIUM")}
                ISO Localized Datetime (SHORT, SHORT): ${flocaldatetime(date, "SHORT", "SHORT")}
                """);
        var context = factory.date(ZonedDateTime.of(2000, 1, 12, 23, 34, 45, 567, ZoneId.of("UTC+2")));
        var stamper = new TestDocxStamper<>(config);
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
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }
}
