var publicationtable = function (resultobj){
    if (resultobj.results) {
        var result = "";
        result = result + "<table border='1'>\n";
        result = result + "  <tr>\n";
        result = result + "  <th>" + resultobj.head.vars[0] + "</th>\n";
        result = result + "  </tr>\n";
        for(i=0;i < resultobj.results.bindings.length; i += 1){
	    result = result + "  <tr>\n";
	    result = result + "    <td>" + resultobj.results.bindings[i].publication.value + "</td>\n";
           result = result + "  </tr>\n"
	       }
        result = result + "</table>";
        return result;
    }
    else {
        return "<p>No results</p>";
    }
}
