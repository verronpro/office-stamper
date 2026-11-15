package pro.verron.officestamper.spi;

/// Post-processor that removes smart tags of a given element name while preserving their content.
///
/// <p>The concrete, library-specific traversal is supplied by a provider (R2-3).
public abstract class HookRemover
        implements PostProcessor {

    private final String element;

    /// @param element the name of the smart-tag element to remove
    protected HookRemover(String element) {
        this.element = element;
    }

    /// @return the name of the smart-tag element this remover targets
    public String element() {
        return element;
    }

    @Override
    public abstract void process(OfficeDocument document);
}
