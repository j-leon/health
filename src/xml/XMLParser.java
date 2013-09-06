package xml;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/*
 * Esta clase sirve de util para parsear el xml contenido en el codigo QR.
 */
public class XMLParser {
	private String xml;

	public XMLParser(String xml) {
		this.xml = xml;
	}

	public HashMap<String, String> getDataFromXML() throws Exception{
		HashMap<String, String> mapa = new HashMap<String, String>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();	
			InputSource inStream = new InputSource();
	        inStream.setCharacterStream(new StringReader(this.xml));	        
			Document dom = builder.parse(inStream);			
			org.w3c.dom.Element root = dom.getDocumentElement();			
			NodeList items = root.getElementsByTagName("cardHolder");			
			mapa.put("cardHolderID", root.getAttribute("documentID"));			
			NodeList card = dom.getElementsByTagName("card");						
			for (int i=0; i<items.getLength(); i++)
			{
				Node item = items.item(i);				
				NodeList datosCardHolder = item.getChildNodes();				
				for (int j=0; j<datosCardHolder.getLength(); j++)
				{
					Node dato = datosCardHolder.item(j);					
					String etiqueta = dato.getNodeName();
					if (etiqueta.equals("name"))
					{	
						Element e_name = (Element)dato;
						mapa.put("title", e_name.getAttribute("title"));
						mapa.put("name", this.getTagText(dato));
					}
					if(etiqueta.equals("document")){
						Element e_document = (Element)dato;
						mapa.put("document_type", e_document.getAttribute("type"));
						mapa.put("document_id", this.getTagText(dato));
					}
					
					if(etiqueta.equals("gender")){
						mapa.put("gender", this.getTagText(dato));
					}
					
					if(etiqueta.equals("card")){
						Element e_dato = (Element)dato;
						mapa.put("cardType", e_dato.getAttribute("cardType"));
						mapa.put("card_creationDate", e_dato.getAttribute("creationDate"));
						NodeList card_datos = dato.getChildNodes();	
						for (int k = 0; k<card_datos.getLength();k++){							
							mapa.put("card_" + card_datos.item(k).getNodeName(), this.getTagText(card_datos.item(k)));
							if(card_datos.item(k).getNodeName().equals("issuer")){
								Element issuer = (Element)card_datos.item(k);
								mapa.put("issuer_is_lab", issuer.getAttribute("isLab"));
							}
						}
					}
					
				}
			}			
			/**/

		} catch (ParserConfigurationException e) {
			throw new Exception("Error al interpretar los datos desde la fuente XML: " + e.getMessage());
		} catch (Exception e) {
			throw new Exception("Error al leer los datos del XML: " + e.getMessage());
		}
			
		return mapa;
	}
	private String getTagText(Node dato) {
		StringBuilder texto = new StringBuilder();
		NodeList fragmentos = dato.getChildNodes();

		for (int k=0;k<fragmentos.getLength();k++)
		{
			texto.append(fragmentos.item(k).getNodeValue());
		}

		return texto.toString();
	}

}
