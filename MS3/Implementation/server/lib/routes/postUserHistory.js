'use strict';

const postUserHistory = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {

    // call database
    database.getAll((err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      res.status(201).send(mappings);
    })
  };
};

module.exports = postUserHistory;
