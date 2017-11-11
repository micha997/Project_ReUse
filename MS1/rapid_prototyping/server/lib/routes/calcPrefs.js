'use strict';
const uuidv4 = require('uuid/v4');

const calcPrefs = function (mappings) {
    var colors =  mappings.colors;
    const countBlue = colors.blue;
    const countBlack = colors.black;
    const countYellow = colors.yellow;
    const countRed = colors.red;
    const countGray = colors.gray;
    const countPrefColor = countBlue + countBlack + countYellow + countRed + countGray;

    const percentBlue = calcPercent(countPrefColor, countBlue);
    const percentBlack = calcPercent(countPrefColor, countBlack);
    const percentYellow = calcPercent(countPrefColor, countYellow);
    const percentRed = calcPercent(countPrefColor, countRed);
    const percentGray = calcPercent(countPrefColor, countGray);

    var percentColor = {};
    percentColor["blue"] = percentBlue;
    percentColor["black"] = percentBlack;
    percentColor["yellow"] = percentYellow;
    percentColor["red"] = percentRed;
    percentColor["gray"] = percentGray;


    var keysSorted = Object.keys(percentColor).sort(function(a,b) {
      return percentColor[a]-percentColor[b]
    })

    var colorSorted = {};

    for (var i=0; i<keysSorted.length;i++) {
      colorSorted[keysSorted[i]] = percentColor[keysSorted[i]];
    }
    console.log(colorSorted);
    colorSorted["uId"] = mappings.uId;
    colorSorted["type"] = "colorprefer";
    
    return colorSorted;
};

const calcPercent = function (countPrefColor, countColor) {
    var percentColor = 100 / countPrefColor * countColor;
    return percentColor;
};

module.exports = calcPrefs;
