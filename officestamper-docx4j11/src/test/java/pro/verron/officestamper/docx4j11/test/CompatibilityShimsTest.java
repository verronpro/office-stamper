package pro.verron.officestamper.docx4j11.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.docx.experimental.ExperimentalStampers;
import pro.verron.officestamper.docx.preset.prooferror.RemoveProofErrors;
import pro.verron.officestamper.docx.wml.WmlUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/// Pins the pre-3.4 compatibility shims: the deprecated classes under the old
/// package locations must resolve to the relocated provider implementations,
/// so code written against the single-artifact 3.3.x line keeps compiling.
class CompatibilityShimsTest {

    @Test
    @DisplayName("the old utils.wml.WmlUtils shim forwards statics to the relocated class")
    void wmlUtilsShimForwards() {
        assertEquals(WmlUtils.asString("x"), pro.verron.officestamper.utils.wml.WmlUtils.asString("x"));
    }

    @Test
    @DisplayName("the old preset processor shims extend the relocated implementations")
    void processorShimsExtendNewTypes() {
        assertInstanceOf(RemoveProofErrors.class, new pro.verron.officestamper.preset.preprocessors.prooferror.RemoveProofErrors());
    }

    @Test
    @DisplayName("the old experimental stampers shim forwards to the relocated factory")
    void experimentalShimForwards() {
        assertEquals(
                ExperimentalStampers.pptxStamper().getClass(),
                pro.verron.officestamper.experimental.ExperimentalStampers.pptxStamper().getClass());
    }
}
