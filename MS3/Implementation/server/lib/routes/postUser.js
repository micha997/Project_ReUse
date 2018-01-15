'use strict';

const postUser = function(database) {

    return function(req, res) {
        // call database
        database.postUser(req.body.uId, err => {
            if (err) {
                console.log("Failed to add prefer!");
                return res.status(500).send("prefer could not be added to the database!");
            } else {
              console.log("Successfully added prefer!");
              return res.sendStatus(201);
            }
        });

    };
};

module.exports = postUser;
