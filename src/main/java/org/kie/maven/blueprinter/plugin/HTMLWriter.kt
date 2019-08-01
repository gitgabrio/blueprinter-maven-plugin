package org.kie.maven.blueprinter.plugin

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File

/**
 * Functions used to write html files
 */

fun createHTMLFiles(svgFilesMap: Map<File, HashSet<File>>) {
    svgFilesMap.entries.forEach { createHTMLFile(it) }
}


private fun createHTMLFile(svgFilesEntry: Map.Entry<File, HashSet<File>>) {
    val originalFileName = svgFilesEntry.key.absolutePath
    val htmlFileName = originalFileName.substring(0, originalFileName.lastIndexOf(".") + 1) + "html"
    val htmlFile = File(htmlFileName)
    val htmlDocument = initHTML(htmlFile)
    val bodyElement = getBodyElement(htmlDocument)
    svgFilesEntry.value.forEach { addSVGFile(bodyElement, it, htmlDocument) }
    writeHtmlDocument(htmlFile, htmlDocument)
}


private fun addSVGFile(bodyElement: Element, svgFile: File, htmlDocument: Document) {
    val importedSVG = htmlDocument.importNode(getSVGNode(svgFile), true)
    bodyElement.appendChild(importedSVG)
}

private fun getSVGNode(svgFile: File): Node {
    val svgDocument = getDocument(svgFile)
    return getNode(svgDocument, "svg")
}

private fun initHTML(htmlFile: File) : Document {
    val toReturn = getEmptyDocument()
    val htmlElement = toReturn.createElement("html")
    toReturn.appendChild(htmlElement)
    val headElement = toReturn.createElement("head")
    htmlElement.appendChild(headElement)
    val titleElement = toReturn.createElement("title")
    titleElement.textContent = "${htmlFile.nameWithoutExtension}"
    headElement.appendChild(titleElement)
    return toReturn
}

private fun getBodyElement(htmlDocument: Document) : Element {
    val toReturn = htmlDocument.createElement("body")
    htmlDocument.getElementsByTagName("html").item(0).appendChild(toReturn)
    return toReturn
}

private fun writeHtmlDocument(htmlFile: File, htmlDocument: Document) {
    val htmlString = getHtmlString(htmlDocument)
    htmlFile.writeText(htmlString)
}


