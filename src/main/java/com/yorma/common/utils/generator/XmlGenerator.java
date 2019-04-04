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
    /**
     * 储存找到的指定节点
     */
    private static Element element = null;

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
     * 解析XML文件,获取报文文件对应的文件名(不包含扩展名)
     *
     * @param xmlStr 带有报关类型的xml字符串
     * @return 报文文件的messageId
     */
    public static String getXmlFileName(String xmlStr) {
        // 获取文件名并使用正则表达式判断文件名是否符合规则
        String regMessageId = "^CN_(MT2201|MT3201|MT4201|MT5202)_[1-9]p[0-9]_4302560757589_\\d{17}.xml$";
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
        Element rootElement = xmlDocument.getRootElement();
        traversalElement(rootElement);
        // 如果遍历后节点依然为空, 说明没有找到此节点
        if (element == null) {
            logger.info("xml内容中找不到指定节点\"MessageID\"");
            throw new RuntimeException("xml内容中找不到指定节点\"MessageID\"");
        }
        String xmlFileName = element.getText() + ".xml";

        if (!xmlFileName.matches(regMessageId)) {
            logger.info("xml内容不正确");
            throw new IllegalArgumentException("xml内容不正确");
        }
        return xmlFileName;
    }

    /**
     * <p>递归遍历xml文件的各个节点和其子节点</p>
     * <p>找到MessageID节点</p>
     *
     * @param rootElement 要遍历的父节点
     */
    @SuppressWarnings({"unchecked", "LoopStatementThatDoesntLoop"})
    private static void traversalElement(final Element rootElement) {
        final List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            if (!"MessageID".equals(element.getName())) {
                traversalElement(element);
            } else {
                XmlGenerator.element = element;
            }
            return;
        }
    }
}
