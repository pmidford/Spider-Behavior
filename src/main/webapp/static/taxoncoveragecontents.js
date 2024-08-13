const sub_taxa =
    ["Arachnida",
        "Araneae",
        "Amblypygi",
        "Opiliones",
        "Palpigradi",
        "Pseudoscorpiones",
        "Ricinulei",
        "Schizomida",
        "Solifugae",
        "Uropygi",
        "Acari",
        "Acariformes",
        "Opilioacariformes",
        "Parasitiformes"
    ];

const sources = new Map();
sources.set("NCBI", "https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Tree&id=6854")
sources.set("WCA", "https://wac.nmbe.ch/")
sources.set("COL", "https://www.catalogueoflife.org/")

const long_names = new Map();
long_names.set("NCBI", "NCBI Taxonomy")
long_names.set("WCA", "World Arachnida Catalog")
long_names.set("COL", "Catalogue Of Life")


const cites = new Map();
cites.set("NCBI",
    "Conrad L Schoch, Stacy Ciufo, Mikhail Domrachev, Carol L Hotton, <br>" +
    "Sivakumar Kannan, Rogneda Khovanskaya, Detlef Leipe, Richard Mcveigh, <br>" +
    "Kathleen O’Neill, Barbara Robbertse, Shobha Sharma, Vladimir Soussov, <br>" +
    "John P Sullivan, Lu Sun, Seán Turner, Ilene Karsch-Mizrachi, <br>" +
    "NCBI Taxonomy: a comprehensive update on curation, resources and tools, <br>" +
    "Database, Volume 2020, 2020, baaa062 <br>" +
    "<a href='https://doi.org/10.1093/database/baaa062'>https://doi.org/10.1093/database/baaa062</a>. <br>" +
    "Accessed ")
cites.set("WCA",
    "World Arachnida Catalog (2024) World Arachnida Catalog. <br>" +
    "Natural History Museum Bern, online at http://wac.nmbe.ch <br>" +
    "Accessed ")
cites.set("COL",
    "Bánki, O., Roskov, Y., Döring, M., Ower, G., Hernández Robles, D. R.,<br>" +
    "Plata Corredor, C. A., Stjernegaard Jeppesen, T., Örn, A., Vandepitte, L., <br>" +
    "Hobern, D., Schalk, P., DeWalt, R. E., Ma, K., Miller, J., Orrell, T., <br" +
    "Aalbu, R., Abbott, J., Adlard, R., Aedo, C., et al. (2024). <br>" +
    "Catalogue of Life (Version current as of date below). Catalogue of Life, <br>" +
    "Amsterdam, Netherlands. https://doi.org/10.48580/dgbqz <br>" +
    "Accessed ")


const taxon_coverage_contents = function(statsObj) {
    "use strict";
    if (statsObj === 'no results') {
        return "<p>No results</p>";
    }
    if (statsObj.msg){
        return "<p>" + statsObj.msg +"</p>";
    }
    if (statsObj.error){
        return "<p style='color:red'>" + statsObj.error +"</p>";
    }
    let results = taxon_coverage_table(statsObj)

    results = results + "<br><hr>"
    results = results + "<dl>"
    for (const source of cites.keys()) {
        const timestamp = statsObj[source]["Updated"]
        const cite = cites.get(source) + timestamp
        results = results + "   <dt>" + source + "<dt>\n"
        results = results + "   <dd>" + cite + "</dd>\n"
    }
    results = results + "</dl>"

    return results

}

const taxon_coverage_table = function (statsObj) {
    let result
    result = "<table class='table'>\n";
    result = result + "  <tr>";
    result = result + "   <th>Taxon/Source</th>\n";
    for (const source of sources.keys()) {
        const link = sources.get(source)
        const long_name = long_names.get(source)
        result = result + "   <th><a href='" + link + "'>" + long_name + "</a></th>\n";
    }
    result = result + "  </tr>"
    for (const taxon of sub_taxa) {
        result = result + "   <tr>\n" + "   <td>" + taxon + "</td>";
        for (const source of sources.keys()) {
            const source_data = statsObj[source]
            const taxon_data = source_data[taxon]
            if (taxon_data === undefined){
                result = result + "   <td> </td>";
            }
            else if ("number" == typeof(taxon_data)){
                result = result + "    <td>" + taxon_data + "</td>";
            }
            else
            {
                const raw = taxon_data['raw'];
                const valid = taxon_data['valid'];
                if (raw !== valid) {
                    result = result + "    <td>" + valid + " (" + raw + ")" + "</td>";
                }
                else {
                    result = result + "    <td>" + valid + "</td>";
                }
            }
        }
        result = result + "</tr>"
    }
    result = result + "</table>\n";

    return result
}
