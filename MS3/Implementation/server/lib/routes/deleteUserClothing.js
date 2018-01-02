'use strict';

const deleteUserClothing = function(database) {

    return function(req, res) {
        console.log("hi");
        // call database
        database.deleteUserClothing(req.params.uId, err => {
            if (err) {
                console.log("Failed to delete clothing!");
                return res.status(500).send("clothing could not be deleted from database!");
            }
            console.log("Successfully deleted clothing!");
        });
        res.sendStatus(201);
    };
};

module.exports = deleteUserClothing;
