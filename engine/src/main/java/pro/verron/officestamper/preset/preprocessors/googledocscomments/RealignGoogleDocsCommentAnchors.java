package pro.verron.officestamper.preset.preprocessors.googledocscomments;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.utils.WmlUtils;

import java.util.stream.IntStream;

/**
 * Google Docs wraps CommentRangeStart into an SdtRun, while CommentRangeEnd still remains right below the paragraph.
 * This causes issues in office-stamper, because CommentRangeStart and CommentRangeEnd are always expected
 * to be at the same level.<br />
 * This pre-processor fixes this by moving the CommentRangeStart out of the SDT and right below the SDT
 * in the paragraph.
 * <br />
 * E.g.:
 * <pre>
 * {@code
 * <w:p>
 *     <w:sdt>
 *         <w:sdtContentRun>
 *             <w:CommentRangeStart />
 *         </w:sdtContentRun>
 *     </w:sdt>
 *     <w:r>...</w:r>
 *     <w:CommentRangeEnd />
 * </w:p>
 * }
 * </pre>
 * is transformed to
 * <pre>
 * {@code
 * <w:p>
 *     <w:sdt>
 *         <w:sdtContentRun>
 *         </w:sdtContentRun>
 *     </w:sdt>
 *     <w:CommentRangeStart />
 *     <w:r>...</w:r>
 *     <w:CommentRangeEnd />
 * </w:p>
 * }
 * </pre>
 */
public class RealignGoogleDocsCommentAnchors implements PreProcessor {

    @Override
    public void process(WordprocessingMLPackage document) {
        var commentElements = WmlUtils.extractCommentElements(document);

        for (Child commentElement : commentElements) {
            if (!(commentElement instanceof CommentRangeStart)) {
                continue;
            }

            if (commentElement.getParent() instanceof CTSdtContentRun sdtContentRun
                    && sdtContentRun.getParent() instanceof SdtRun sdtRun
                    && sdtRun.getParent() instanceof ContentAccessor contentAccessor) {

                // Re-wire the parent of CommentRangeStart
                commentElement.setParent(contentAccessor);

                // Find the position of the SDT below the paragraph
                // to re-insert the CommentRangeStart at
                var content = contentAccessor.getContent();
                var position = IntStream.range(0, content.size())
                        .filter(i -> {
                            if (content.get(i) instanceof JAXBElement<?> jaxbElement) {
                                return jaxbElement.getValue() == sdtRun;
                            }
                            return false;
                        })
                        .findFirst();

                if (position.isEmpty()) {
                    throw new IllegalStateException("Unexpected XML structure. This is likely a logic error.");
                }
                contentAccessor.getContent().add(position.getAsInt() + 1, commentElement);

                // Finally, remove the CommentRangeStart from the SDT
                sdtContentRun.getContent().remove(commentElement);
            }
        }
    }
}
