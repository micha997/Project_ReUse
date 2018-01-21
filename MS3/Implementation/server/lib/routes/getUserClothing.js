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
            } else {

              var profile = { href: "/user/" + req.params.uId};
              mappings[0] = { _links : profile };
              return res.send(mappings);
            }
        })

    };
};

module.exports = getUserClothing;
