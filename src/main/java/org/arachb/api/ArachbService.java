/**
 * 
 */
package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;


/**
 * @author pmidford
 *
 */
public class ArachbService extends HttpServlet {


	//needs to extract target uri from request,
	//figure out what class this is or what it is an instance of
	//for publication individuals, find an rdfs:comment with the json record, parse, return or format
	//for taxon individuals - find parent, maybe some new comments


	final static String ARACHBPREFIX = "http://arachb.org";
	final static String TOSTRIP = "/spider-behavior";

	private static final long serialVersionUID = 1L;

	private final static File BASEDIR = new File(Util.ADUNAHOME);


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {


		final OutputStream os = response.getOutputStream();

		boolean writeHTML;
		String target;
		String[] targetComponents = request.getRequestURI().substring(TOSTRIP.length()).split("\\.");
		if (targetComponents.length == 0 || targetComponents.length > 2){
			Util.noResultsError(os);
		}
		else{
			target = ARACHBPREFIX + targetComponents[0];
			if (targetComponents.length == 2){
				if ("json".equalsIgnoreCase(targetComponents[1])){
					writeHTML = false;
				}
				else {
					writeHTML = true;
				}
			}
			else {
				writeHTML = true;
			}
			Repository repo = null;
			RepositoryConnection con = null;
			final LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
			try {
				manager.initialize();
				repo = manager.getRepository(Util.REPONAME);
				con = repo.getConnection();
				final StringBuilder msgBuffer = new StringBuilder();
				String commentString = null;
				final String commentQueryString = getURI2CommentQuery(target);
				System.out.println("Query String is: " + commentQueryString);
				List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();
				resultList = Util.tryAndAccumulateQueryResult(resultList, commentQueryString, con);
				PublicationSummary ps = new PublicationSummary();
				if (!resultList.isEmpty()){
					for (TupleQueryResult tqr : resultList){
						while (tqr.hasNext() && commentString==null){
							BindingSet bs = tqr.next();
							Binding b = bs.getBinding("comment");
							if (b != null){
								Value v = b.getValue();
								commentString = v.stringValue().trim();
							}
						}
					}
					if (jsonCheck(commentString)){
						JsonFactory jsonF = new JsonFactory();
						JsonParser p = jsonF.createParser(commentString);
						ps = read(p);  //overwrites default
					}
				}
				if (!ps.containsKey("localIdentifier")){
					ps.put("localIdentifier", target);
				}
				if (writeHTML){
					msgBuffer.append(generateHTML(ps));
				}
				else {
					msgBuffer.append(generatejson(commentString));
				}
				response.getOutputStream().write(msgBuffer.toString().getBytes("UTF-8"));

			} catch (RepositoryException e) {  //TODO - make these return meaningful JSON strings
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				cleanupResources(repo,con);
			}
		}
		os.flush();
		os.close();
	}


