package priv.xiaohu.common.utils.xml;

import com.alibaba.fastjson.JSON;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.bind.JAXBContext.newInstance;

/**
 * xml实体类生成xml字符或文件
 *
 * @author zxh
 * @version 1.0.0
 * @since 1.0.0
 */
public class XmlUtil {
	private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);
	
	/**
	 * <p>使用对象生成xml格式的字符串</p>
	 * <p>实体类和器其属性必须使用Dom4j的注解标记</p>
	 *
	 * @param xmlObj
	 * 		要转成xml字符串的对象
	 * @return xml格式的字符串
	 * @throws JAXBException
	 * 		创建JAXBContext实例时出现异常 可能是注解不正确
	 */
	public static String toXmlString(Object xmlObj) throws JAXBException {
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
			private final boolean ignoreNamespace = false;
			private final String rootNamespace = null;
			private boolean isRootElement = true;
			
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
	 * xml格式的字符串转换成json格式的字符串
	 *
	 * @param xmlString
	 * 		xml格式的字符串
	 * @return json格式的字符串
	 */
	public static String xmlToJson(String xmlString) {
		return XML.toJSONObject(xmlString).toString();
	}
	
	/**
	 * json格式的字符串转换成xml格式的字符串
	 *
	 * @param jsonString
	 * 		json格式的字符串
	 * @return xml格式的字符串
	 */
	public static String json2xml(String jsonString) {
		return XML.toString(new JSONObject(jsonString));
	}
	
	/**
	 * 将xml格式的字符串转化成实体对象
	 *
	 * @param xmlStr
	 * 		xml格式的字符串
	 * @param tClass
	 * 		要转化成的实体类型
	 * @param <T>
	 * 		实体类型泛型
	 * @return 转化成的实体
	 */
	public static <T> T generateXmlEntry(String xmlStr, Class<T> tClass) {
		return generateXmlEntry(new StringReader(xmlStr), tClass);
	}
	
	/**
	 * 将xml格式的字符串转化成实体对象
	 *
	 * @param reader
	 * 		包含xml的字符输入流
	 * @param tClass
	 * 		要转化成的实体类型
	 * @param <T>
	 * 		实体类型泛型
	 * @return 转化成的实体
	 */
	public static <T> T generateXmlEntry(final Reader reader, final Class<T> tClass) {
		final JAXBContext jaxbContext;
		try {
			jaxbContext = newInstance(tClass);
			final Unmarshaller u = jaxbContext.createUnmarshaller();
			return (T) u.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 将xml格式的字符串转化成实体对象
	 *
	 * @param xmlFile
	 * 		包含xml的文件
	 * @param tClass
	 * 		要转化成的实体类型
	 * @param <T>
	 * 		实体类型泛型
	 * @return 转化成的实体
	 */
	public static <T> T generateXmlEntry(File xmlFile, Class<T> tClass) throws FileNotFoundException {
		return generateXmlEntry(new BufferedReader(new FileReader(xmlFile)), tClass);
	}
	
	/**
	 * 将xml格式的字符串转化成实体对象
	 *
	 * @param inputStream
	 * 		包含xml的字节输入流
	 * @param tClass
	 * 		要转化成的实体类型
	 * @param <T>
	 * 		实体类型泛型
	 * @return 转化成的实体
	 */
	public static <T> T generateXmlEntry(InputStream inputStream, Class<T> tClass) {
		return generateXmlEntry(new BufferedReader(new InputStreamReader(inputStream)), tClass);
	}
	
	/**
	 * 格式化xml字符串
	 *
	 * @param str
	 * 		xml字符串
	 * @return 格式化后的字符串
	 * @throws DocumentException
	 * 		xml字符串格式不正确
	 * @throws IOException
	 * 		输入流异常
	 */
	public static String formatXml(final String str) throws DocumentException, IOException {
		final Document document = DocumentHelper.parseText(str);
		// 格式化输出格式
		final OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		final StringWriter writer = new StringWriter();
		// 格式化输出流
		final XMLWriter xmlWriter = new XMLWriter(writer, format);
		// 将document写入到输出流
		xmlWriter.write(document);
		xmlWriter.close();
		return writer.toString();
	}
	
	public static void main(String[] args) {
		final Map<String, String> map = Collections.singletonMap("q", "q");
		final String s = JSON.toJSONString(map);
		System.out.println(s);
		final String xml = json2xml(s);
		System.out.println(xml);
		final Map entry = generateXmlEntry(xml, HashMap.class);
		System.out.println(entry);
	}
}
