'use strict';

const getOutfit = function(database, choise) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        //console.log(req.params.latitude);

        // call database

        database.getOutfit(choise,req.params.art, req.params, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
              var i=0;
              var clothingLinks = {};
              for (var category_element in mappings.layers) {
                for (var outfit_element in mappings[mappings.layers[category_element]]) {
                  var clothing = { href: "/clothing/" + mappings[mappings.layers[category_element]][outfit_element], layer: mappings.layers[category_element]};
                  clothingLinks["clothing"+i] = clothing;
                  i++;
                }
              }
              mappings["_embedded"] = { clothingLinks};
              return res.status(201).send(mappings);
            }
        })
    };
};

module.exports = getOutfit;
