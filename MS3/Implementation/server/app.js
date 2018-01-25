'use strict';

const https = require('https');
const fs = require('fs');
const getApp = require('./lib/getApp');
const database = require('./lib/database');

const serviceAccount = require("./eis1718-ef0c1-firebase-adminsdk-diqi4-393278b7ef.json");
let firebase = require("firebase-admin");
var API_KEY = "AAAAq9R19N8:APA91bEnRFTBgRH79QW6AVAfBS5mkQZlsQTwORFbcdKs0GIpgNBy-fk2odgLsMhPbIzWXeVW-Y0Xfaa4gO_toFjeqC_-D1KmVI26Gk1GFv1tDLJwIChi-qxAfU6B9uRJlb9xCH-WvkT8";
const app = getApp(database,firebase);



// configuration for MongoDB USER: admin, PW: secret PORT: 27017
//const mongoUrl = process.env.MONGO_URL || 'mongodb://admin:secret@localhost:27017/admin';
const mongoUrl = process.env.MONGO_URL || 'mongodb://klamotten:v3rt31l3r@ds257245.mlab.com:57245/klamotten-verteiler';

firebase.initializeApp({
    credential: firebase.credential.cert(serviceAccount),
    databaseURL: "https://eis1718-ef0c1.firebaseio.com/",
    apiKey: API_KEY,
    projectId: "eis1718-ef0c1"
});


// Load HTTPS configuration
const options = {
    key: fs.readFileSync(__dirname + '/keys/key.pem', 'utf8'),
    cert: fs.readFileSync(__dirname + '/keys/server.crt', 'utf8')
};

// define server
const server = https.createServer(options,app);

// server port
const port = process.env.PORT || 50262;



// initialize MongoDB
database.initialize(mongoUrl, err => {
    if (err) {
        console.log('Failed to connect to database.', {
            err
        });
        process.exit(1);
    }
});

const requestHandler = (request, response) => {
    console.log(request.url)
    response.end('Hello Node.js Server!')
}

// start server
server.listen(port, () => {
    console.log('Server is started.', {
        port
    });
});
