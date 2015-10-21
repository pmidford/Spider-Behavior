package org.arachb.api.provider;

import java.text.DateFormat;
import java.util.Date;

public abstract class AbstractPage implements ArachbPage {

	final static String JQUERYSTRING = "http://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js";
	final static String BOOTSTRAPSTRING = "../static/bootstrap/js/bootstrap.min.js";
	final static String ENVSTRING = "../static/env.js";
	
	MetaDataSummary metadata;
	
	protected AbstractPage(MetaDataSummary mds){
		metadata = mds;
	}
	
	String generateHeader(String content){
		String result = "";
		result += "   <meta charset=\"utf-8\"/>";
		result += "   <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">";
		result += "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>";
		if (metadata.contains("title")){
			result += String.format("   <title>%s</title>",metadata.get("title"));
		}
		else {
			result += String.format("   <title>%s</title>",metadata.get("localIdentifier"));			
		}
		result += "   <meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\" />";
		String generatedtemplate = "   <meta name=\"description\" content=\"generated %s page\"/>";
		result += String.format(generatedtemplate, content);
		result += "   <meta name=\"author\" content=\"Peter E. Midford\"/>";
		result += "   <link href=\"../static/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\"/>";
		result += "   <link type=\"text/css\" rel=\"stylesheet\" href=\"../static/spider-behavior.css\"/>";
		return result;
	}
	
	String addNavBar(){
		String result = "";
		result += "<nav class=\"navbar navbar-inverse navbar-fixed-top\">";
		result += "   <div class=\"container-fluid\">";
		result += "      <div class=\"navbar-header\">";
		result += "         <button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#projectpage-navbar-collapse\" aria-expanded=\"false\">";
		result += "            <span class=\"sr-only\">Toggle navigation</span>";
		result += "            <span class=\"icon-bar\"></span>";
		result += "            <span class=\"icon-bar\"></span>";
		result += "            <span class=\"icon-bar\"></span>";
		result += "        </button>";
		result += "        <a class=\"navbar-brand\" href=\"http://arachb.org\">";
		result += "          <img class=\"navbar-image\" src=\"../static/spiderwords_small.jpg\"  alt=\"spider words\"/>";
		result += "        </a>";
		result += "      </div>";
		result += "      <ul class=\"nav navbar-nav navbar-right\">";        
		result += "      </ul>";
		result += "      <div class= \"collapse navbar-collapse\" id=\"projectpage-navbar-collapse\">";
		result += "      <ul class=\"nav navbar-nav\">";        
		result += "        <li>";
		result += "           <a href=\"../index.html\">Home</a>";
		result += "        </li>";
		result += "        <li>";
		result += "           <a href=\"../pages/project.html\">About</a>";
		result += "        </li>";
		result += "        <li>";
		result += "           <a href=\"../pages/taxonomy.html\">Taxonomy Status</a>";
		result += "        </li>";
		result += "        <li>";
		result += "          <a href=\"../pages/curation.html\">Curation</a>";
		result += "        </li>";
		result += "      </ul>";
		result += "    </div>";
		result += "  </div>";
		result += "</nav>";
		return result;
	}
	
	
	String addScript(String script_url){
		String result = "   <script type=\"text/javascript\" ";
		result += String.format("           src=\"%s\">", script_url);
		result += "   </script>";
		return result;
	}

	String addFooter(){
		String current = DateFormat.getDateInstance().format(new Date());
		String result = "   <footer>";
		result += String.format("      <p>Last update %s</p>",current);;
		result += "   </footer>";
		return result;
	}

	
	String addStartScript(String target){
		String result = "   <script>";
		result +=  "      $(document).ready(function(){";
		result += "      $.ajax({";
		result += "        url: myhost + \"taxon\" + $(location).attr('search'),";
		result += "        dataType: \"text\"";
		result += "      }";
		String donefunc = String.format("      ).done(function(data, testStatus, jqXHR){$(\"%s\").html(resulttable(eval( '(' + data + ')')))})",target);
		result += donefunc;
		result += "      .fail(function(jqXHR, textStatus, errorThrown) { alert(\"error: \" + textStatus); });";
		result += "     });";
		result += "   </script>";
		return result;
	}
	
	//Probably not really this simple
	@Override
	public String generatejson(){
		return metadata.raw();
	}

}