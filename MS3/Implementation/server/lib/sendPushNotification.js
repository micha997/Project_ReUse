'use strict';
const uuidv4 = require('uuid/v4');


var request = require('request');




const sendPushNotification = function(token, cId, payload, fits, from, firebase) {
 //TODO: Switch

 if (from=="message") {
   var message = {
       data: {
         message: payload["message"],
         sender: from,
         ouId: payload["from"]
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
