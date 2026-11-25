const uricolumn = function (resultObj, column) {
    "use strict";
    const result_set = resultObj.results
    for (let i = 0; i < result_set.bindings.length; i += 1) {
        const binding = result_set.bindings[i];
        if (binding[column].type !== 'uri'){
            return false;
        }
    }
    return true;
};
