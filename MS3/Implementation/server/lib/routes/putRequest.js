'use strict';

const putRequest = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.putRequest(req.body, req.params.uId, req.params.id, err => {
            if (err) {
                console.log("Failed to edit clothing!");
                return res.status(500).send("clothing could not be edited!");
            }
            console.log("Successfully edited clothing!");
        });
        res.sendStatus(201);
    };
};

module.exports = putRequest;