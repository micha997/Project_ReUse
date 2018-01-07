'use strict';

const getUserClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        // call database
        database.getUserClothing(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            }
            for (var single_map in mappings) {
                delete mappings[single_map].image;
            }
            res.send(mappings);
        })

    };
};

module.exports = getUserClothing;
