package pro.verron.officestamper.imageio.emf;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import static javax.imageio.metadata.IIOMetadataFormatImpl.standardMetadataFormatName;

class EMFMetadata
        extends IIOMetadata {
    private final int width;
    private final int height;

    EMFMetadata(int width, int height) {
        super(true, null, null, null, null);
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isReadOnly() {return true;}

    @Override
    public Node getAsTree(String formatName) {
        if (standardMetadataFormatName.equals(formatName)) return getStandardTree();
        throw new IllegalArgumentException("Unsupported format: " + formatName);
    }

    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IIOMetadataNode getStandardDimensionNode() {
        var node = new IIOMetadataNode("Dimension");
        node.appendChild(createNode("HorizontalScreenSize", Integer.toString(width)));
        node.appendChild(createNode("VerticalScreenSize", Integer.toString(height)));
        return node;
    }

    private static IIOMetadataNode createNode(String nodeName, String value) {
        var widthNode = new IIOMetadataNode(nodeName);
        widthNode.setAttribute("value", value);
        return widthNode;
    }

    @Override
    public void reset() {}
}
