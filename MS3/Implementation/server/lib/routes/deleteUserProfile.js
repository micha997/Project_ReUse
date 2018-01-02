'use strict';

const deleteUserProfile = function(database) {

    return function(req, res) {
        console.log("hi");
        // call database
        database.deleteUserProfile(req.params.uId, err => {
            if (err) {
                console.log("Failed to delete user profile!");
                return res.status(500).send("user profile could not be deleted from database!");
            }
            console.log("Successfully deleted user profile!");
        });
        res.sendStatus(201);
    };
};

module.exports = deleteUserProfile;
