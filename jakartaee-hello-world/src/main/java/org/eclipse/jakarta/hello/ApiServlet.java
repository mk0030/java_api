package org.eclipse.jakarta.hello;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@WebServlet("/films")
public class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public ApiServlet() {
        super();
        
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");
        String filePath = getServletContext().getRealPath("/WEB-INF/films.xml");
        File xmlFile = new File(filePath);
        FileInputStream fis = new FileInputStream(xmlFile);

        PrintWriter out = response.getWriter();
        int content;
        while ((content = fis.read()) != -1) {
            out.write(content);
        }

        fis.close();
        out.flush();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String titre = request.getParameter("titre");
        String realisateur = request.getParameter("realisateur");
        String annee = request.getParameter("annee");
        String genre = request.getParameter("genre");
        String description = request.getParameter("description");
        if ( titre == null || realisateur == null || annee == null || genre == null || description == null ) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tous les paramètres sont obligatoires.");
            return;
        }
        try {
            String filePath = getServletContext().getRealPath("/WEB-INF/films.xml");
            File xmlFile = new File(filePath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            Element root = doc.getDocumentElement();

            // Générer un ID aléatoire
            String id = UUID.randomUUID().toString();

            Element newFilm = doc.createElement("film");

            Element newId = doc.createElement("id");
            newId.appendChild(doc.createTextNode(id));
            newFilm.appendChild(newId);

            Element newTitre = doc.createElement("titre");
            newTitre.appendChild(doc.createTextNode(titre));
            newFilm.appendChild(newTitre);

            Element newRealisateur = doc.createElement("realisateur");
            newRealisateur.appendChild(doc.createTextNode(realisateur));
            newFilm.appendChild(newRealisateur);

            Element newAnnee = doc.createElement("annee");
            newAnnee.appendChild(doc.createTextNode(annee));
            newFilm.appendChild(newAnnee);

            Element newGenre = doc.createElement("genre");
            newGenre.appendChild(doc.createTextNode(genre));
            newFilm.appendChild(newGenre);

            Element newDescription = doc.createElement("description");
            newDescription.appendChild(doc.createTextNode(description));
            newFilm.appendChild(newDescription);


            root.appendChild(newFilm);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
            transformer.transform(source, result);

            response.setStatus(HttpServletResponse.SC_CREATED);
            PrintWriter out = response.getWriter();
            out.println("Film ajouté avec succès.");
            out.flush();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de l'ajout du film.");
            e.printStackTrace();
        }
	}
	
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		
		try {
            // Obtenez l'ID du film à partir des paramètres de la requête
            String id = req.getParameter("id");
            
            if (id == null || id.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID is required");
                return;
            }
         // Lire les autres paramètres pour les nouvelles données du film
            String titre = req.getParameter("titre");
            String realisateur = req.getParameter("realisateur");
            String annee = req.getParameter("annee");
            String genre = req.getParameter("genre");
            String description = req.getParameter("description");
            
            
            // Charger et parser le fichier XML
            String filePath = getServletContext().getRealPath("/WEB-INF/films.xml");
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
         // Normaliser le document XML
            doc.getDocumentElement().normalize();

            // Trouver le film par ID et mettre à jour ses données
            NodeList filmList = doc.getElementsByTagName("film");
            boolean filmFound = false;
            for (int i = 0; i < filmList.getLength(); i++) {
                Node filmNode = filmList.item(i);
                if (filmNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element filmElement = (Element) filmNode;
                    if (filmElement.getAttribute("id").equals(id)) {
                        filmFound = true;

                        // Mettre à jour les éléments du film
                        updateElementValue(filmElement, "titre", titre);
                        updateElementValue(filmElement, "realisateur", realisateur);
                        updateElementValue(filmElement, "annee", annee);
                        updateElementValue(filmElement, "genre", genre);
                        updateElementValue(filmElement, "description", description);
                        
                        break;
                    }
                }
            }
            if (!filmFound) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Film not found");
                return;
            }

            // Écrire les modifications dans le fichier XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            // Envoyer une réponse de succès
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating film");
        }
         
	}
	private void updateElementValue(Element parentElement, String tagName, String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            NodeList nodeList = parentElement.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                node.setTextContent(newValue);
            }
        }
    }
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 try {
	            // Obtenez l'ID du film à partir des paramètres de la requête
	            String id = request.getParameter("id");
	            
	            if (id == null || id.isEmpty()) {
	                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID is required");
	                return;
	            }
	            String filePath = getServletContext().getRealPath("/WEB-INF/films.xml");
	            File xmlFile = new File(filePath);
	            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	            Document doc = dBuilder.parse(xmlFile);

	            // Normaliser le document XML
	            doc.getDocumentElement().normalize();

	            // Trouver le film par ID et le supprimer
	            NodeList filmList = doc.getElementsByTagName("film");
	            boolean filmFound = false;
	            for (int i = 0; i < filmList.getLength(); i++) {
	                Node filmNode = filmList.item(i);
	                if (filmNode.getNodeType() == Node.ELEMENT_NODE) {
	                    Element filmElement = (Element) filmNode;
	                    if (filmElement.getAttribute("id").equals(id)) {
	                        filmFound = true;
	                        System.out.println("Film found with ID: " + id); // Log that the film is found
	                        filmElement.getParentNode().removeChild(filmElement);
	                        break;
	                    }
	                }
	            }

	            if (!filmFound) {
	                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Film not found");
	                return;
	            }

	            // Écrire les modifications dans le fichier XML
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            DOMSource source = new DOMSource(doc);
	            StreamResult result = new StreamResult(xmlFile);
	            transformer.transform(source, result);

	            // Envoyer une réponse de succès
	            response.setStatus(HttpServletResponse.SC_OK);
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting film");
	        }
	}
	
}
