package com.payline.payment.wechatpay.util;

import com.payline.payment.wechatpay.exception.PluginException;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class XMLService {
    private static class Holder {
        private static final XMLService instance = new XMLService();
    }

    public static XMLService getInstance() {
        return XMLService.Holder.instance;
    }


    public String mapToXml(Map<String, String> data) {
        try {
            Document document = newDocument();
            Element root = document.createElement("xml");
            document.appendChild(root);

            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue().trim() : "";

                Element filed = document.createElement(key);
                filed.appendChild(document.createTextNode(value));
                root.appendChild(filed);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // Compliant

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            String output = writer.getBuffer().toString();
            writer.close();

            return output;

        } catch (IOException | ParserConfigurationException | TransformerException e) {
            String errorMessage = "unable to convert Map into a valid XML String";
            log.error(errorMessage, e);
            throw new PluginException(errorMessage, e);
        }
    }

    public Map<String, String> xmlToMap(String s) {
        Map<String, String> data = new HashMap<>();
        try {
            DocumentBuilder documentBuilder = newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }

            stream.close();

        } catch (ParserConfigurationException e) {
            log.info("Parser configuration error", e);
            throw new PluginException("Plugin error: Parser configuration error", e);
        } catch (IOException e) {
            log.info("InputStream error", e);
            throw new PluginException("Plugin error: InputStream error", e);
        } catch (SAXException e) {
            log.info("Parsing error", e);
            throw new PluginException("Plugin error: Parsing error", e);
        }

        return data;
    }


    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);

        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant

        return documentBuilderFactory.newDocumentBuilder();
    }

    public static Document newDocument() throws ParserConfigurationException {
        return newDocumentBuilder().newDocument();
    }
}
