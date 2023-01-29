const publicationtable = function (resultObj) {
    "use strict";
    if (resultObj && resultObj !== 'no results') {
        const fieldType = resultObj.head.vars[0]
        let result = "<table class='table'>\n";
        result = result + "  <tr>\n";
        result = result + "  <th>" + fieldType + "</th>\n";
        result = result + "  </tr>\n";
        for (let i = 0; i < resultObj.results.bindings.length; i += 1) {
            let binding = resultObj.results.bindings[i];
            result = result + "  <tr>\n";
            result = result + "    <td>" + binding[fieldType].value + "</td>\n";
            result = result + "  </tr>\n";
        }
        result = result + "</table>";
        return result;
    }
    return "<p>No results</p>";
};
