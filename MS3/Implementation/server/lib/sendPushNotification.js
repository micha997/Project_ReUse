'use strict';
const uuidv4 = require('uuid/v4');
const firebase = require("firebase-admin");
const serviceAccount = require("../eis1718-ef0c1-firebase-adminsdk-diqi4-393278b7ef.json")
var request = require('request');

var API_KEY = "AAAAq9R19N8:APA91bEnRFTBgRH79QW6AVAfBS5mkQZlsQTwORFbcdKs0GIpgNBy-fk2odgLsMhPbIzWXeVW-Y0Xfaa4gO_toFjeqC_-D1KmVI26Gk1GFv1tDLJwIChi-qxAfU6B9uRJlb9xCH-WvkT8";

firebase.initializeApp({
    credential: firebase.credential.cert(serviceAccount),
    databaseURL: "https://eis1718-ef0c1.firebaseio.com/",
    apiKey: API_KEY
});

const sendPushNotification = function(token, cId, mapping, fits) {


    console.log(fits);


    var payload = {
        data: {
            art: mapping["art"],
            model: fits["model"],
            type: "missing"
        }
    };

    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    firebase.messaging().sendToDevice(token, payload, options).then(function(response) {
        console.log("Successfully", response.results);
    }).catch(function(error) {
        console.log("Error", error);
    });


}



module.exports = sendPushNotification;
