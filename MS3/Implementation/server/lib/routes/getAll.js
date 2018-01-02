'use strict';

const getAll = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        // call database
        database.getAll((err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            }
            console.log(mappings);
            res.status(201).send(mappings);
        })
        /*for (var single_map in mappings) {
            delete mappings[single_map].image;
         }*/
    };
};

module.exports = getAll;
