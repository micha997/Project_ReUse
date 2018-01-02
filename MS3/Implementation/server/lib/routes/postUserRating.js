'use strict';

const postUserRating = function(database) {

    return function(req, res) {
        // call database
        database.postUserRating(req.params.uId, req.body, err => {
            if (err) {
                console.log("Failed to add rating!");
                return res.status(500).send("Rating could not be added to the database!");
            }
            console.log("Successfully added rating!");
        });
        res.sendStatus(201);
    };
};

module.exports = postUserRating;
