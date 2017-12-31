'use strict';

const deleteConversation = function (database) {

  return function (req, res)  {
    console.log("hi");
    // call database
    database.deleteConversation(req.params.uId, req.params.ouId, err => {
      if (err) {
        console.log("Failed to delete conversation!");
        return res.status(500).send("conversation could not be deleted from database!");
      }
      console.log("Successfully deleted conversation!");
    });
    res.sendStatus(201);
  };
};

module.exports = deleteConversation;