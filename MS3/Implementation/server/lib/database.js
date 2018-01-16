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
    getUserOutfitClothing(uId, oId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
        }
        if (!oId) {
            throw new Error('id is missing.');
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
                for (var single_ownReq in mappings.requests) {
                    mappings.requests[single_ownReq].from = "own";
                    requests.push(mappings.requests[single_ownReq]);
                }
                //send results back to handler
                console.log(requests);
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

                for (var single_req in requests) {

                    for (var single_clothing in mapping) {
                        if (mapping[single_clothing].id == requests[single_req].cId)
                            delete mapping[single_clothing].uId;
                        delete mapping[single_clothing].id;
                        delete mapping[single_clothing].uId;
                        var obj = Object.assign(requests[single_req], mapping[single_clothing]);
                    }
                }
                console.log(requests);
                //
                // console.log(mapping.id);


                //send results back to handler
                callback(null, requests);
            })
        }

    },

    putUserProfile(uId, put, callback) {
        if (!uId) {
            throw new Error('id is missing.');
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
        }
        // find all elements

        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $set: put
        })
    },
    putUserRating(uId, id, put) {
        if (!id) {
            throw new Error('id is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
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
    putRequest(body, uId, id, callback) {
        if (!body) {
            throw new Error('body is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
        }
        if (!callback) {
            throw new Error('callback is missing.');
        }

        async.waterfall([
            async.apply(findRequest, this.mappings, id, body),
            async.apply(sendPush, this.mappings, body),
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

        function sendPush(mappings, body, callback) {
            if (body.status == "accepted") {
                mappings.findOne({
                    uId: body.uId,
                    type: "token"
                }, (err, mappings) => {
                    if (err) {
                        callback(null);
                    } else {
                        sendPushNotification(mappings.token, body.uId, mappings, "", "accepted");
                        callback(null);
                    }

                })
            }
        }
    },
    putClothing(cId, put, callback) {
        if (!cId) {
            throw new Error('id is missing.');
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
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
                var mappings_new = [];
                for (var i = 0; i < mappings.length; i++) {
                    // calc distance
                    var distance = calcDistance(mappings[i].latitude, mappings[i].longitude, params.latitude, params.longitude);
                    if (distance <= params.vicinity) {
                        // add distance
                        mappings[i].distance = distance;
                        mappings_new.push(mappings[i]);
                    }
                }
                mappings = mappings_new;
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
        }
        if (!longitude) {
            throw new Error('longitude is missing.');
        }
        if (!vicinity) {
            throw new Error('vicinity is missing.');
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
    postRequest(cId, body, callback) {
        if (!cId) {
            throw new Error('id is missing.');
        }
        if (!body) {
            throw new Error('body is missing.');
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
            confirmed: "0"
        };
        //write mapping to Database

        this.mappings.update({
            type: "userprofile",
            uId: mapping.uId
        }, {
            $push: {
                requests: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }
            this.mappings.findOne({
                uId: body.ouId,
                type: "token"
            }, (err, mappings) => {
                if (err) {
                    return callback(err);
                }
                sendPushNotification(mappings.token, cId, body.ouId, "", "postRequest");
                callback(null);
            });
        });

    },
    postUserToken(id, token, callback) {
        if (!id) {
            throw new Error('id is missing.');
        }
        if (!token) {
            throw new Error('Token is missing.');
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
        }
        if (!body) {
            throw new Error('body is missing.');
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
    postMessage(uId, message, callback) {
        if (!uId) {
            throw new Error('id is missing.');
        }
        if (!message) {
            throw new Error('message is missing.');
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

            sendPushNotification(mappings.token, message["to"], message, "", "message");
        })
        callback(null);
    },
    postUserRating(uId, rating, callback) {
        if (!uId) {
            throw new Error('id is missing.');
        }
        if (!rating) {
            throw new Error('rating is missing.');
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
            time: rating["time"]
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
            callback(null);
        });
    },
    deleteUserToken(id, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!id) {
            throw new Error('id is missing.');
        }
        // find all elements
        this.mappings.update({
                uId : uId,
                type : "userprofile"
            },
            {
                $pull : { requests : { id : id } }
            }, err => {
                console.log("yes");
                if (err) {
                    return callback(err);
                }
                //send results back to handler
                callback(	null, mappings);
            }
        );
    },
    deleteUserRequest(uId, id, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!id) {
            throw new Error('id is missing.');
        }


        this.mappings.update({
        uId : uId,
        type : "userprofile"
    },
    {
        $pull : { requests : { id : id } }
    },
    {},
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
        }
        if (!ouId) {
            throw new Error('ouId is missing.');
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
        if (!id) {
            throw new Error('id is missing.');
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
        if (!id) {
            throw new Error('id is missing.');
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
        if (!id) {
            throw new Error('id is missing.');
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
    postUserPrefer(id, prefer, callback) {
        if (!id) {
            throw new Error('id is missing.');
        }
        if (!prefer) {
            throw new Error('prefer is missing.');
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }

        const mapping = {
            id: uuidv4(),
            uId: id,
            prefer: prefer["prefer"],
            color: prefer["color"],
            size: prefer["size"],
            style: prefer["style"],
            art: prefer["art"],
            type: "prefer"
        };

        //write mapping to Database
        this.mappings.insertOne(mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null, mapping);
        });

        this.mappings.update({
            type: "userprofile"
        }, {
            $inc: {
                ['colors.' + prefer["color"]]: 1
            }
        })
        this.mappings.update({
            type: "userprofile"
        }, {
            $inc: {
                ['style.' + prefer["style"]]: 1
            }
        })
        this.mappings.update({
            type: "userprofile"
        }, {
            $inc: {
                ['art.' + prefer["art"]]: 1
            }
        })
        this.mappings.update({
            type: "userprofile"
        }, {
            $set: {
                ['sizes.' + prefer["art"]]: prefer["size"]
            }
        })
        // this.mappings.remove();
    },
    postUser(uId, callback) {
        if (!uId) {
            throw new Error('uId is missing.');
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const sizes = {
            tshirt: 0,
            sweatshirt: 0,
            trousers: 0,
            shoes: 0,
            hats: 0
        }
        const art = {
            tshirt: 0,
            sweatshirt: 0,
            trousers: 0,
            shoes: 0,
            hats: 0
        }
        const colors = {
            blue: 0,
            yellow: 0,
            red: 0,
            black: 0,
            gray: 0
        }
        const style = {
            sport: 0,
            casual: 0,
            working: 0,
            sleep: 0,
            bathing: 0
        }
        const mapping = {
            id: uuidv4(),
            uId: uId,
            gender: "?",
            colors: colors,
            style: style,
            sizes: sizes,
            art: art,
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
    getUserPrefer(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.findOne({
            type: "userprofile"
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getUserToken(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.findOne({
            uId: uId,
            type: "token"
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },
    getConversation(uId, ouId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('uId is missing.');
        }
        if (!ouId) {
            throw new Error('ouId is missing.');
        }
        // find all elements
        this.mappings.findOne({
            type: "userprofile",
            uId: uId,
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }

            this.mappings.find({
                type: "userprofile",
                "messages.from": ouId,
                "messages.to": uId,
            }).toArray((err, mapping) => {
                if (err) {
                    return callback(err);
                }
                var allMessages = [];
                for (var single_mapping in mapping) {
                    for (var one_message in mapping[single_mapping].messages) {
                        if (mapping[single_mapping].messages[one_message].to == uId) {
                            allMessages.push(mapping[single_mapping].messages[one_message]);
                        }
                    }

                }
                for (var single_Messages in mappings.messages) {
                    allMessages.push(mappings.messages[single_Messages]);
                }
                //send results back to handler
                callback(null, allMessages);
            })
        })


    },
    getClothingPrefer(uId, callback) {
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
    }

};



module.exports = database;
