'use strict';

const mongo = require('mongodb');

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
      position: clothing["position"]
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
