'use strict';

const getIndex = function () {

  return function (req, res)  {
    res.send("Welcome!");
  };
};

module.exports = getIndex;
