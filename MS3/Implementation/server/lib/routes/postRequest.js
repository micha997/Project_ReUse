'use strict';

const postRequest = function(database) {

    return function(req, res) {
        // call database
        database.postRequest(req.params.cId, req.body, err => {
            if (err) {
                console.log("Failed to add request!");
                return res.status(500).send("request could not be added to the database!");
            } else {
              console.log("Successfully added request!");
              return res.sendStatus(201);
            }
        });

    };
};

module.exports = postRequest;
