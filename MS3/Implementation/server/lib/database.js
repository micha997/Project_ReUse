'use strict';

const mongo = require('mongodb');
const calcDistance = require('./calcDistance');
const calcOutfit = require('./calcOutfit');
const sendPushNotification = require('./sendPushNotification');
const MongoClient = mongo.MongoClient;
const uuidv4 = require('uuid/v4');
const async = require("async");

const database = {
    initialize(connectionString, callback) {
        MongoClient.connect(connectionString, {
            autoReconnect: true
        }, (err, database) => {
            // check for missing arguments
            if (err) {
                return callback(err);
            }
            if (!connectionString) {
                throw new Error('connectionString is missing.');
            }
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            // define collection name
            const mappings = database.collection('mappings');
            this.mappings = mappings;
            callback(null);
        });
    },
    getClothing(cId, callback) {
        if (!cId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.findOne({
            type: "clothing",
            id: cId
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getCustomeClothing(filter, latitude, longitude, vicinity, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!latitude) {
            throw new Error('latitude is missing.');
            callback(err);
        }
        if (!longitude) {
            throw new Error('longitude is missing.');
            callback(err);
        }
        if (!vicinity) {
            throw new Error('vicinity is missing.');
            callback(err);
        }
        if (!filter) {
            throw new Error('filter is missing.');
            callback(err);
        }
        var type = {
            type: "clothing"
        };
        // find all elements
        this.mappings.find(
            filter
        ).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            var clothing = calcClothingDistance(mappings, latitude, longitude, vicinity);

            //send results back to handler
            callback(null, clothing);
        })
    },
    getUserOutfitClothing(uId, oId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!oId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({
            type: "clothing",
            uId: uId,
            active: "false",
            oId: oId
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getUserOutfit(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({
            type: "clothing",
            uId: uId,
            active: "false"
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getUserProfile(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.findOne({
            type: "userprofile",
            uId: uId
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getUserRequests(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements


        async.waterfall([
            async.apply(findOwnRequests, this.mappings, uId),
            async.apply(findOtherRequests, this.mappings),
            async.apply(searchClothing, this.mappings)
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                return callback(null, result);
            }
        });

        function findOwnRequests(mappings, uId, callback) {
            mappings.find({
                type: "userprofile",
                "requests.ouId": uId
            }).toArray((err, mapping) => {
                if (err) {
                    callback("1", null);
                } else {
                    callback(null, mapping);
                }
            })
        }

        function findOtherRequests(mappings, mapping, callback) {
            var requests = [];

            for (var single_map in mapping) {
                var obj = mapping[single_map].requests;
                for (var single_request in obj) {
                    if (obj[single_request].ouId == uId) {
                        obj[single_request].from = "foreign";
                        requests.push(obj[single_request]);
                    }
                }
            }
            mappings.findOne({
                type: "userprofile",
                uId: uId
            }, (err, mappings) => {
                if (err) {
                    callback("1", null);
                }
                for (var single_profile in mappings) {
                  delete mappings.image;
                }
                for (var single_ownReq in mappings.requests) {
                    mappings.requests[single_ownReq].from = "own";
                    requests.push(mappings.requests[single_ownReq]);
                }
                //send results back to handler
                callback(null, requests);
            })
        }

        function searchClothing(mappings, requests, callback) {

            mappings.find({
                type: "clothing",
            }).toArray((err, mapping) => {
                if (err) {
                    callback("1", null);
                }
                var results= [];
                for (var single_req in requests) {

                    for (var single_clothing in mapping) {
                        if (mapping[single_clothing].id == requests[single_req].cId) {
                            delete mapping[single_clothing].uId;
                        delete mapping[single_clothing].id;
                        delete mapping[single_clothing].uId;

                        var obj = Object.assign(requests[single_req], mapping[single_clothing]);
                        results.push(obj);

                        }
                    }

                }
                console.log(obj);
                //send results back to handler
                callback(null, results);
            })
        }

    },

    putUserProfile(uId, put, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
            callback(err);
        }
        // find all elements

        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $set: put
        })
    },
    putUserRating(uId, id, put, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!put) {
            throw new Error('put is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('callback is missing.');
        }
        this.mappings.update({
            type: "userprofile",
            uId: uId,
            "rating.id": id
        }, {
            $push: {
                rating: put
            }
        })
    },
    putRequest(body, uId, id, firebase, callback) {
        if (!body) {
            throw new Error('body is missing.');
            callback(err);
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('callback is missing.');
        }


        async.waterfall([
            async.apply(findRequest, this.mappings, id, body),
            async.apply(sendPush, this.mappings, body, firebase),
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                return callback(null, result);
            }
        });

        function findRequest(mappings, id, body, callback) {
            mappings.update({
                type: "userprofile",
                "requests.id": id,

            }, {
                $set: {
                    "requests.$.status": body.status,
                    "requests.$.confirmed": body.confirmed

                }
            }, (err) => {
                if (err) {
                    callback("1");
                } else {


                    callback(null)
                }
            })
        }

        function sendPush(mappings, body, firebase, callback) {
                mappings.findOne({
                    uId: body.uId,
                    type: "token"
                }, (err, mappings) => {
                    if (err) {
                        callback(null);
                    } else {
                        sendPushNotification(mappings.token, body.uId, mappings, "", body.status, firebase);
                        callback(null);
                    }

                })
        }
    },
    putClothing(cId, put, callback) {
        if (!cId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.update({
            type: "clothing",
            id: cId
        }, {
            $set: put,
        })
    },
    getUserClothing(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({
            type: "clothing",
            uId: uId
        }).toArray((err, mappings) => {

            if (err) {
                return callback(err);
            }
            for (var single_mappings in mappings)
            delete mappings[single_mappings].image;
            //send results back to handler
            callback(null, mappings);
        })
    },
    // send all DB-Values
    getAll(callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({}).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getUserRating(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.findOne({
            type: "userprofile",
            uId: uId
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getOutfit(choise, art, params, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!art) {
            throw new Error('art is missing.');
            callback(err);
        }
        if (!params) {
            throw new Error('params is missing.');
            callback(err);
        }
        if (!choise) {
            throw new Error('choise is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.find({
            type: "clothing"
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            if (choise == "true") {

                var mappings = calcClothingDistance(mappings, params.latitude, params.longitude, params.vicinity);

            }
            var clothing = calcOutfit("winter", mappings, false);

            // search for elements in vicinity + add distance
            callback(null, clothing);
        })
    },
    getAllClothingLocation(latitude, longitude, vicinity, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!latitude) {
            throw new Error('latitude is missing.');
            callback(err);
        }
        if (!longitude) {
            throw new Error('longitude is missing.');
            callback(err);
        }
        if (!vicinity) {
            throw new Error('vicinity is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.find({
            type: "clothing"
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }

            // container for elements in vicinity
            var mappings_new = [];
            // search for elements in vicinity + add distance
            delete mappings.image;

            for (var i = 0; i < mappings.length; i++) {
                // calc distance
                var distance = calcDistance(mappings[i].latitude, mappings[i].longitude, latitude, longitude);
                if (distance <= vicinity) {
                    // add distance
                    mappings[i].distance = distance;
                    mappings_new.push(mappings[i]);
                }
            }
            callback(null, mappings_new);
        })
    },

    addClothing(clothing, callback) {
        if (!clothing) {
            throw new Error('Clothing is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        clothing = JSON.parse(clothing);

        const mapping = {
            id: uuidv4(),
            longitude: clothing["longitude"],
            latitude: clothing["latitude"],
            size: clothing["size"],
            art: clothing["art"],
            color: clothing["colour"],
            style: clothing["style"],
            gender: clothing["gender"],
            fabric: clothing["fabric"],
            notes: clothing["notes"],
            brand: clothing["brand"],
            date: Date.now(),
            postalCode: clothing["postalCode"],
            city: clothing["city"],
            uId: clothing["uId"],
            type: "clothing",
            image: clothing["image"]
        };

        async.waterfall([
            async.apply(insertClothing, this.mappings),
            searchFits,
            async.apply(findUsers, this.mappings),
            async.apply(sendPush, this.mappings),
        ], function(err) {
            if (err == "1") {
                return callback(err);
            } else {
                callback(null);
            }
        });

        function insertClothing(mappings, callback) {
            mappings.insertOne(mapping, err => {
                if (err) {
                    callback("1");
                } else {
                    callback(null, mappings, mapping);
                }
            })

        }

        function searchFits(mappings, mapping, callback) {
            var fits = calcOutfit(null, mapping, true);
            callback(null, mapping, fits);
        }

        function findUsers(mappings, mapping, fits, callback) {

            function queryCollection(mappings, callback) {
                mappings.find({
                    type: "userprofile"
                }).toArray(function(err, users) {
                    if (err) {
                        callback("2");
                    } else if (users.length > 0) {
                        callback(users);
                    }
                });
            }

            queryCollection(mappings, function(users) {
                callback(null, mapping, fits, users);
                //You can do more stuff with the result here
            });
        }

        function sendPush(mappings, mapping, fits, users, callback) {
            //callback(null, 'done');
            try {
                for (var single_mapping in users) {
                    if (users[single_mapping].subscription != null) {
                        for (var single_subscription in users[single_mapping].subscription) {
                            if (fits.model == users[single_mapping].subscription[single_subscription].type + "_" + users[single_mapping].subscription[single_subscription].missing) {
                                mappings.find({
                                    uId: users[single_mapping].uId,
                                    type: "token"
                                }).toArray(function(err, users) {
                                    if (err) {
                                        callback("2");
                                    } else {
                                        var i = 0;
                                        for (var map in users) {
                                            sendPushNotification(users[map].token, "0", mapping, fits, "missing");
                                        }
                                    }
                                })

                            }
                        }
                    }

                }
                callback(null);
            } catch (e) {
                callback("2");
            }

        }

    },
    postRequest(cId, body, firebase, callback) {
        if (!cId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!body) {
            throw new Error('body is missing.');
            callback(err);
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            cId: cId,
            uId: body.uId,
            ouId: body.ouId,
            status: "open",
            confirmed: "0",
            closed: "0",
            finished:"0"
        };
        //write mapping to Database

        async.waterfall([
            async.apply(findRequest, this.mappings, mapping),
            async.apply(findUserProfile, this.mappings, mapping),
            async.apply(sendMessage, this.mappings, body.ouId, mapping.cId)
        ], function(err) {
            if (err == "1") {
                return callback(err);
            } else {
                callback(null);
            }
        });


        function findRequest(mappings, mapping, callback) {
            var flag="0";
            mappings.findOne({
                uId: mapping.uId,
                type: "userprofile",
              }, (err, userprofile) => {
                    if (err) {
                        callback("1", null);
                    } else {
                    for ( var single_req in userprofile.requests) {
                      if (userprofile.requests[single_req].cId == mapping.cId) {
                          callback("1", null);
                          flag="1";
                      }
                      }
                      if (flag=="0") {
                        callback(null);
                        }
                      }
                    })
                  }

            function findUserProfile(mappings, mapping, callback) {
                          mappings.update({
                              type: "userprofile",
                              uId: mapping.uId
                          }, {
                              $push: {
                                  requests: mapping
                              }
                          }, (err) => {
                            if (err) {
                                callback("1", null);
                            } else {
                            callback(null);
                            }
                          })
                        }

          function sendMessage(mappings, uId, cId ,callback) {
            mappings.findOne({
                uId: uId,
                type: "token"
            }, (err, mappings) => {
                if (err) {
                    callback("1", null);
                }
                sendPushNotification(mappings.token, cId, uId, "", "postRequest", firebase);
                callback(null);
            });
          }




          },

    postUserToken(id, token, callback) {
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!token) {
            throw new Error('Token is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            uId: id,
            token: token,
            type: "token"
        };
        //write mapping to Database
        this.mappings.insertOne(mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null);
        });
    },
    postUserSearch(uId, body, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!body) {
            throw new Error('body is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        //write mapping to Database
        const mapping = {
            id: uuidv4(),
            type: body.model,
            missing: body.missing,
            time: "heute"
        };

        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $push: {
                subscription: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null);
        });
    },
    postMessage(uId, message, firebase, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!message) {
            throw new Error('message is missing.');
            callback(err);
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            from: message["from"],
            to: message["to"],
            message: message["message"],
            attach: message["attach"],
            time: message["time"]
        };
        //write mapping to Database
        //write mapping to Database
        this.mappings.update({
            type: "userprofile",
            uId: message["from"]
        }, {
            $push: {
                messages: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }

        });
        this.mappings.findOne({
            uId: mapping["to"],
            type: "token"
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }

            sendPushNotification(mappings.token, message["to"], message, "", "message", firebase);
        })
        callback(null);
    },
    postUserRating(uId, rating, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!rating) {
            throw new Error('rating is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            type: "rating",
            from: rating["from"],
            choice: rating["choice"],
            comment: rating["comment"],
            tId: rating["tId"],
            time: rating["time"],
            rfrom: rating["rfrom"],
            finished: rating["finished"]
        };
        //write mapping to Database
        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $push: {
                rating: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }
            this.mappings.update({
                type: "userprofile",
                "requests.id": rating["tId"],

            }, {
                $set: {
                    "requests.$.status": "closed",
                    "requests.$.closed": rating["rFrom"],
                    "requests.$.finished": rating["finished"]
                }
            }, (err) => {
                if (err) {
                    callback("1");
                } else {
                    callback(null)
                }
            })

        });
    },
    deleteUserRequest(uId, id, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }


        this.mappings.update({
                uId: uId,
                type: "userprofile"
            }, {
                $pull: {
                    requests: {
                        id: id
                    }
                }
            }, {},
            err => {
                if (err) {
                    return callback(err);
                }
                //send results back to handler
                return callback(null);
            })
    },
    deleteConversation(uId, ouId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('uId is missing.');
            callback(err);
        }
        if (!ouId) {
            throw new Error('ouId is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.remove({
            from: uId,
            to: ouId
        }), err => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        }
    },
    deleteUserProfile(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.remove({
            'uId': uId,
            type: "userprofile"
        }), err => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        }
    },
    deleteUser(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.remove({
            'uId': uId,
            type: "clothing"
        }), err => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        }
    },
    deleteUserClothing(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.remove({
            'uId': uId,
            type: "clothing"
        }), err => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        }
    },
    postUser(uId, callback) {
        if (!uId) {
            throw new Error('uId is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            uId: uId,
            gender: "?",
            type: "userprofile"
        };
        //write mapping to Database
        this.mappings.insertOne(mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null);
        });
    },
    getConversation(uId, ouId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('uId is missing.');
            callback(err);
        }
        if (!ouId) {
            throw new Error('ouId is missing.');
            callback(err);
        }



        async.waterfall([
            async.apply(findOwnMessages, this.mappings, uId),
            async.apply(findOtherMessages, this.mappings, uId, ouId)
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                return callback(null, result);
            }
        });

        function findOwnMessages(mappings, uId, callback) {
            mappings.findOne({
                type: "userprofile",
                uId: uId,
            }, (err, mappings) => {
                if (err) {
                    callback("1", null);
                } else {
                    callback(null, mappings);
                }
            })
        }

        function findOtherMessages(mappings, uId, ouId, ownMessages, callback) {
            mappings.find({
                type: "userprofile",
                "messages.from": ouId,
                "messages.to": uId,
            }).toArray((err, mapping) => {
                if (err) {
                    callback("1", null);
                }
                var allMessages = [];
                for (var single_mapping in mapping) {
                    for (var one_message in mapping[single_mapping].messages) {
                        if (mapping[single_mapping].messages[one_message].to == uId) {
                            allMessages.push(mapping[single_mapping].messages[one_message]);
                        }
                    }

                }
                for (var single_Messages in ownMessages.messages) {
                    allMessages.push(ownMessages.messages[single_Messages]);
                }
                //send results back to handler
                callback(null, allMessages);
            })
        }
    },

};

function calcClothingDistance(mappings, latitude, longitude, vicinity) {
    var mappings_new = [];
    for (var i = 0; i < mappings.length; i++) {
        // calc distance
        var distance = calcDistance(mappings[i].latitude, mappings[i].longitude, latitude, longitude);
        if (distance <= vicinity) {
            // add distance
            mappings[i].distance = distance;
            mappings_new.push(mappings[i]);
        }
    }
    return mappings_new;
}



module.exports = database;
