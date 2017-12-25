'use strict';

const searchPreferredClothing = function (prefer, clothing) {

  var i=0;
  for(var clothingObject in clothing)  {
    i=0;
    for (var prefColor in prefer["colors"]) {
      if  (JSON.stringify(clothing[clothingObject].color).replace(/\"/g, "") == prefColor) {
        clothing[clothingObject].position= i;
      }
      i++;
    }
    if (clothing[clothingObject].position > 2) {
      clothing.splice(clothingObject, 1);
    }
  }
    return clothing;
};


module.exports = searchPreferredClothing;
