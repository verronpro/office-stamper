package pro.verron.officestamper.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

/// Registry for the AsciiDoc preview extension.
public class AsciiDocPreviewExtensionRegistry
        implements ExtensionRegistry {
    @Override
    public void register(Asciidoctor asciidoctor) {
        asciidoctor.javaExtensionRegistry()
                   .blockMacro(new AsciiDocPreviewBlockMacro("preview"));
    }
}
