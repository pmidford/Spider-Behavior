const uricolumn = function (resultObj, column) {
    "use strict";
    for (let i = 0; i < resultObj.results.bindings.length; i += 1) {
        let binding = resultObj.results.bindings[i];
        if (binding[column].type !== 'uri'){
            return false;
        }
    }
    return true;
};

const taxoneventtable = function (resultObj) {
    "use strict";
    if (resultobj === 'no results') {
        return "<p>No results</p>";
    }
    if (resultObj.msg){
        return "<p>" + resultObj.msg +"</p>";
    }
    if (resultObj.error){
        return "<p style='color:red'>" + resultObj.error +"</p>";
    }
    if (resultObj.results.bindings && resultObj.results.bindings.length > 0) {
        var result, i, j, fieldname, nextfieldname, fielddatatype, nextfielddatatype;
        result = "<table class='table'>\n";
        result = result + "  <tr>\n";
        for (i = 0; i < resultObj.head.vars.length; i += 1) {
            const fieldType = resultObj.head.vars[i];
            if (!uricolumn(resultObj, fieldType)) {
                result = result + "  <th>" + fieldType + "</th>\n";
            }
        }
        result = result + "  </tr>\n";
        for (j = 0; j < resultObj.results.bindings.length; j += 1) {
            const binding = resultObj.results.bindings[j];
            result = result + "  <tr>\n";
            for (i = 0; i < resultObj.head.vars.length; i += 1) {
                fieldname = resultObj.head.vars[i];
                fielddatatype = binding[fieldname].type;
                if (i < resultObj.head.vars.length - 1) {
                    nextfielddatatype = binding[resultObj.head.vars[i + 1]].type;
                } else {
                    nextfielddatatype = '';
                }
                if (nextfielddatatype === 'uri') {
                    nextfieldname = resultObj.head.vars[i + 1];
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

