'use strict';

const getConversation = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        // call database
        database.getConversation(req.params.uId, req.params.ouId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
              mappings.sort(function(a, b) {
                return parseFloat(a.time) - parseFloat(b.time);
              })
            return res.status(200).send(mappings);
            }
          })

        }
    };


module.exports = getConversation;
