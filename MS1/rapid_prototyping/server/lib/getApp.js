'use strict';

const express = require('express');
const bodyParser = require('body-parser')
const getIndex = require('./routes/getIndex');


const getApp = function(database) {
  // check Database
  if (!database) {
    throw new Error('Database is missing!');
  }
  // define express Webframework
  const app=express();

  //include Body Parser for JSON req
  app.use(bodyParser.json());

  app.get('/', getIndex());

  return app;
};

module.exports = getApp;
