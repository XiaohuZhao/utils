package com.yorma.common.utils.generator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.List;

import static org.dom4j.DocumentHelper.parseText;

/**
 * xml实体类生成xml字符或文件
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/28
 * @since 1.0.0
 */
public class XmlGenerator {
    private static final Logger logger = LoggerFactory.getLogger(XmlGenerator.class);

    private static Element target;

    public static String generateXmlStr(Transformable xmlObj) throws JAXBException {
        // 设置xml装配器
        final JAXBContext context = JAXBContext.newInstance(xmlObj.getClass());
        final Marshaller marshaller = context.createMarshaller();
        // 编码格式
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        // 是否格式化生成的XML串
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // 是否省略XML头信息
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);

        // 生成xml文件内容
        final StringWriter out = new StringWriter();
        final OutputFormat format = new OutputFormat();
        format.setIndent(true);
        format.setNewlines(true);
        format.setNewLineAfterDeclaration(false);
        final XMLWriter xmlWriter = new XMLWriter(out, format);
        final XMLFilterImpl xmlFilter = new XMLFilterImpl() {
            private boolean ignoreNamespace = false;
            private String rootNamespace = null;
            private boolean isRootElement = true;

            @Override
            public void startDocument() throws SAXException {
                super.startDocument();
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                if (this.ignoreNamespace) {
                    uri = "";
                }
                if (this.isRootElement) {
                    this.isRootElement = false;
                } else {
                    final String xmlns = "xmlns";
                    if (!"".equals(uri) && !localName.contains(xmlns)) {
                        localName = localName + " xmlns=\"" + uri + "\"";
                    }
                }

                super.startElement(uri, localName, localName, atts);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (this.ignoreNamespace) {
                    uri = "";
                }
                super.endElement(uri, localName, localName);
            }

            @Override
            public void startPrefixMapping(String prefix, String url) throws SAXException {
                if (this.rootNamespace != null) {
                    url = this.rootNamespace;
                }
                if (!this.ignoreNamespace) {
                    super.startPrefixMapping("", url);
                }
            }
        };
        xmlFilter.setContentHandler(xmlWriter);
        marshaller.marshal(xmlObj, xmlFilter);
        return out.toString();
    }

    /**
     * <p>解析XML文件,获取指定节点的值</p>
     *
     * @param xmlStr xml字符串
     * @return 指定节点的值
     */
    public static String getElementValue(String xmlStr, String elementNameSelector) {
        Document xmlDocument;
        try {
            // 解析字符串内容生成xml节点
            xmlDocument = parseText(xmlStr);
        } catch (DocumentException e) {
            e.printStackTrace();
            logger.info("解析xml内容时出现异常:[{}]", e.getMessage());
            throw new RuntimeException("xml内容解析失败");
        }

        // 获取根节点进行遍历
        target = xmlDocument.getRootElement();

        final String[] elementNames = elementNameSelector.split(">");
        for (String elementName : elementNames) {
            traversalElement(target, elementName.trim());
        }
        // 如果遍历后节点依然为空, 说明没有找到此节点
        if (!target.getName().equals(elementNames[elementNames.length - 1])) {
            logger.info(String.format("xml内容中找不到指定节点\"%s\"", elementNameSelector));
            throw new RuntimeException(String.format("xml内容中找不到指定节点\"%s\"", elementNameSelector));
        }
        return target.getText();
    }

    /**
     * <p>递归遍历xml文件的各个节点和其子节点</p>
     * <p>找到第一个名为elementName节点</p>
     *
     * @param rootElement 要遍历的父节点
     * @param elementName 要查找的节点名
     */
    @SuppressWarnings("unchecked")
    private static void traversalElement(Element rootElement, String elementName) {
        if (rootElement != null) {
            if (elementName.equals(rootElement.getName())) {
                return;
            }
            final List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                if (!elementName.equals(element.getName())) {
                    traversalElement(element, elementName);
                } else {
                    target = element;
                    return;
                }
            }
        }
    }
}
