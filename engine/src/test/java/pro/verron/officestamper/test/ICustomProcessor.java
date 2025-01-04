package pro.verron.officestamper.test;

import pro.verron.officestamper.api.Processor;

/// ICustomProcessor interface.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
public interface ICustomProcessor
        extends Processor {

    void visitParagraph();
}
