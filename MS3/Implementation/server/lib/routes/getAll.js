'use strict';

const getAll = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {
    // call database
    database.getAll((err, mappings) => {
      console.log("hi");
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      res.status(201).send(mappings);
    })
  };
};

module.exports = getAll;
