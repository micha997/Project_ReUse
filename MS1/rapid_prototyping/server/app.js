'use strict';

const http= require('http');
const getApp = require('./lib/getApp'),
      database = require('./lib/database');
const app = getApp(database);
const server = http.createServer(app);

// configuration for MongoDB USER: admin, PW: secret PORT: 27017
const mongoUrl = process.env.MONGO_URL || 'mongodb://admin:secret@localhost:27017/admin';

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
