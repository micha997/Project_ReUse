'use strict';

const putClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.putClothing(req.params.cId, req.body, err => {
            if (err) {
                console.log("Failed to edit clothing!");
                return res.status(500).send("clothing could not be edited!");
            }
            console.log("Successfully edited clothing!");
        });
        res.sendStatus(201);
    };
};

module.exports = putClothing;
