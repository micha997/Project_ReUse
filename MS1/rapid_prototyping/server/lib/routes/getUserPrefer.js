'use strict';
const calcPrefs=require('./calcPrefs');

const getUserPrefer = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {

    // call database
    database.getUserPrefer(req.params.id, (err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      var prefs = calcPrefs(mappings);
      res.send(prefs);
    })

  };
};

module.exports = getUserPrefer;
