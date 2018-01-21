'use strict';

const getUserProfile = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        // call database
        database.getUserProfile(req.params.id, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
              var requests = { href: "/user/" + req.params.id + "/requests"};
              var clothing = {href: "/user/" + req.params.id + "/clothing" };
              mappings["_links"] =  { requests, clothing} ;
              return res.send(mappings);
            }
        })

    };
};

module.exports = getUserProfile;
