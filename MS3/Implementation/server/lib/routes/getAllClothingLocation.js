'use strict';

const getAllClothingLocation = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        //console.log(req.params.latitude);
        const latitude = req.params.latitude;
        const longitude = req.params.longitude;
        const vicinity = req.params.vicinity;
        // call database
        database.getAllClothingLocation(latitude, longitude, vicinity, (err, mappings) => {
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

module.exports = getAllClothingLocation;
