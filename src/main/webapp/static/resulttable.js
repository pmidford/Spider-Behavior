"use strict";

function resulttable(resultobj) {
    if (resultobj === "no results") {
        return "<p>No results</p>";
    }
    if (resultobj.msg){
        return "<p>" + resultobj.msg +"</p>";
    }
    if (resultobj.error){
        return "<p style='color:red'>" + resultobj.error +"</p>";
    }
    const start_row = " <tr>\n";
    const head = resultobj.head;
    const head_vars = head.vars;
    if (resultobj.results.bindings && resultobj.results.bindings.length > 0) {
        let result = "<table class='table'>\n";
        result = result + start_row;
        for (let i = 0; i < head_vars.length; i += 1) {
            const field_name = head_vars[i];
            const pretty_name = field_name.replaceAll("_", " ");
            if (!uricolumn(resultobj, field_name)) {
                result = result + "  <th>" + pretty_name + "</th>\n";
            }
        }
        result = result + "  </tr>\n";
        let next_field_name;
        let next_field_data_type;
        for (let j = 0; j < resultobj.results.bindings.length; j += 1) {
            const binding = resultobj.results.bindings[j];
            result = result + start_row;
            for (let i = 0; i < head_vars.length; i += 1) {
                const field_name = head_vars[i];
                let field_data_type = binding[field_name].type;
                if (i < head_vars.length - 1) {
                    next_field_data_type = binding[head_vars[i + 1]].type;
                } else {
                    next_field_data_type = "";
                }
                if (next_field_data_type === "uri") {
                    next_field_name = head_vars[i + 1];
                    next_field_value = binding[next_field_name].value;
                    field_value = binding[field_name].value;
                    result = result + "    <td> <a href= '" + next_field_value + "'>" + field_value + "</a></td>\n";
                } else {
                    if (field_data_type !== "uri") {
                        result = result + "    <td>" + field_value + "</td>\n";
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

function publication_table(resultobj) {
    if (resultobj === "no results") {
        return "<p>No Publications to Display</p>";
    }
    if (resultobj.msg){
        return "<p>" + resultobj.msg +"</p>";
    }
    if (resultobj.error){
        return "<p style='color:red'>" + resultobj.error +"</p>";
    }
    const start_row = " <tr>\n";
    const head = resultobj.head;
    const head_vars = head.vars;
    if (resultobj.results.bindings && resultobj.results.bindings.length > 0) {
        let update_field = head_vars[head_vars.length-1];
        let result = "<table class='table'>\n";
        result = result + start_row;
        for (let i = 0; i < head_vars.length; i += 1) {
            const field_name = head_vars[i];
            const pretty_name = field_name.replaceAll("_", " ");
            if (field_name === update_field ||
                !uricolumn(resultobj, field_name)) {
                result = result + "  <th>" + pretty_name + "</th>\n";
            }
        }
        let first_field = head_vars[0];
        let nextfieldname;
        let nextfielddatatype;
        let bindings = reduce_table(resultobj.results.bindings, first_field, update_field);
        result = result + "  </tr>\n";
        for (let j = 0; j < bindings.length; j += 1) {
            let binding = bindings[j];
            result = result + start_row;
            for (let i = 0; i < head_vars.length; i += 1) {
                const field_name = head_vars[i];
                let fielddatatype = binding[field_name].type;
                if (i < head_vars.length - 1) {
                    nextfielddatatype = binding[head_vars[i + 1]].type;
                } else {
                    nextfielddatatype = "";
                }
                if (nextfielddatatype === "uri") {
                    nextfieldname = head_vars[i + 1];
                    result = result + "    <td> <a href= '" + binding[nextfieldname].value + "'>" + binding[field_name].value + "</a></td>\n";
                } else {
                    if (fielddatatype !== "uri") {
                        result = result + "    <td>" + binding[field_name].value + "</td>\n";
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

function reduce_table(bindings, head_var, update_var){
    const new_bindings = [];
    const counts = new Map();
    const firsts = new Map();
    for (let j = 0; j < bindings.length; j += 1) {
        const binding = bindings[j];
        const head_value = binding[head_var];
        const head = head_value.value;
        if (counts.get(head)) {
            counts.set(head,counts.get(head)+1);
        }
        else {
            counts.set(head, 1);
            firsts.set(head, j);
        }
    }
    let new_index = 0;
    for (const [k, index] of firsts) {
        const old_value = bindings[index];
        const new_count = counts.get(k);
        old_value[update_var] =  {"type": "literal", "value": new_count};
        new_bindings[new_index] = old_value;
        new_index += 1;
    }

    return new_bindings;
}
