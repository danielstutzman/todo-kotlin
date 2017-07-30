package integration_test

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.Writer

public class SAXWriteTagPerLine(val writer: Writer) : DefaultHandler() {
  override fun startElement(namespaceURI: String,
                            localName: String,
                            qName: String,
                            atts: Attributes) {
    val tag = localName!!.toLowerCase()
    writer.write("<${tag}") // e.g. <input
    for (i in 0..atts.length - 1) {
      val name = atts.getLocalName(i)
      val value = atts.getValue(i)
      writer.write(" ${name}=${value}")
    }
    writer.write(">\n")
  }

  override fun endElement(uri: String?, localName: String?, qName: String?) {
    val tag = localName!!.toLowerCase()
    writer.write("</${tag}>\n")
  }
}
