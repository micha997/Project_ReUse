'use strict';

const postUserToken = function(database) {

    return function(req, res) {
        // call database
        database.postUserToken(req.params.id, req.params.token, err => {
            if (err) {
                console.log("Failed to add clothing!");
                return res.status(500).send("Token could not be added to the database!");
            }
            console.log("Successfully added Token!");
        });
        res.sendStatus(201);
    };
};

module.exports = postUserToken;
