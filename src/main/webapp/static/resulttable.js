var resulttable = function (resultobj) {
    "use strict";
    if (resultobj === 'no results') {
        return "<p>No results</p>";
    }
    if (resultobj.results.bindings && resultobj.results.bindings.length > 0) {
        var result, i, j, binding, fieldtype;
        result = "<table class='table'>\n";
        result = result + "  <tr>\n";
        for (i = 0; i < resultobj.head.vars.length; i += 1) {
            fieldtype = resultobj.head.vars[i];
            result = result + "  <th>" + fieldtype + "</th>\n";
        }
        result = result + "  </tr>\n";
        for (j = 0; j < resultobj.results.bindings.length; j += 1) {
            binding = resultobj.results.bindings[j];
            result = result + "  <tr>\n";
            for (i = 0; i < resultobj.head.vars.length; i += 1) {
                fieldtype = resultobj.head.vars[i];
                result = result + "    <td>" + binding[fieldtype].value + "</td>\n";
            }
            result = result + "  </tr>\n";
        }
        result = result + "</table>";
        return result;
    }
    return "<p>No results</p>";
};
