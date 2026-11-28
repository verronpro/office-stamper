package pro.verron.officestamper.api;

import java.util.List;

/// The live, mutable content of a container: an [OfficePart]'s body or the children of a container
/// [Element]. All splicing operations of the engine go through views of this interface.
public interface Content {

    /// @return a snapshot of the current child elements of this content
    List<Element> elements();

    /// @param element the element to locate
    /// @return the index of the element in this content, or -1 if it is not present
    int indexOf(Element element);

    /// Inserts the given elements into this content at the given index. Elements produced by
    /// [Element#copy()] are detached; this operation re-parents them to the container.
    void addAll(int index, List<Element> elements);

    /// Removes the given element from this content (no-op when it is not present).
    void remove(Element element);
}
