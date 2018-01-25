'use strict';

const requireAuthentication = function(database, token, next, callback) {
    if (!database) {
        throw new Error('Database is missing.');
    }
{
        // call database

        database.getUserToken(token, (err, mappings) => {
            if (err) {
                return callback(null, false, null);
            } else {
              if (mappings != null) {
                return callback(null, true, next);
              } else {
                return callback(null, false, null);
              }
            }
        })

    };
};

module.exports = requireAuthentication;
