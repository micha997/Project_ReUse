'use strict';

const postKlamotten = function(database) {

    return function(req, res) {
        // call database
        database.addClothing(JSON.stringify(req.body), err => {
            if (err) {
                console.log("Failed to add clothing!");
                return res.status(500).send("Clothing could not be added to the database!");
            } else {
              console.log("Successfully added clothing!");
              res.sendStatus(201);
            }
        });

    };
};

module.exports = postKlamotten;
