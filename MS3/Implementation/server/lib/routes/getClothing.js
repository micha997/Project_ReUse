'use strict';

const getClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        // call database
        database.getClothing(req.params.cId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
              console.log(mappings);
              var self= { href: "/clothing/" + req.params.cId};
              var userprofile = { href: "/user/" + mappings["uId"]};
              mappings["_links"]= {self: self, userprofile: userprofile};
              return res.send(mappings);
            }
        })

    };
};

module.exports = getClothing;
