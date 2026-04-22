module pro.verron.officestamper.imageio.svg {
    requires java.desktop;
    provides javax.imageio.spi.ImageReaderSpi with pro.verron.officestamper.imageio.svg.SvgImageReaderSpi;
}
