'use strict';

const getUserOutfitClothing = function(database, choise) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        // call database

        database.getUserOutfitClothing(req.params.uId, req.params.oId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            }
            res.status(201).send(mappings);
        })
    };
};

module.exports = getUserOutfitClothing;