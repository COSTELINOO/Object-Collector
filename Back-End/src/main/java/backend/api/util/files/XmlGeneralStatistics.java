package backend.api.util.files;

import backend.api.config.applicationConfig.Properties;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.GlobalStatistics.GeneralStatistics;
import backend.api.model.GlobalStatistics.Clasament;

import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class XmlGeneralStatistics {

    // Constante pentru configurare
    private static final String RSS_VERSION = "2.0";
    private static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
    private static final String FEED_TITLE = "OCo Statistics";
    private static final String FEED_DESCRIPTION = "Statistici globale ale platformei Object Collector";
    private static final String GENERATOR_NAME = "OCo RSS Generator";
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
    private static final int TOP_ITEMS_COUNT = 3;

    public static void generateSimpleRssXml(GeneralStatistics stats) throws CustomException {
        if (stats == null) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "NullStatistics",
                    "Obiectul cu statistici este null"
            );
        }

        Logger.info("Generare XML RSS cu statistici generale");

        try {
            Document doc = createSecureXmlDocument();
            Logger.debug("Document XML creat");

            Element rss = doc.createElement("rss");
            rss.setAttribute("version", RSS_VERSION);
            rss.setAttribute("xmlns:atom", ATOM_NAMESPACE);
            doc.appendChild(rss);

            Element channel = doc.createElement("channel");
            rss.appendChild(channel);

            String baseUrl = getBaseUrl();
            String pubDate = formatDate(new Date());

            Element atomLink = doc.createElementNS(ATOM_NAMESPACE, "atom:link");
            atomLink.setAttribute("href", baseUrl + "/statistics/public/rss");
            atomLink.setAttribute("rel", "self");
            atomLink.setAttribute("type", "application/rss+xml");
            channel.appendChild(atomLink);

            addChannelMetadata(doc, channel, baseUrl, pubDate);
            Logger.debug("Metadata canal RSS adăugată");

            addGeneralStatistics(doc, channel, stats);
            Logger.debug("Statistici generale adăugate");

            addTopItemsStatistics(doc, channel, stats);
            Logger.debug("Statistici de top adăugate");

            String xmlString = transformDocumentToString(doc);

            saveToFile(xmlString, getOutputFilePath());
            Logger.success("XML RSS cu statistici generale generat cu succes");
        } catch (ParserConfigurationException e) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "ParserConfigurationFailed",
                    "Eroare la configurarea parser-ului XML: " + e.getMessage(),
                    e
            );
        } catch (TransformerException e) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "TransformationFailed",
                    "Eroare la transformarea documentului XML: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "GenerationFailed",
                    "Eroare la generarea XML-ului RSS: " + e.getMessage(),
                    e
            );
        }
    }

    private static Document createSecureXmlDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        docFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        docFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        docFactory.setXIncludeAware(false);
        docFactory.setExpandEntityReferences(false);

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    private static String getBaseUrl() {
        return "http://localhost:1111";
    }

    private static String getOutputFilePath() {
        return Properties.getPath() +  File.separator+"flux"+ File.separator+"statistics_rss.xml";
    }

    private static void addChannelMetadata(Document doc, Element channel, String baseUrl, String pubDate) {
        addElement(doc, channel, "title", FEED_TITLE);
        addElement(doc, channel, "description", FEED_DESCRIPTION);
        addElement(doc, channel, "link", baseUrl + "/statistics/public/rss");
        addElement(doc, channel, "lastBuildDate", pubDate);
        addElement(doc, channel, "generator", GENERATOR_NAME);
    }

    private static void addGeneralStatistics(Document doc, Element channel, GeneralStatistics stats) {
        addItemWithMetadata(doc, channel, "totalCollections", stats.getTotalCollections().toString());
        addItemWithMetadata(doc, channel, "totalObjects", stats.getTotalObjects().toString());
        addItemWithMetadata(doc, channel, "totalValue", stats.getTotalValue().toString());
        addItemWithMetadata(doc, channel, "lastMonth", stats.getLastMonth().toString());

        addItemWithMetadata(doc, channel, "procentMonede", stats.getProcentMonede().toString());
        addItemWithMetadata(doc, channel, "procentTablouri", stats.getProcentTablouri().toString());
        addItemWithMetadata(doc, channel, "procentTimbre", stats.getProcentTimbre().toString());
        addItemWithMetadata(doc, channel, "procentVinil", stats.getProcentVinil().toString());
        addItemWithMetadata(doc, channel, "procentCustom", stats.getProcentCustom().toString());
    }

    private static void addTopItemsStatistics(Document doc, Element channel, GeneralStatistics stats) {
        String[][] objectArrays = {
                {"topLikeObjId_", "topLikeObjName_", "topLikeObjValue_", "topLikeObjVisible_"},
                {"topViewObjId_", "topViewObjName_", "topViewObjValue_", "topViewObjVisible_"},
                {"topPriceObjId_", "topPriceObjName_", "topPriceObjValue_", "topPriceObjVisible_"}
        };

        String[][] collectionArrays = {
                {"topLikeColId_", "topLikeColName_", "topLikeColValue_", "topLikeColVisible_"},
                {"topViewColId_", "topViewColName_", "topViewColValue_", "topViewColVisible_"},
                {"topPriceColId_", "topPriceColName_", "topPriceColValue_", "topPriceColVisible_"}
        };


        List<Clasament>[] objectLists = new List[]{
                stats.getMostLikedObjects(),
                stats.getMostViewedObjects(),
                stats.getMostValuableObjects()
        };

        List<Clasament>[] collectionLists = new List[]{
                stats.getMostLikedCollections(),
                stats.getMostViewedCollections(),
                stats.getMostValuableCollections()
        };

        for (int listIndex = 0; listIndex < objectLists.length; listIndex++) {
            List<Clasament> list = objectLists[listIndex];
            String[] keys = objectArrays[listIndex];
            processRankingList(doc, channel, list, keys);
        }

        for (int listIndex = 0; listIndex < collectionLists.length; listIndex++) {
            List<Clasament> list = collectionLists[listIndex];
            String[] keys = collectionArrays[listIndex];
            processRankingList(doc, channel, list, keys);
        }
    }

    private static void processRankingList(Document doc, Element channel, List<Clasament> list, String[] keys) {
        for (int i = 0; i < TOP_ITEMS_COUNT; i++) {
            if (i < list.size()) {
                for (int j = 0; j < 4; j++) {
                    String title = keys[j] + (i + 1);
                    String value = list.get(i).getInfo((long) j);
                    addItemWithMetadata(doc, channel, title, value);
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    String title = keys[j] + (i + 1);
                    addItemWithMetadata(doc, channel, title, "");
                }
            }
        }
    }

    private static void addItemWithMetadata(Document doc, Element channel, String title, String description) {
        Element item = doc.createElement("item");

        addElement(doc, item, "title", title);
        addElement(doc, item, "description", description);

        Element guid = doc.createElement("guid");
        guid.setAttribute("isPermaLink", "false");
        guid.appendChild(doc.createTextNode(title + "-" + UUID.randomUUID()));
        item.appendChild(guid);

        channel.appendChild(item);
    }

    private static void addElement(Document doc, Element parent, String tag, String value) {
        Element elem = doc.createElement(tag);
        elem.appendChild(doc.createTextNode(value != null ? value : ""));
        parent.appendChild(elem);
    }

    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return sdf.format(date);
    }

    private static String transformDocumentToString(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        return writer.toString();
    }

    public static void saveToFile(String xmlContent, String filePath) throws CustomException {
        if (xmlContent == null || xmlContent.isEmpty()) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "EmptyContent",
                    "Conținutul XML este gol"
            );
        }

        if (filePath == null || filePath.isEmpty()) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "InvalidFilePath",
                    "Calea fișierului este invalidă"
            );
        }

        Logger.info("Salvare XML în fișier: " + filePath);

        try {
            Path fileDirectory = Paths.get(filePath).getParent();
            if (fileDirectory != null && !Files.exists(fileDirectory)) {
                Files.createDirectories(fileDirectory);
                Logger.debug("Director creat pentru XML: " + fileDirectory);
            }

            DocumentBuilderFactory factory;
            factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);

            Logger.success("XML salvat cu succes în: " + filePath);
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "SaveFailed",
                    "Eroare la salvarea XML în fișier: " + e.getMessage(),
                    e
            );
        }
    }
}