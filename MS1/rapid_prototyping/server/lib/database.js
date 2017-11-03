'use strict';

const mongo = require('mongodb');
const calcDistance = require('./calcDistance')
const MongoClient = mongo.MongoClient;

const database = {
  initialize(connectionString, callback) {
    MongoClient.connect(connectionString, { autoReconnect: true}, (err, database) => {
      // check for missing arguments
      if (err) {
        return callback(err);
      }
      if (!connectionString) {
        throw new Error ('connectionString is missing.');
      }
      if (!callback) {
        throw new Error ('Callback is missing.');
      }
      // define collection name
      const mappings=database.collection('mappings');
      this.mappings = mappings;
      callback(null);
    });
  },
  // send all DB-Values
  getAll(callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.find({}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getClothingLocation(latitude, longitude, vicinity, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.find({}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      var mappings_json = JSON.parse(JSON.stringify(mappings));

      for(var i = 0; i < mappings_json.length; i++) {
          var distance = calcDistance(mappings_json[i].latitude, mappings_json[i].longitude, latitude,longitude);
          if (distance >= vicinity) {
              var removed = mappings_json.splice(i, 1);
          }

      }
      callback(null, mappings_json);
    })
  },
  addClothing(clothing, callback) {
    if (!clothing) {
      throw new Error ('Clothing is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    clothing = JSON.parse(clothing);

    const mapping = {
      name: clothing["name"],
      longitude: clothing["longitude"],
      latitude: clothing["latitude"],
      groesse: clothing["groesse"],
      city: clothing["city"]
    };
    //write mapping to Database
    this.mappings.insertOne(mapping, err => {
      if (err) {
        return callback(err);
      }
      callback(null);
    });
  }

};



module.exports = database;
