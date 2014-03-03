var uricolumn = function (resultobj, column) {
    "use strict";
    var i, binding, celltype;
    for (i = 0; i < resultobj.results.bindings.length; i += 1) {
        binding = resultobj.results.bindings[i];
        celltype = binding[column].type;
        if (celltype !== 'uri') {
            return false;
        }
    }
    return true;
};

var resulttable = function (resultobj) {
    "use strict";
    if (resultobj === 'no results') {
        return "<p>No results</p>";
    }
    if (resultobj.results.bindings && resultobj.results.bindings.length > 0) {
        var result, i, j, binding, fieldtype, fieldname, nextfieldname, fielddatatype, nextfielddatatype;
        result = "<table class='table'>\n";
        result = result + "  <tr>\n";
        for (i = 0; i < resultobj.head.vars.length; i += 1) {
            fieldtype = resultobj.head.vars[i];
            if (!uricolumn(resultobj, fieldtype)) {
                result = result + "  <th>" + fieldtype + "</th>\n";
            }
        }
        result = result + "  </tr>\n";
        for (j = 0; j < resultobj.results.bindings.length; j += 1) {
            binding = resultobj.results.bindings[j];
            result = result + "  <tr>\n";
            for (i = 0; i < resultobj.head.vars.length; i += 1) {
                fieldname = resultobj.head.vars[i];
                fielddatatype = binding[fieldname].type;
                if (i < resultobj.head.vars.length - 1) {
                    nextfielddatatype = binding[resultobj.head.vars[i + 1]].type;
                } else {
                    nextfielddatatype = '';
                }
                if (nextfielddatatype === 'uri') {
                    nextfieldname = resultobj.head.vars[i + 1];
                    result = result + "    <td> <a href= '" + binding[nextfieldname].value + "'>" + binding[fieldname].value + "</a></td>\n";
                } else {
                    if (fielddatatype !== 'uri') {
                        result = result + "    <td>" + binding[fieldname].value + "</td>\n";
                    }
                }
            }
            result = result + "  </tr>\n";
        }
        result = result + "</table>";
        return result;
    }
    return "<p>No results</p>";
};

