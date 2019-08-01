package org.kie.maven.blueprinter.plugin

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


/**
 * Functions using **SAX**
 */

fun getNode(document: Document, tagName: String): Node = document.getElementsByTagName(tagName).item(0)


fun getEmptyDocument(): Document {
    val dbf = DocumentBuilderFactory.newInstance()
    val builder = dbf.newDocumentBuilder()
    return builder.newDocument()
}

fun getDocument(xmlFile: File): Document {
    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val xmlInput = InputSource(StringReader(xmlFile.readText()))
    return dBuilder.parse(xmlInput)
}


fun getHtmlString(document: Document): String {
    val domSource = DOMSource(document)
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    transformer.setOutputProperty("method", "html")
    val sw = StringWriter()
    val sr = StreamResult(sw)
    transformer.transform(domSource as Source?, sr)
    return sw.toString()
}