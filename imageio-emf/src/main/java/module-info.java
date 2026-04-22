module pro.verron.officestamper.imageio.emf {
    requires java.desktop;
    provides javax.imageio.spi.ImageReaderSpi with pro.verron.officestamper.imageio.emf.EmfImageReaderSpi;
}
