'use strict';

const deleteUserToken = function (database) {

  return function (req, res)  {
    console.log("hi");
    // call database
    database.deleteUserToken(req.params.id, err => {
      if (err) {
        console.log("Failed to add clothing!");
        return res.status(500).send("Token could not be added to the database!");
      }
      console.log("Successfully added Token!");
    });
    res.sendStatus(201);
  };
};

module.exports = deleteUserToken;