	private boolean jsonCheck(String comment){
		return !comment.isEmpty() && 
				(comment.charAt(0) == '{') &&
				(comment.charAt(comment.length()-1) == '}');

	}

	
	PublicationSummary read(JsonParser jp) throws IOException{
		// Sanity check: verify that we got "Json Object":
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected data to start with an Object");
		}
		PublicationSummary result = new PublicationSummary();
		// Iterate over object fields:
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jp.getCurrentName();
			// Let's move to value
			jp.nextToken();	
			result.put(fieldName,jp.getText());
		}
		jp.close(); // important to close both parser and underlying File reader
		return result;
	}



	String getURI2CommentQuery(String uriStr){
		SparqlBuilder b = SparqlBuilder.startSparql();
		b.addText("SELECT ?comment %n");
		String line2 = String.format("WHERE {<%s> rdfs:comment ?comment . } %n", uriStr);
		b.addText(line2);
		return b.finish();
	}



	//Probably not really this simple
	private String generatejson(String jsonString){
		return jsonString;
	}

	private String generateHTML(PublicationSummary pubdata) throws IOException{
		String result = "";
		result += "<!DOCTYPE html>";
		result += "<html lang=\"en\">";
		result += "<head>";
		result += "   <meta charset=\"utf-8\"/>";
		if (pubdata.containsKey("title")){
			result += String.format("   <title>%s</title>",pubdata.get("title"));
		}
		else {
			result += String.format("   <title>%s</title>",pubdata.get("localIdentifier"));			
		}
		result += "   <meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\" />";
		result += "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>";
		result += "   <meta name=\"description\" content=\"generated publication page\"/>";
		result += "   <meta name=\"author\" content=\"Peter E. Midford\"/>";
		result += "   <link href=\"../static/bootstrap/css/bootstrap.css\" rel=\"stylesheet\"/>";
		result += "   <style type=\"text/css\">";
		result += "      body {";
		result += "         padding-top: 60px;";
		result += "         padding-bottom: 40px;";
		result += "      }";
		result += "   </style>";
		result += "   <link href=\"../static/bootstrap/css/bootstrap-responsive.css\" rel=\"stylesheet\"/>";
		result += "   <link type=\"text/css\" rel=\"stylesheet\" href=\"../static/spider-behavior.css\"/>";
		result += "</head>";
		result += "<body>";
		result += "	 <div class=\"navbar navbar-inverse navbar-fixed-top\">";
		result += "      <div class=\"navbar-inner\">";
		result += "         <div class=\"container\">";
		result += "            <button class=\"btn btn-navbar\" data-target=\".nav-collapse\" "
				+ "                    data-toggle=\"collapse\" type=\"button\">";
		result += "		         <span class=\"icon-bar\"></span>";
		result += "		         <span class=\"icon-bar\"></span>";
		result += "		         <span class=\"icon-bar\"></span>";
		result += "		      </button>";
		result += "		      <img class=\"brand\" src=\"../static/spiderwords_small.jpg\" "
				+ "                 alt=\"spider words\" width=\"32\" height=\"19\"/>";
		result += "		      <div class=\"nav-collapse collapse\">";
		result += "		         <ul class=\"nav\">";
		result += "                  <li>";
		result += "                     <a href=\"../index.html\">Home</a>";
		result += "                  </li>";
		result += "                  <li>";
		result += "                     <a href=\"../pages/project.html\">About</a>";
		result += "                  </li>";
		result += "                  <li>";
		result += "                     <a href=\"../pages/taxonomy.html\">Taxonomy Status</a>";
		result += "                  </li>";
		result += "		            <li>";
		result += "                     <a href=\"../pages/curation.html\">Curation</a>";
		result += "                  </li>";
		result += "               </ul>";
		result += "            </div>";
		result += "		   </div>";
		result += "		</div>";
		result += "   </div>";
		result += "   <div class=\"container\">";
		if (pubdata.containsKey("title")){
			result += String.format("      <h3>%s</h3>",pubdata.get("title"));
		}
		else {
			result += String.format("      <h3>%s</h3>",pubdata.get("localIdentifier"));			
		}
		result += "   </div>";
		result += "   <div class=\"row\">";
		result += "      <div class=\"span11\">";
		result += "         <div id=\"results\">";
		result += generateResults(pubdata);
		result += "		    </div>";
		result += "      </div>";
		result += "   </div>";
		result += "   <hr/>";
		result += "   <footer>";
		String current = DateFormat.getDateInstance().format(new Date());
		String timeStr = String.format("      <p>Last update %s</p>",current);
		result += timeStr;
		result += "   </footer>";
		result += addScript("http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js");
		result += addScript("../static/bootstrap/js/bootstrap.min.js");
		result += addScript("../static/env.js");
		result += "</body>";
		result += "</html>";
		return result;
	}




	private String addScript(String script_url){
		String result = "   <script type=\"text/javascript\" ";
		result += String.format("           src=\"%s\">", script_url);
		result += "   </script>";
		return result;
	}

	
	private String generateResults(PublicationSummary pubdata){
		String result = "";
		result += "            <ul>";
		if (pubdata.containsKey("title")){
			result += "               <li><strong>Title: </strong>" + pubdata.get("title") + "</li>";
		}
		if (pubdata.containsKey("publicationYear")){
			result += "               <li><strong>Publication year: </strong>" + pubdata.get("publicationYear") + "</li>";
		}
		if (pubdata.containsKey("authorList")){
			result += "               <li><strong>Authors: </strong>" + pubdata.get("authorList") + "</li>";
		}
		if (pubdata.containsKey("publicationType")){
			result += "               <li><strong>Publication type: </strong>" + pubdata.get("publicationType") + "</li>";
		}
		if (pubdata.containsKey("sourcePublication")){
			result += "               <li><strong>Source Title: </strong>" + pubdata.get("sourcePublication") + "</li>";
		}
		if (pubdata.containsKey("volumne")){
			result += "               <li><strong>Volume: </strong>" + pubdata.get("volume") + "</li>";
		}
		if (pubdata.containsKey("issue")){
			result += "               <li><strong>Issue: </strong>" + pubdata.get("issue") + "</li>";
		}
		if (pubdata.containsKey("pageRange")){
			result += "               <li><strong>Pages: </strong>" + pubdata.get("pageRange") + "</li>";
		}
		result += "            </ul>";
		return result;
	}
	
	//cleanup methods

	private void cleanupResources(Repository repo, RepositoryConnection con){
		try{
			if (con != null){
				con.close();
			}
			if (repo != null){
				repo.shutDown();
			}
		}
		catch (NullPointerException e){
			System.out.println("Error trying to close null repository");
			e.printStackTrace();
		}
		catch (RepositoryException e){
			System.out.println("Error while trying to close repository");
			e.printStackTrace();
		}
	}


}


final class PublicationSummary extends HashMap<String,String>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}