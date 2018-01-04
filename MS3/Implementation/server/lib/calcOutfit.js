'use strict';

const calcOutfit = function(context, clothing) {
    var wintermodel = {
        wintermodel_head: {
          respiratory_activity_low: 0,
          respiratory_activity_high: 0,
          warmth_low: 7,
          warmth_high: 10,
          moisture_pickup_low: 0,
          moisture_pickup_high: 0,
          art: ["wollmütze","mütze"],
          model: "wintermodel_head"
        },
        wintermodel_1layer: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["tshirt", "shirt"],
            model: "wintermodel_1layer"
        },
        wintermodel_1layer_alternative: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["tshirt", "shirt"],
            model: "wintermodel_1layer_alternative"
        },
        wintermodel_2layer: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 4,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["tshirt", "shirt"],
            model: "wintermodel_2layer"
        },
        wintermodel_2layer_alternative: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["tshirt", "shirt"],
            model: "wintermodel_1layer_alternative"
        },
        wintermodel_3layer: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 8,
            warmth_high: 10,
            moisture_pickup_low: 0,
            moisture_pickup_high: 5,
            art: ["wintermantel","mantel"],
            model: "wintermodel_3layer"
        },
        wintermodel_3layer_alternative: {
            respiratory_activity_low: 4,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 5,
            moisture_pickup_high: 10,
          art: ["wintermantel","mantel"],
            model: "wintermodel_3layer_alternative"
        },
        wintermodel_bottom: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["hose", "jeans"],
            model: "wintermodel_bottom"
        },
        wintermodel_shoes: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["shoes","schuhe"],
            model: "shoes"
        }

    }

    var outfit =    {
            model: "wintermodel",
            head: 0,
            layer1: 0,
            layer2: 0,
            layer3: 0,
            bottom: 0,
            shoes: 0,
            layers: ["head", "layer1", "layer2", "layer3", "bottom", "shoes"]
            }

    var charac = [{
            name: "baumwolle",
            warmth: 6,
            moisture_pickup: 2,
            respiratory_activity: 10
        },
        {
            name: "wolle",
            warmth: 9,
            moisture_pickup: 4,
            respiratory_activity: 9
        },
        {
            name: "viskose",
            warmth: 6,
            moisture_pickup: 3,
            respiratory_activity: 9
        },
        {
            name: "modal",
            warmth: 6,
            moisture_pickup: 3,
            respiratory_activity: 9
        },
        {
            name: "polyester",
            warmth: 9,
            moisture_pickup: 9,
            respiratory_activity: 7
        },
        {
            name: "leinen",
            warmth: 4,
            moisture_pickup: 1,
            respiratory_activity: 10
        },
        {
            name: "polyacryl",
            warmth: 7,
            moisture_pickup: 8,
            respiratory_activity: 8
        },
        {
            name: "polyamid",
            warmth: 7,
            moisture_pickup: 8,
            respiratory_activity: 4
        },
        {
            name: "seide",
            warmth: 3,
            moisture_pickup: 7,
            respiratory_activity: 9
        },
        {
            name: "kaschmir",
            warmth: 10,
            moisture_pickup: 8,
            respiratory_activity: 8
        },
        {
            name: "denim",
            warmth: 7,
            moisture_pickup: 4,
            respiratory_activity: 8
        },
        {
            name: "leder",
            warmth: 8,
            moisture_pickup: 9,
            respiratory_activity: 8
        },
        {
            name: "synthetik",
            warmth: 6,
            moisture_pickup: 10,
            respiratory_activity: 2
        }

    ];
    if (context== "winter") {
      outfit.head = calcLayerClothing(wintermodel["wintermodel_head"], clothing, charac);
      if (outfit.head == 0) {
          console.log("There is no clothing for head winter!");
      }
      outfit.layer1 = calcLayerClothing(wintermodel["wintermodel_1layer"], clothing, charac);
      if (outfit.layer1 == 0) {
          outfit.layer1 = calcLayerClothing(wintermodel["wintermodel_1layer_alternative"], clothing, charac);
          if (outfit.layer1 == 0) {
              console.log("There is no clothing for layer 1 winter!");
          }
      }
      outfit.layer2 = calcLayerClothing(wintermodel["wintermodel_2layer"], clothing, charac);
      if (outfit.layer2 == 0) {
          outfit.layer2 = calcLayerClothing(wintermodel["wintermodel_2layer_alternative"], clothing, charac);
          if (outfit.layer2 == 0) {
              console.log("There is no clothing for layer 3 winter!");
          }
      }
      outfit.layer3 = calcLayerClothing(wintermodel["wintermodel_3layer"], clothing, charac);
      if (outfit.layer3 == 0) {
          outfit.layer3 = calcLayerClothing(wintermodel["wintermodel_3layer_alternative"], clothing, charac);
          if (outfit.layer3 == 0) {
              console.log("There is no clothing for layer 3 winter!");
          }
      }
      outfit.bottom = calcLayerClothing(wintermodel["wintermodel_bottom"], clothing, charac);
      if (outfit.bottom == 0) {
          console.log("There is no clothing for bottom winter!");
      }
      outfit.shoes = calcLayerClothing(wintermodel["wintermodel_shoes"], clothing, charac);
      if (outfit.shoes == 0) {

          console.log("There is no clothing for shoes winter!");
      }
          return wintermodel;
    }

    function calcLayerClothing(model, clothing, charac) {
        var result = [];
        for (var single_charac in charac) {
            if ((model.respiratory_activity_low <= charac[single_charac].respiratory_activity && model.respiratory_activity_high >= charac[single_charac].respiratory_activity) || (model.respiratory_activity_high == 0 && model.respiratory_activity_low == 0)) {
                if ((model.warmth_low <= charac[single_charac].warmth && model.warmth_high >= charac[single_charac].warmth) || (model.warmth_high == 0 && model.warmth_low == 0)) {
                    if ((model.moisture_pickup_low <= charac[single_charac].moisture_pickup && model.moisture_pickup_high >= charac[single_charac].moisture_pickup) || (model.moisture_pickup_high == 0 && model.moisture_pickup_low == 0)) {
                        for (var single_clothing in clothing) {
                            for (var art in model.art) {
                                if (clothing[single_clothing].art == model.art[art] && clothing[single_clothing].fabric == charac[single_charac].name) {
                                    result.push(clothing[single_clothing].id);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    };
}

module.exports = calcOutfit;
