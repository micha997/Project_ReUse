'use strict';

const getUserRating = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {

    // call database
    database.getUserRating(req.params.uId, (err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      res.status(201).send(mappings["rating"]);
    })
  };
};

module.exports = getUserRating;
