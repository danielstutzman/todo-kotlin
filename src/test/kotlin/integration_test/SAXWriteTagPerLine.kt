package integration_test

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.Writer

public class SAXWriteTagPerLine(val writer: Writer) : DefaultHandler() {
  var hadCharacters = false

  override fun startElement(namespaceURI: String,
                            localName: String,
                            qName: String,
                            atts: Attributes) {
    if (hadCharacters) {
      writer.write("\n")
      hadCharacters = false
    }

    val attrMap = LinkedHashMap<String, String>() // preserve order
    for (i in 0..atts.length - 1) {
      val attrName = atts.getLocalName(i)
      val attrValue = atts.getValue(i)

      if (attrName == "class") {
        // Remove whitespace inside class attributes
        attrMap[attrName] = attrValue.trim().replace(Regex("\\s+"), " ")
      } else {
        attrMap[attrName] = attrValue
      }
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
        if (name == "class") {
          val valueSorted = value.split(" ").sorted().joinToString(" ")
          writer.write(" ${name}='${valueSorted}'")
        } else {
          writer.write(" ${name}='${value}'")
        }
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
      hadCharacters = true
    }
  }

  override fun endElement(uri: String?, localName: String?, qName: String?) {
    if (hadCharacters) {
      writer.write("\n")
      hadCharacters = false
    }

    val tag = localName!!.toLowerCase()
    writer.write("</${tag}>\n")
  }
}
