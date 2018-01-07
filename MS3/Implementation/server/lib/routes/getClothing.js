'use strict';

const getClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
                console.log("HI");
        // call database
        database.getClothing(req.params.cId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            }
            res.send(mappings);
        })

    };
};

module.exports = getClothing;
