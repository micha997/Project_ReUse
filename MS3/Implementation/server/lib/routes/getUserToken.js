'use strict';

const getUserToken = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        // call database
        database.getUserToken(req.params.id, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
              return res.status(201).send(mappings);
            }
        })
    };
};

module.exports = getUserToken;
