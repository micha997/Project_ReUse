'use strict';

const getOutfit = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {
    //console.log(req.params.latitude);

    // call database
    database.getOutfit(req.params.art,(err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      for (var single_map in mappings) {
          delete mappings[single_map].image;
       }
      res.status(201).send(mappings);
    })
  };
};

module.exports = getOutfit;
