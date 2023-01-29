"use strict";
function uricolumn(resultobj, column) {
    for (let i = 0; i < resultobj.results.bindings.length; i += 1) {
        let binding = resultobj.results.bindings[i];
        let celltype = binding[column].type;
        if (celltype !== 'uri') {
            return false;
        }
    }
    return true;
}

function resulttable(resultobj) {
    if (resultobj === 'no results') {
        return "<p>No results</p>";
    }
    if (resultobj.msg){
        return "<p>" + resultobj.msg +"</p>";
    }
    if (resultobj.error){
        return "<p style='color:red'>" + resultobj.error +"</p>";
    }
    const start_row = " <tr>\n";
    if (resultobj.results.bindings && resultobj.results.bindings.length > 0) {
        var nextfieldname, nextfielddatatype;
        let result = "<table class='table'>\n";
        result = result + start_row;
        for (let i = 0; i < resultobj.head.vars.length; i += 1) {
            const fieldtype = resultobj.head.vars[i];
            if (!uricolumn(resultobj, fieldtype)) {
                result = result + "  <th>" + fieldtype + "</th>\n";
            }
        }
        result = result + "  </tr>\n";
        for (let j = 0; j < resultobj.results.bindings.length; j += 1) {
            let binding = resultobj.results.bindings[j];
            result = result + start_row;
            const head = resultobj.head;
            const vars = head.vars;
            for (let i = 0; i < vars.length; i += 1) {
                const fieldname = vars[i];
                let fielddatatype = binding[fieldname].type;
                if (i < vars.length - 1) {
                    nextfielddatatype = binding[vars[i + 1]].type;
                } else {
                    nextfielddatatype = '';
                }
                if (nextfielddatatype === 'uri') {
                    nextfieldname = vars[i + 1];
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
}

