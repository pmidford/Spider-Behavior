var publicationtable = function (resultobj) {
    "use strict";
    if (resultobj && resultobj !== 'no results') {
        var result, i, binding;
        var fieldtype = resultobj.head.vars[0]
        result = "<table border='1'>\n";
        result = result + "  <tr>\n";
        result = result + "  <th>" + fieldtype + "</th>\n";
        result = result + "  </tr>\n";
        for (i = 0; i < resultobj.results.bindings.length; i += 1) {
            binding = resultobj.results.bindings[i];
            result = result + "  <tr>\n";
            result = result + "    <td>" + binding[fieldtype].value + "</td>\n";
            result = result + "  </tr>\n";
        }
        result = result + "</table>";
        return result;
    }
    return "<p>No results</p>";
};
