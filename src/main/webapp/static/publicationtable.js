const publicationtable = function (resultobj) {
    "use strict";
    if (resultobj && resultobj !== 'no results') {
        const fieldtype = resultobj.head.vars[0]
        let result = "<table style=\"border-width: 1px;\">\n";
        result = result + "  <tr>\n";
        result = result + "  <th>" + fieldtype + "</th>\n";
        result = result + "  </tr>\n";
        for (let i = 0; i < resultobj.results.bindings.length; i += 1) {
            let binding = resultobj.results.bindings[i];
            result = result + "  <tr>\n";
            result = result + "    <td>" + binding[fieldtype].value + "</td>\n";
            result = result + "  </tr>\n";
        }
        result = result + "</table>";
        return result;
    }
    return "<p>No results</p>";
};
