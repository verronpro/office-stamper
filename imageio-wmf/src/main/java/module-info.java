module pro.verron.officestamper.imageio.wmf {
    requires java.desktop;
    provides javax.imageio.spi.ImageReaderSpi with pro.verron.officestamper.imageio.wmf.WmfImageReaderSpi;
}
