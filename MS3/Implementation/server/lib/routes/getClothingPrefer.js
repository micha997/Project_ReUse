'use strict';

const searchPreferredClothing = require('../searchPreferredClothing');

const getClothingPrefer = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {

    // call database
    database.getUserPrefer(req.params.id,(err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }

      database.getAllClothingLocation(req.params.latitude, req.params.longitude, req.params.vicinity,(err, maps) => {
        res.setHeader("Content-Type", "application/json");
        if (err) {
          return res.status(500).end();
        }
        maps = searchPreferredClothing(mappings, maps);
        res.status(201).send(maps);
      })



    })
  };
};

module.exports = getClothingPrefer;
