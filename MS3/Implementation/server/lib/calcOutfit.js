'use strict';

const calcOutfit = function(context, clothing) {
    var wintermodel = {
      model: "wintermodel",
      head: 0,
      layer1: 0,
      layer2: 0,
      layer3: 0,
      bottom: 0,
      shoes: 0
    }
    
    var wintermodel_1layer = {
        respiratory_activity_low: 7,
        respiratory_activity_high: 10,
        warmth_low: 0,
        warmth_high: 0,
        moisture_pickup_low: 0,
        moisture_pickup_high: 0,
        art: ["tshirt", "shirt"],
        model: "wintermodel_1layer"
    }

    var wintermodel_1layer_alternative = {
        respiratory_activity_low: 7,
        respiratory_activity_high: 10,
        warmth_low: 0,
        warmth_high: 0,
        moisture_pickup_low: 0,
        moisture_pickup_high: 0,
        art: ["tshirt", "shirt"],
        model: "wintermodel_1layer_alternative"
    }

    var wintermodel_2layer = {
        respiratory_activity_low: 0,
        respiratory_activity_high: 4,
        warmth_low: 0,
        warmth_high: 0,
        moisture_pickup_low: 0,
        moisture_pickup_high: 0,
        art: ["tshirt", "shirt"],
        model: "wintermodel_2layer"
    }

    var wintermodel_2layer_alternative = {
        respiratory_activity_low: 7,
        respiratory_activity_high: 10,
        warmth_low: 0,
        warmth_high: 0,
        moisture_pickup_low: 0,
        moisture_pickup_high: 0,
        art: ["tshirt", "shirt"],
        model: "wintermodel_1layer_alternative"
    }

    var wintermodel_3layer = {
        respiratory_activity_low: 0,
        respiratory_activity_high: 0,
        warmth_low: 8,
        warmth_high: 10,
        moisture_pickup_low: 0,
        moisture_pickup_high: 5,
        art: ["wintermantel"],
        model: "wintermodel_3layer"
    }

    var wintermodel_3layer_alternative = {
        respiratory_activity_low: 4,
        respiratory_activity_high: 10,
        warmth_low: 0,
        warmth_high: 0,
        moisture_pickup_low: 5,
        moisture_pickup_high: 10,
        art: ["wintermantel"],
        model: "wintermodel_3layer_alternative"
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

    wintermodel.layer1 = calcLayerClothing(wintermodel_1layer, clothing, charac);
    if (wintermodel.layer1 == 0) {
        wintermodel.layer1 = calcLayerClothing(wintermodel_1layer_alternative, clothing, charac);
        if (wintermodel.layer1 == 0) {
            console.log("There is no clothing for layer 1 winter!");
        }
    }
    wintermodel.layer2 = calcLayerClothing(wintermodel_2layer, clothing, charac);
    if (wintermodel.layer2 == 0) {
        wintermodel.layer2 = calcLayerClothing(wintermodel_2layer_alternative, clothing, charac);
        if (wintermodel.layer2 == 0) {
            console.log("There is no clothing for layer 3 winter!");
        }
    }
    wintermodel.layer3 = calcLayerClothing(wintermodel_3layer, clothing, charac);
    if (wintermodel.layer3 == 0) {
        wintermodel.layer3 = calcLayerClothing(wintermodel_3layer_alternative, clothing, charac);
        if (wintermodel.layer3 == 0) {
            console.log("There is no clothing for layer 3 winter!");
        }
    }

    return wintermodel;



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
