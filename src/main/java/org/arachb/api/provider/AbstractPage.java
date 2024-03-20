package org.arachb.api.provider;

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
	
	String addNavBar(){
		String result = "";
		result += "<div class=\"container\">";
		result += "<nav class=\"navbar fixed-top navbar-expand-lg navbar-dark bg-dark\">";
		result += "<a class=\"navbar-brand\" href=\"https://arachb.org\">";
		result += "<img class=\"navbar-image\"";
		result += "src=\"static/spiderwords_small.jpg\"";
		result += "alt=\"Spider standing on an Anglo-Saxon glossary\"/>";
		result += "</a>";
		result += "<button class=\"navbar-toggler\" type=\"button\" data-toggle=\"collapse\":";
		result += " data-target=\"#navbarNavDropdown\" aria-controls=\"navbarNavDropdown\"";
		result += " aria-expanded=\"false\" aria-label=\"Toggle navigation\">";
		result += " <span class=\"navbar-toggler-icon\"></span>";
		result += "</button>";
		result += "<div class=\"collapse navbar-collapse\" id=\"navbarNavDropdown\">";
		result += "<ul class=\"navbar-nav\">";
		result += "<li class=\"nav-item\">";
		result += "<a class=\"nav-link\" href=\"#\">Home</a>";
		result += "</li>";
		result += "<li class=\"nav-item\">";
		result += "<a class=\"nav-link\" href=\"pages/about.html\">About</a>";
		result += "</li>";
		result += "<li class=\"nav-item\">";
		result += "<a class=\"nav-link\" href=\"pages/taxonomy.html\">Taxonomy Status</a>";
		result += "</li>";
		result += "<li class=\"nav-item\">";
		result += "<a class=\"nav-link\" href=\"pages/ontology.html\">Ontology Resources</a>";
		result += "</li>";
		result += "<li class=\"nav-item\">";
		result += "<a class=\"nav-link\" href=\"pages/curation.html\">Curation</a>";
		result += "</li>";
		result += "</ul>";
		result += "</div>";
		result += "</nav>";
		result += "</div>";
		return result;
	}
	
	
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
	public String generatejson(){
		return metadata.raw();
	}

}
