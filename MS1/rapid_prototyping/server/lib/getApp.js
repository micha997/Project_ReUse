'use strict';

const express = require('express');
const bodyParser = require('body-parser')
const getIndex = require('./routes/getIndex');
const postKlamotten = require('./routes/postKlamotten');
const getAll = require('./routes/getAll');
const getClothingLocation = require('./routes/getClothingLocation');

const getApp = function(database) {
  // check Database
  if (!database) {
    throw new Error('Database is missing!');
  }
  // define express Webframework
  const app=express();

  //include Body Parser for JSON req
  app.use(bodyParser.json());

  // define routes
  //app.get('/all', getAll(database));
  app.get('/all/:latitude/:longitude/:vicinity', getClothingLocation(database));
  app.get('/all', getAll(database));
  app.post('/klamotten', postKlamotten(database));
  app.get('/', getIndex());

  return app;
};

module.exports = getApp;
