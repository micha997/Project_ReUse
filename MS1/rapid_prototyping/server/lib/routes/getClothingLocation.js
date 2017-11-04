'use strict';

const getClothingLocation = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {
    //console.log(req.params.latitude);
    const latitude =req.params.latitude;
    const longitude = req.params.longitude;
    const vicinity = req.params.vicinity;
    // call database
    database.getClothingLocation(latitude, longitude, vicinity,(err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      res.status(201).send(mappings);
    })
  };
};

module.exports = getClothingLocation;
