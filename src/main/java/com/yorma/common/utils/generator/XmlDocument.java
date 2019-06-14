package com.yorma.common.utils.generator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

import static org.dom4j.DocumentHelper.parseText;

/**
 * xml文档工具类
 * @author zxh
 */
public class XmlDocument {
    private static Document document;

    private static Element target;

    public XmlDocument(String xmlStr) {
        document = parseToXmlDocument(xmlStr);
    }

    public Document getDocument() {
        return document;
    }

    public Element getRootElement() {
        return document.getRootElement();
    }

    /**
     * 将xml格式的字符串解析成Document文档对象
     *
     * @param xmlStr xml格式的字符串
     * @return xml的document对象
     */
    public Document parseToXmlDocument(String xmlStr) {
        try {
            // 解析字符串内容生成xml节点
            return parseText(xmlStr);
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("xml内容解析失败");
        }
    }

    /**
     * <p>获取指定路径的节点</p>
     *
     * @param elementNameSelector 元素路径
     * @return 所有符合指定路径的节点
     */
    public List<Element> getElements(String elementNameSelector) {

        final List<Element> elements = new ArrayList<>();

        // 获取根节点进行遍历
        final Element rootElement = document.getRootElement();

        final String[] elementNames = elementNameSelector.split(">");
        for (String elementName : elementNames) {
            traversalElements(elements, rootElement, elementName.trim(), elementNames[elementNames.length - 1]);
        }
        return elements;
    }

    /**
     * <p>递归遍历xml文件的各个节点和其子节点</p>
     * <p>找到第一个名为elementName节点</p>
     *
     * @param rootElement 要遍历的父节点
     * @param elementName 要查找的节点名
     */
    private void traversalElements(List<Element> collect, Element rootElement, String elementName, String targetName) {
        if (rootElement != null) {
            if (elementName.equals(rootElement.getName())) {
                return;
            }
            final List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                if (!elementName.equals(element.getName())) {
                    traversalElements(collect, element, elementName, targetName);
                } else {
                    if (element.getName().equals(targetName)) {
                        collect.add(element);
                    }
                }
            }
        }
    }

    /**
     * <p>获取指定路径的节点</p>
     *
     * @param elementNameSelector 元素路径
     * @return 所有符合指定路径的节点
     */
    public Element getElement(String elementNameSelector) {


        // 获取根节点进行遍历
        target = document.getRootElement();

        final String[] elementNames = elementNameSelector.split(">");
        for (String elementName : elementNames) {
            traversalElement(target, elementName.trim());
        }
        return target;
    }

    /**
     * <p>递归遍历xml文件的各个节点和其子节点</p>
     * <p>找到第一个名为elementName节点</p>
     *
     * @param rootElement 要遍历的父节点
     * @param elementName 当前查找的节点名
     */
    private void traversalElement(Element rootElement, String elementName) {
        if (rootElement != null) {
            if (elementName.equals(rootElement.getName())) {
                target = rootElement;
                return;
            }
            final List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                if (!elementName.equals(element.getName())) {
                    traversalElement(element, elementName);
                } else {
                    target = element;
                }
            }
        }
    }
}
