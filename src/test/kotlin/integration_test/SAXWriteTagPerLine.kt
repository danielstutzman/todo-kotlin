package integration_test

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.Writer

public class SAXWriteTagPerLine(val writer: Writer) : DefaultHandler() {
  override fun startElement(namespaceURI: String,
                            localName: String,
                            qName: String,
                            atts: Attributes) {
    val attrMap = LinkedHashMap<String, String>() // preserve order
    for (i in 0..atts.length - 1) {
      val name = atts.getLocalName(i)
      val value = atts.getValue(i)
      attrMap[name] = value
    }

    val tag = localName.toLowerCase()
    if (tag == "meta" && attrMap["name"] == "csrf-token") {
      attrMap["content"] = "MASKED"
    } else if (tag == "input" && attrMap["name"] == "authenticity_token") {
      attrMap["value"] = "MASKED"
    }

    writer.write("<${tag}")
    for ((name, value) in attrMap) {
      if (value.contains(' ')) {
        writer.write(" ${name}='${value}'")
      } else {
        writer.write(" ${name}=${value}")
      }
    }
    writer.write(">\n")
  }

  override fun characters(ch: CharArray?, start: Int, length: Int) {
    val s = ch!!
        .joinToString("")
        .substring(start, start + length)
        .trim()
    if (s != "") {
      writer.write(s)
      writer.write("\n")
    }
  }

  override fun endElement(uri: String?, localName: String?, qName: String?) {
    val tag = localName!!.toLowerCase()
    writer.write("</${tag}>\n")
  }
}
