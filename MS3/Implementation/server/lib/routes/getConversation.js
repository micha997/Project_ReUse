'use strict';

const getConversation = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {
    // call database
    database.getConversation(req.params.uId,req.params.ouId,(err, mappings) => {
      res.setHeader("Content-Type", "application/json");
      if (err) {
        return res.status(500).end();
      }
      console.log(mappings);
      res.status(201).send(mappings);
    })
  };
};

module.exports = getConversation;
