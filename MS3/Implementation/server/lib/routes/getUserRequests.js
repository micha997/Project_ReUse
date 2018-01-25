'use strict';

const getUserRequests = function(database, firebase, login) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
      console.log(login);
        database.getUserRequests(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");

            if (err) {
                return res.status(500).end();
            } else {
              return res.status(201).send(mappings);
            }


        })

    };
};

module.exports = getUserRequests;
