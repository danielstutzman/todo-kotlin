import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.Attributes

class SAXFindAuthenticityToken : DefaultHandler() {
  var foundToken: String? = null

  override fun startElement(namespaceURI: String,
                            localName: String,
                            qName: String,
                            atts: Attributes) {
    if (localName == "INPUT") {
      var isNameEqAuthenticityToken = false

      for (i in 0..atts.length - 1) {
        if (atts.getLocalName(i) == "name") {
          if (atts.getValue(i) == "authenticity_token") {
            isNameEqAuthenticityToken = true
          }
        }
      }

      if (isNameEqAuthenticityToken) {
        for (i in 0..atts.length - 1) {
          if (atts.getLocalName(i) == "value") {
            this.foundToken = atts.getValue(i)
          }
        }
      }
    }
  }
}
