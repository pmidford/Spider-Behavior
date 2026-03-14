package org.arachb.api.provider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.text.DateFormat;
import java.util.Date;

public abstract class AbstractPage implements ArachbPage {

	final static String BOOTSTRAPSTRING = "../static/bootstrap/js/bootstrap.bundle.min.js";
	final static String ENVSTRING = "../static/env.js";

	final MetaDataSummary  metadata;

	protected AbstractPage(MetaDataSummary mds){
		metadata = mds;
	}

	String generateHeader(String content){
		final String ls = System.lineSeparator();
		String result = "";
		result += "   <meta charset=\"utf-8\"/>" + ls;
		result += "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>" +ls;
		String titleTemplate = "   <title>%s</title>\n";
		if (metadata.contains("title")){
			result += String.format(titleTemplate,metadata.get("title"));
		}
		else {
			result += String.format(titleTemplate,metadata.get("localIdentifier"));
		}
		result += "   <meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\" />";
		String generatedtemplate = "   <meta name=\"description\" content=\"generated %s page\"/>";
		result += String.format(generatedtemplate, content) + ls;
		result += "   <meta name=\"author\" content=\"Peter E. Midford\"/>";
		result += "   <link href=\"../static/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\"/>";
		result += "   <link type=\"text/css\" rel=\"stylesheet\" href=\"../static/spider-behavior.css\"/>";
		return result;
	}

		final static String navBar =
			"<div class=\"container\">" +
					"<nav class=\"navbar fixed-top navbar-expand-lg navbar-dark bg-dark\">" +
					"<a class=\"navbar-brand\" href=\"https://arachb.org\">" +
					"<img class=\"navbar-image\""+
					"src=\"static/spiderwords_small.jpg\"" +
					"alt=\"Spider standing on an Anglo-Saxon glossary\"/>" +
					"</a>" +
					"<button class=\"navbar-toggler\" type=\"button\" data-bs-toggle=\"collapse\":" +
					" data-bs-target=\"#navbarNavDropdown\" aria-controls=\"navbarNavDropdown\"" +
					" aria-expanded=\"false\" aria-label=\"Toggle navigation\">" +
					" <span class=\"navbar-toggler-icon\"></span>" +
					"</button>" +
					"<div class=\"collapse navbar-collapse\" id=\"navbarNavDropdown\">" +
					"<ul class=\"navbar-nav\">" +
					"<li class=\"nav-item\">" +
					"<a class=\"nav-link\" href=\"#\">Home</a>" +
					"</li>" +
					"<li class=\"nav-item\">" +
					"<a class=\"nav-link\" href=\"pages/faq.html\">About</a>" +
					"</li>" +
					"<li class=\"nav-item\">" +
					"<a class=\"nav-link\" href=\"pages/taxonomy.html\">Taxonomy Status</a>" +
					"</li>" +
					"<li class=\"nav-item\">" +
					"<ul>" +
					"<a class=\"nav-link\" href=\"pages/ontology.html\">Ontology Resources</a>" +
					"<li><a class=\"dropdown-item\" href=\"vocabulary.html\">Vocabulary for Spider (eventually Arachnid) Behavior</a></li>" +
					"<li><a class=\"dropdown-item\" href=\"ontologies.html\">Ontologies</a></li>" +
					"</ul>" +
					"</li>" +
					"<li class=\"nav-item\">" +
					"<a class=\"nav-link\" href=\"pages/curation.html\">Curation</a>" +
					"</li>" +
					"</ul>" +
					"</div>" +
					"</nav>" +
					"</div>";



	String addScript(String script_url){
		String result = "   <script type=\"text/javascript\" ";
		result += String.format("           src=\"%s\"> \n", script_url);
		result += "   </script>" + System.lineSeparator();
		return result;
	}

	String addFooter(){
		final String current = DateFormat.getDateInstance().format(new Date());
		final String ls = System.lineSeparator();
		String result = "   <footer>" + ls;
		result += String.format("      <p>Last update %s</p>",current) + ls;
		result += "   </footer>" + ls;
		return result;
	}


	String addStartScript(String target){
		String ls = System.lineSeparator();
		String result = "   <script>" + ls;
		result +=  "      $(document).ready(function(){" + ls;
		result += "      $.ajax({" + ls;
		result += "        url: myhost + \"taxon\" + $(location).attr('search')," + ls;
		result += "        dataType: \"text\"" + ls;
		result += "      }" + ls;
		String donefunc = String.format("      ).done(function(data, testStatus, jqXHR){$(\"%s\").html(resulttable(JSON.parse(data)))})\n",target);
		result += donefunc;
		result += "      .fail(function(jqXHR, textStatus, errorThrown) { alert(\"error: \" + textStatus); });" + ls;
		result += "     });" + ls;
		result += "   </script>" + ls;
		return result;
	}

	//Probably not really this simple
	@Override
	public String generatejson(ServletContext servletContext){
		return metadata.raw();
	}

}
