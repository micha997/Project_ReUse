'use strict';

const mongo = require('mongodb');
const calcDistance = require('./calcDistance')
const MongoClient = mongo.MongoClient;
const uuidv4 = require('uuid/v4');

const database = {
  initialize(connectionString, callback) {
    MongoClient.connect(connectionString, { autoReconnect: true}, (err, database) => {
      // check for missing arguments
      if (err) {
        return callback(err);
      }
      if (!connectionString) {
        throw new Error ('connectionString is missing.');
      }
      if (!callback) {
        throw new Error ('Callback is missing.');
      }
      // define collection name
      const mappings=database.collection('mappings');
      this.mappings = mappings;
      callback(null);
    });
  },
  getClothing(cId, callback) {
    if (!cId) {
      throw new Error ('id is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.findOne({type: "clothing", id: cId}, (err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getUserProfile(uId, callback) {
  if (!uId) {
    throw new Error ('id is missing.');
  }
  if (!callback) {
    throw new Error ('Callback is missing.');
  }
  // find all elements
  this.mappings.findOne({type: "userprofile", uId : uId}, (err,mappings) => {
    if (err) {
      return callback(err);
    }
    //send results back to handler
    callback(null, mappings);
  })
},
putUserProfile(uId, put, callback) {
if (!uId) {
  throw new Error ('id is missing.');
}
if (!callback) {
  throw new Error ('Callback is missing.');
}
if (!put) {
  throw new Error ('put is missing.');
}
// find all elements

this.mappings.update(
    { type:"userprofile", uId: uId},
    {
      $set: put ,
    }
)
},
putClothing(cId, put, callback) {
if (!cId) {
  throw new Error ('id is missing.');
}
if (!callback) {
  throw new Error ('Callback is missing.');
}
if (!put) {
  throw new Error ('put is missing.');
}
// find all elements
console.log("hi");
this.mappings.update(
    { type:"clothing", id: cId},
    {
      $set: put ,
    }
)
},
  getUserClothing(uId, callback) {
    if (!uId) {
      throw new Error ('id is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.find({type: "clothing", uId : uId}).toArray((err,mappings) => {
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
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.find({}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getUserRating(uId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!uId) {
      throw new Error ('id is missing.');
    }
    // find all elements
    this.mappings.findOne({type: "userprofile", uId: uId}, (err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getOutfit(callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.find({}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getAllClothingLocation(latitude, longitude, vicinity, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!latitude) {
      throw new Error ('latitude is missing.');
    }
    if (!longitude) {
      throw new Error ('longitude is missing.');
    }
    if (!vicinity) {
      throw new Error ('vicinity is missing.');
    }
    // find all elements
    this.mappings.find({type: "clothing"}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }

      // container for elements in vicinity
      var mappings_new = [];
      // search for elements in vicinity + add distance
      delete mappings.image;

      for (var i = 0; i < mappings.length; i++) {
        // calc distance
        var distance = calcDistance(mappings[i].latitude, mappings[i].longitude,latitude,longitude);
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
      throw new Error ('Clothing is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
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
    //write mapping to Database
    this.mappings.insertOne(mapping, err => {
      if (err) {
        return callback(err);
      }
      callback(null);
    });
  },

  postUserToken(id, token, callback) {
    if (!id) {
      throw new Error ('id is missing.');
    }
    if (!token) {
      throw new Error ('Token is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
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
  postMessage(id, message, callback) {
    if (!id) {
      throw new Error ('id is missing.');
    }
    if (!message) {
      throw new Error ('message is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    const mapping = {
      id: uuidv4(),
      type: "message",
      from: message["from"],
      to: message["from"],
      message: message["message"],
      attach: message["attach"],
      time: message["time"]
    };
    //write mapping to Database
    this.mappings.insertOne(mapping, err => {
      if (err) {
        return callback(err);
      }
      callback(null);
    });
  },
  postUserRating(uId, rating, callback) {
    if (!uId) {
      throw new Error ('id is missing.');
    }
    if (!rating) {
      throw new Error ('rating is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    const mapping = {
      id: uuidv4(),
      type:"rating",
      from: uId,
      choice: rating["choice"],
      comment: rating["comment"],
      time: rating["time"]
    };
    //write mapping to Database
    this.mappings.update(  { type: "userprofile", uId: uId  },
   { $push: { rating: mapping } }, mapping, err => {
      if (err) {
        return callback(err);
      }
      callback(null);
    });
  },
  deleteUserToken(id, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!id) {
      throw new Error ('id is missing.');
    }
    // find all elements
    this.mappings.remove({'uId':id, type: "token"}), err => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    }
  },
  deleteConversation(uId, ouId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!uId) {
      throw new Error ('uId is missing.');
    }
    if (!ouId) {
      throw new Error ('ouId is missing.');
    }
    // find all elements
    this.mappings.remove({from: uId, to: ouId}), err => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    }
  },
  deleteUserProfile(uId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!id) {
      throw new Error ('id is missing.');
    }
    // find all elements
    this.mappings.remove({'uId':uId, type: "userprofile"}), err => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    }
  },
  deleteUser(uId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!id) {
      throw new Error ('id is missing.');
    }
    // find all elements
    this.mappings.remove({'uId':uId, type: "clothing"}), err => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    }
  },
  deleteUserClothing(uId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!id) {
      throw new Error ('id is missing.');
    }
    // find all elements
    this.mappings.remove({'uId':uId, type: "clothing"}), err => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    }
  },
  postUserPrefer(id, prefer, callback) {
    if (!id) {
      throw new Error ('id is missing.');
    }
    if (!prefer) {
      throw new Error ('prefer is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
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

    this.mappings.update({type:"userprofile"}, {$inc: {['colors.'+prefer["color"]]: 1}})
    this.mappings.update({type:"userprofile"}, {$inc: {['style.'+prefer["style"]]: 1}})
    this.mappings.update({type:"userprofile"}, {$inc: {['art.'+prefer["art"]]: 1}})
    this.mappings.update({type:"userprofile"}, {$set: {['sizes.'+prefer["art"]]: prefer["size"]}})
    // this.mappings.remove();
  },
  postUser(uId, callback) {
    if (!uId) {
      throw new Error ('uId is missing.');
    }
    if (!callback) {
      throw new Error ('Callback is missing.');
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
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.findOne({type: "userprofile"}, (err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getConversation(uId, ouId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    if (!uId) {
      throw new Error ('uId is missing.');
    }
    if (!ouId) {
      throw new Error ('ouId is missing.');
    }
    // find all elements
    this.mappings.find({from: uId, to: ouId}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  },
  getClothingPrefer(uId, callback) {
    if (!callback) {
      throw new Error ('Callback is missing.');
    }
    // find all elements
    this.mappings.find({}).toArray((err,mappings) => {
      if (err) {
        return callback(err);
      }
      //send results back to handler
      callback(null, mappings);
    })
  }

};



module.exports = database;