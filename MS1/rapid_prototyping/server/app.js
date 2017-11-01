'use strict';

const https= require('https');
const fs = require('fs');
const getApp = require('./lib/getApp'),
      database = require('./lib/database');
const app = getApp(database);


// configuration for MongoDB USER: admin, PW: secret PORT: 27017
const mongoUrl = process.env.MONGO_URL || 'mongodb://admin:secret@localhost:27017/admin';

// Load HTTPS configuration
const options = {
  key: fs.readFileSync(__dirname + '/keys/key.pem', 'utf8'),
  cert: fs.readFileSync(__dirname + '/keys/server.crt', 'utf8')
};

// define server
const server = https.createServer(options, app);

// server port
const port = process.env.PORT || 3000;

// initialize MongoDB
database.initialize(mongoUrl, err => {
    if (err) {
      console.log('Failed to connect to database.', {err});
      process.exit(1);
    }
});

const requestHandler = (request, response) => {
    console.log(request.url)
    response.end('Hello Node.js Server!')
}

// start server
server.listen(port,() => {
    console.log('Server is started.', {port});
});
