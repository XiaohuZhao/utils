package com.yorma.common.utils.generator;

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
}
