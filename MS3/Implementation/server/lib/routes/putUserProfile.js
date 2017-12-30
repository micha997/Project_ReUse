'use strict';

const putUserProfile = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {
    console.log("hi");
    database.putUserProfile(req.params.uId, req.body, err => {
      if (err) {
        console.log("Failed to edit Userprofile!");
        return res.status(500).send("prefer could not be added to the database!");
      }
      console.log("Successfully edited Userprofile!");
    });
    res.sendStatus(201);
  };
};

module.exports = putUserProfile;
