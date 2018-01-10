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

const sendPushNotification = function(token, cId, payload, fits, from) {
 //TODO: Switch
 if (from=="message") {
   var message = {
       data: {
         message: payload["message"],
         sender: from
        // uId: cId
       }
     }
 };

  if (from == "accepted") {
  var message = {
      data: {
        uId: cId,
        sender: from
      }
    }
  };

    if (from == "missing") {
    var message = {
        data: {
            art: payload["art"],
            model: fits["model"],
            type: "missing",
            sender: from
        }
    };

    }

    if (from == "postRequest") {
    var message = {
        data: {
            cId: cId,
            ouId: payload,
            type: "postRequest",
            sender: from
        }
    };
    }

    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    firebase.messaging().sendToDevice(token, message, options).then(function(response) {
        console.log("Successfully", response.results);
    }).catch(function(error) {
        console.log("Error", error);
    });


}



module.exports = sendPushNotification;
