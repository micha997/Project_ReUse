'use strict';

const getUserRequests = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        // call database
        database.getUserRequests(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            }
            res.send(mappings);
        })

    };
};

module.exports = getUserRequests;