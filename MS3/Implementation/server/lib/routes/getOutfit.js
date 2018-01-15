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
              return res.status(201).send(mappings);
            }
        })
    };
};

module.exports = getOutfit;
