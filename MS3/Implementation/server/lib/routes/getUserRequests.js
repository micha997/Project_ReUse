'use strict';

const getUserRequests = function(database, firebase) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        // console.log(req.params.token);
        // firebase.auth().verifyIdToken("")
        // .then(function(decodedToken) {
        //   var uid = decodedToken.uid;
        //   console.log(decodedToken.uid);
        //   // ...
        // }).catch(function(error) {
        //   console.log(error);
        // });
        // call database
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
