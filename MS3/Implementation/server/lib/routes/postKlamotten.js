'use strict';

const requireAuthentication = require("../requireAuthentication");

const postKlamotten = function(database) {

    return function(req, res) {
        // call database

        var logedIn = requireAuthentication(database, req.body.token);
        console.log(logedIn);
        database.addClothing(JSON.stringify(req.body), err => {
            if (err) {
                console.log("Failed to add clothing!");
                return res.status(500).send("Clothing could not be added to the database!");
            } else {
              console.log("Successfully added clothing!");
              return res.sendStatus(201);
            }
        });

    };
};

module.exports = postKlamotten;
