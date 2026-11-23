package pro.verron.officestamper.docx4j11.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.docx4j11.DocxStamperProvider;
import pro.verron.officestamper.api.OfficeStampers;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/// Proves the drop-in provider mechanism: a docx4j-free caller obtains the
/// stamper purely via {@link OfficeStampers} (ServiceLoader), so the docx4j
/// dependency is confined to this module and can be swapped or removed.
class DocxStamperDiscoveryTest {

    @Test
    void providerIsDiscoverableWithoutNamingDocx4j() {
        var stamper = OfficeStampers.docxStamper();
        assertNotNull(stamper);
        assertInstanceOf(DocxStamperProvider.class, stamper);
    }
}
