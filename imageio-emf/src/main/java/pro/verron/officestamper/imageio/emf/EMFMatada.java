package pro.verron.officestamper.imageio.emf;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.metadata.IIOMetadataFormat;
import org.w3c.dom.Node;

class EMFMetadata extends IIOMetadata {
    private final int width;
    private final int height;

    EMFMetadata(int width, int height) {
        super(false,
              "javax_imageio_1.0",
              null,
              null, null);
        this.width = width;
        this.height = height;
    }

    @Override
    public Node getAsTree(String formatName) {
        if (formatName.equals(nativeMetadataFormatName)) {
            IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);
            IIOMetadataNode dimension = new IIOMetadataNode("Dimension");

            IIOMetadataNode widthNode = new IIOMetadataNode("PixelWidth");
            widthNode.setAttribute("value", Integer.toString(width));

            IIOMetadataNode heightNode = new IIOMetadataNode("PixelHeight");
            heightNode.setAttribute("value", Integer.toString(height));

            dimension.appendChild(widthNode);
            dimension.appendChild(heightNode);
            root.appendChild(dimension);
            return root;
        }
        throw new IllegalArgumentException("Unsupported format: " + formatName);
    }

    @Override
    public boolean isReadOnly() { return true; }

    @Override
    public IIOMetadataFormat getMetadataFormat() { return null; }

    @Override
    public void reset() { }

    @Override
    public IIOMetadataNode getAsTree() {
        return (IIOMetadataNode) getAsTree(nativeMetadataFormatName);
    }

    @Override
    public boolean isStandardMetadataFormatSupported() { return true; }

    public String getNativeMetadataFormatName() { return nativeMetadataFormatName; }

    public void setFromTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }

    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }
}
