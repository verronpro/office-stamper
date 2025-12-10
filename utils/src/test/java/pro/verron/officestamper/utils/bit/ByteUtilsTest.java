package pro.verron.officestamper.utils.bit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteUtilsTest {

    @CsvSource({
            "  0 B , 0",
            "  1 B , 1",
            "512 B , 512",
            "1.0 kB, 1000",
            "1.0 kB, 1024",
            "1.5 kB, 1500",
            "1.0 MB, 1000000",
            "1.0 MB, 1048576",
            "1.1 GB, 1073741824",
            "1.2 GB, 1234567890",
            "2.1 GB, " + Integer.MAX_VALUE
    })
    @ParameterizedTest(name = "Convert {0} to {1}")
    @DisplayName("Readable size conversion")
    void readableSize(String expected, int length) {
        var actual = ByteUtils.readableSize(length);
        assertEquals(expected, actual);
    }
}
