'use strict';

const express = require('express');
const bodyParser = require('body-parser')

var routes = require("./routes");
const getApp = function(database) {
  // check Database
  if (!database) {
    throw new Error('Database is missing!');
  }
  // define express Webframework
  const app=express();

  //include Body Parser for JSON req
  app.use(bodyParser.json({ extended: true, limit: '5mb' }));
  app.use(bodyParser.urlencoded({ extended: true, limit: '5mb' }));


  // define routes
  app.get('/users/:id/prefer', routes.getUserPrefer(database));
  app.post('/users/:id/prefer', routes.postUserPrefer(database));

  app.get('/users/:id/prefer/klamotten/:latitude/:longitude/:vicinity', routes.getClothingPrefer(database));

  app.get('/users/:id/token', routes.getUserToken(database));
  app.post('/users/:id/:token', routes.postUserToken(database));
  app.delete('/users/:id/token', routes.deleteUserToken(database));

  app.get('/user/:uId/clothing/', routes.getUserClothing(database));
  app.get('/user/:id', routes.getUserProfile(database));
  app.post('/users', routes.postUser(database));

  app.get('/clothing/:uId', routes.getUserClothing(database));
  app.post('/klamotten', routes.postKlamotten(database));
  app.get('/klamotten/:latitude/:longitude/:vicinity/:uId', routes.getAllClothingLocation(database));

  app.get('/outfit/:art', routes.getOutfit(database));

  app.get('/all', routes.getAll(database));

  app.get('/', routes.getIndex());

  return app;
};

module.exports = getApp;
