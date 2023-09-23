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
