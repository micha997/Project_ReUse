'use strict';

const postMessage = function(database) {

    return function(req, res) {
        // call database
        database.postMessage(req.params.uId, req.body, err => {
            if (err) {
                console.log("Failed to add message!");
                return res.status(500).send("Message could not be added to the database!");
            } else {
              console.log("Successfully added message!");
              return res.sendStatus(201);
            }

        });

    };
};

module.exports = postMessage;
