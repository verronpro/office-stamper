package pro.verron.officestamper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.excel.ExcelContext;
import pro.verron.officestamper.excel.ExcelMergeStrategy;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static pro.verron.officestamper.Main.extension;

public class Contextualizer {
    static Object contextualise(Path path, ExcelMergeStrategy excelMergeStrategy, String excelJoinKey) {
        var extension = extension(path);
        return switch (extension) {
            case "csv" -> processCsv(path);
            case "properties" -> processProperties(path);
            case "html", "xml" -> processXmlOrHtml(path);
            case "json" -> processJson(path);
            case "yaml", "yml" -> processYaml(path);
            case "xlsx" -> processExcel(path, excelMergeStrategy, excelJoinKey);
            default -> throw new OfficeStamperException("Unsupported file type: " + path);
        };
    }

    static boolean isSupportedDataFile(Path p) {
        var extension = extension(p.getFileName());
        return extension.equals("csv") || //
                extension.equals("properties") || //
                extension.equals("html") || //
                extension.equals("xml") || //
                extension.equals("json") || //
                extension.equals("yaml") || //
                extension.equals("yml") || //
                extension.equals("xlsx");
    }

    /// Return a list of objects with the csv properties
    static Object processCsv(Path path) {
        try (var inputStream = Files.newInputStream(path); var streamReader = new InputStreamReader(inputStream); var reader = new CSVReader(streamReader);) {
            var headers = reader.readNext();
            return reader.readAll().stream().map(row -> mapRowToHeaders(row, headers)).toList();
        } catch (IOException | CsvException e) {
            throw new OfficeStamperException(e);
        }
    }

    static Object processProperties(Path path) {
        var properties = new Properties();
        try (var inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            return properties.entrySet().stream().collect(toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue()), (a, b) -> b, LinkedHashMap::new));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    static Object processXmlOrHtml(Path path) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);

            var builder = factory.newDocumentBuilder();
            var document = builder.parse(Files.newInputStream(path));
            return processNode(document.getDocumentElement());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    static Map<String, Object> processNode(Element element) {
        var result = new LinkedHashMap<String, Object>();
        var children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            var node = children.item(i);
            if (node instanceof Element childElement) {
                var name = childElement.getTagName();
                if (!childElement.hasChildNodes()) {
                    result.put(name, childElement.getTextContent());
                } else {
                    var firstChild = childElement.getFirstChild();
                    if (firstChild.getNodeType() == Node.TEXT_NODE) {
                        result.put(name, childElement.getTextContent());
                    } else {
                        result.put(name, processNode(childElement));
                    }
                }
            }
        }
        return result;
    }

    static Object processJson(Path path) {
        try {
            var mapper = SerializationUtils.newMapper();
            var typeRef = new TypeReference<LinkedHashMap<String, Object>>() {
            };
            return mapper.readValue(Files.newInputStream(path), typeRef);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    static Object processYaml(Path path) {
        try {
            // Lazy YAML support using Jackson if available on classpath;
            // else, provide a clear error.
            var yaml = "com.fasterxml.jackson.dataformat.yaml.YAMLFactory";
            var mapperClass = Class.forName(yaml);
            var declaredConstructor = mapperClass.getDeclaredConstructor();
            var jsonFactory = (JsonFactory) declaredConstructor.newInstance();
            var mapper = new ObjectMapper(jsonFactory);
            var typeRef = new TypeReference<LinkedHashMap<String, Object>>() {
            };
            return mapper.readValue(Files.newInputStream(path), typeRef);
        } catch (ClassNotFoundException e) {
            var msg = "YAML support requires 'jackson-dataformat-yaml' on the" + " classpath";
            throw new OfficeStamperException(msg);
        } catch (Exception e) {
            throw new OfficeStamperException(e);
        }
    }

    static Object processExcel(Path path, ExcelMergeStrategy excelMergeStrategy, String excelJoinKey) {
        try (var is = Files.newInputStream(path)) {
            var ctx = ExcelContext.from(is);
            if (excelMergeStrategy == ExcelMergeStrategy.JOIN) {
                return ctx.joinAllSheets(excelJoinKey);
            }
            return ctx;
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    static Map<String, String> mapRowToHeaders(String[] row, String[] headers) {
        return IntStream.range(0, headers.length).boxed().collect(toMap(i -> headers[i], i -> row[i], (_, b) -> b, LinkedHashMap::new));
    }

    static Object contextualize(String model, ExcelMergeStrategy mergeStrategy, String joinKey) {
        var path = Path.of(model);
        return Files.isDirectory(path) ? contextualiseDirectory(path, mergeStrategy, joinKey) : contextualise(path, mergeStrategy, joinKey);
    }

    private static Map<String, Object> contextualiseDirectory(Path dir, ExcelMergeStrategy mergeStrategy, String joinKey) {
        try (var stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile).filter(Contextualizer::isSupportedDataFile).collect(toMap(PathUtils::baseName, path -> contextualise(path, mergeStrategy, joinKey), (_, b) -> b, TreeMap::new));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }
}
