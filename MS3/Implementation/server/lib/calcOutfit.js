'use strict';

/* Die Funktion calcOutfit bietet die Möglichkeit aus einzelnen Kleidungsstücken sinnvolle Outfits zusammenzustellen und darüber hinaus
* eine Möglichkeit Kleidungsstücke daraufhin zu prüfen, für welchen Nutzungskontext sie zu gebrauchen sind.
* Fest definierte Modelle, die sich jeweils auf die Bekleidung von bestimmten Bereichen des Körpers beziehen,
* enthalten hierbei Restriktionen, die sich auf die verwendete Art von Stoff beziehen.
* Die einzelnen Arten von Stoffen besitzen passend dazu jeweils eine Datenstruktur, der verschiedene Eigenschaften des Stoffes festhält.
*
* Darüber hinaus halten die Modelle fest, für welchen Nutzungskontext ein bestimmtes Kleidungsstück sinnvoll ist.
*
* Anhand der festegehaltenen Eigenschaften und den Einschränkungen innerhalb des Modells können durch Vergleichsoperationen sinnvolle Outfits zusammengestellt
* werden.
*
* @param {String} context - Definiert den gewünschen Nutzungskontext eines Outfits.
* @param {Object} clothing - Enhält die Kleidungsstücke, die bei der Berechnung berücksichtigt werden sollen.
* @param (Boolean) single - Unterscheidet zwischen Outfitsuche und Zuordnung von einem Kleidungsstück zu Nutzungskontexten
*/



const calcOutfit = function(context, clothing, single) {

/* Models enthalten Informationen darüber, welche Merkmale ein Kleidungsstück erfüllen muss, um für den angegebenen Kontext gültig zu sein.
*  Dabei werden jeweils ein "high" und ein "low"-Wert für verschiedene Eigenschaften des Stoffes, der verwendet wird, eingetragen. Stoffe, die innerhalb des Bereichs der Definition liegen, werden
*  bei der späteren Berechnung von Outfits als gültige Stoffarten registriert. Ebenfalls die Art der Kleidung, die gültig sein soll, wird festgehalten, da für einen bestimmten Kontext nur bestimmte Arten von Kleidungsstücken zulässig sind.
*  Einige Schichten besitzen zudem eine Alternative, deren Restriktionen etwas weniger streng sind und zum Einsatz kommen, falls kein passendes Kleidungsstück gefunden werden konnte.
*/

var wintermodel = {
        wintermodel_head: {
          respiratory_activity_low: 0,
          respiratory_activity_high: 0,
          warmth_low: 7,
          warmth_high: 10,
          moisture_pickup_low: 0,
          moisture_pickup_high: 0,
          art: ["Wollmütze","Mütze","Beanie"],
          model: "wintermodel_head"
        },
        wintermodel_layer1: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["T-Shirt", "Shirt","Hemd","Bluse"],
            model: "wintermodel_1layer"
        },
        wintermodel_layer1_alternative: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["T-Shirt", "Shirt","Hemd","Bluse"],
            model: "wintermodel_1layer_alternative"
        },
        wintermodel_layer2: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 4,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Pullover","Hoodie","Sweatshirt","Weste","Strickjacke","Blazer"],
            model: "wintermodel_2layer"
        },
        wintermodel_layer2_alternative: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Pullover","Hoodie","Sweatshirt","Weste","Strickjacke","Blazer"],
            model: "wintermodel_1layer_alternative"
        },
        wintermodel_layer3: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 8,
            warmth_high: 10,
            moisture_pickup_low: 0,
            moisture_pickup_high: 5,
            art: ["Wintermantel","Mantel","Jacke"],
            model: "wintermodel_3layer"
        },
        wintermodel_layer3_alternative: {
            respiratory_activity_low: 4,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 5,
            moisture_pickup_high: 10,
            art: ["Wintermantel","Mantel","Jacke"],
            model: "wintermodel_3layer_alternative"
        },
        wintermodel_bottom: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Jeans","Hose","Leggings"],
            model: "wintermodel_bottom"
        },
        wintermodel_shoes: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Stiefel","Schuhe","Segelschuhe","Outdoorschuhe"],
            model: "wintermodel_shoes"
        }

    }

    /* Die folgende Definition dient als Container für das fertige Outfit. Diese Unterscheiden sich bei den meisten Modellen. Ein Winteroutfit bietet beispielsweise Platz für drei Schichten der Oberkörperbekleidung, bei einem Sommeroutfit ist es lediglich eine Schicht.*/
    var winteroutfit =    {
            model: "wintermodel",
            head: 0,
            layer1: 0,
            layer2: 0,
            layer3: 0,
            bottom: 0,
            shoes: 0,
            layers: ["head", "layer1", "layer2", "layer3", "bottom", "shoes"]
            }



    /* Die Datenstruktur characs ist wesentlicher Bestandteil bei der Auswahl von Kleidungsstücken. Sie repräsentiert die verschiedenen Stoffarten und ihre Eigenschaften. */
    var charac = [{
            name: "Baumwolle",
            warmth: 6,
            moisture_pickup: 2,
            respiratory_activity: 10
        },
        {
            name: "Wolle",
            warmth: 9,
            moisture_pickup: 4,
            respiratory_activity: 9
        },
        {
            name: "Viskose",
            warmth: 6,
            moisture_pickup: 3,
            respiratory_activity: 9
        },
        {
            name: "Modal",
            warmth: 6,
            moisture_pickup: 3,
            respiratory_activity: 9
        },
        {
            name: "Polyester",
            warmth: 9,
            moisture_pickup: 9,
            respiratory_activity: 7
        },
        {
            name: "Leinen",
            warmth: 4,
            moisture_pickup: 1,
            respiratory_activity: 10
        },
        {
            name: "Polyacryl",
            warmth: 7,
            moisture_pickup: 8,
            respiratory_activity: 8
        },
        {
            name: "Polyamid",
            warmth: 7,
            moisture_pickup: 8,
            respiratory_activity: 4
        },
        {
            name: "Seide",
            warmth: 3,
            moisture_pickup: 7,
            respiratory_activity: 9
        },
        {
            name: "Kaschmir",
            warmth: 10,
            moisture_pickup: 8,
            respiratory_activity: 8
        },
        {
            name: "Denim",
            warmth: 7,
            moisture_pickup: 4,
            respiratory_activity: 8
        },
        {
            name: "Leder",
            warmth: 8,
            moisture_pickup: 9,
            respiratory_activity: 8
        },
        {
            name: "Synthetik",
            warmth: 6,
            moisture_pickup: 10,
            respiratory_activity: 2
        }

    ];

    var models = [wintermodel];
    var fits;
    var res;
    if (single) {
        // Durchlaufe alle Modelle
        for (var single_model in models) {
            // Durchlaufe alle Schichten der einzelnen Modelle
            for (var single_layer in models[single_model]) {

                // Berechne für das jeweilige Kleidungsstück, ob die jeweilige Schicht gültig ist.
                res = calcLayerClothing(models[single_model][single_layer], clothing, charac, single);
                if (res != null) {
                    fits=res;
                }
            }
        }
      return fits;
    }

    // Wenn nach Outfits gesucht wird und der Anwendungszweck Winter ist
    if (!single && context== "winter") {
      // Berechne passende Kleidungsstücke für einen bestimmten Layer eines Modells
      winteroutfit.head = calcLayerClothing(wintermodel["wintermodel_head"], clothing, charac, false);
      // Wenn kein Kleidungsstück gefunden wurde
      if (winteroutfit.head == 0) {
          console.log("There is no clothing for head winter!");
      }

      winteroutfit.layer1 = calcLayerClothing(wintermodel["wintermodel_layer1"], clothing, charac, false);
            // Wenn kein Kleidungsstück gefunden wurde
      if (winteroutfit.layer1 == 0) {
          // Suche erneut mit Alternativem Modell für die jeweilige Schicht
          winteroutfit.layer1 = calcLayerClothing(wintermodel["wintermodel_layer1_alternative"], clothing, charac, false);
          if (winteroutfit.layer1 == 0) {
              console.log("There is no clothing for layer 1 winter!");
          }
      }
      winteroutfit.layer2 = calcLayerClothing(wintermodel["wintermodel_layer2"], clothing, charac, false);
      if (winteroutfit.layer2 == 0) {
          winteroutfit.layer2 = calcLayerClothing(wintermodel["wintermodel_layer2_alternative"], clothing, charac, false);
          if (winteroutfit.layer2 == 0) {
              console.log("There is no clothing for layer 3 winter!");
          }
      }
      winteroutfit.layer3 = calcLayerClothing(wintermodel["wintermodel_layer3"], clothing, charac, false);
      if (winteroutfit.layer3 == 0) {
          winteroutfit.layer3 = calcLayerClothing(wintermodel["wintermodel_layer3_alternative"], clothing, charac, false);
          if (winteroutfit.layer3 == 0) {
              console.log("There is no clothing for layer 3 winter!");
          }
      }
      winteroutfit.bottom = calcLayerClothing(wintermodel["wintermodel_bottom"], clothing, charac, false);
      if (winteroutfit.bottom == 0) {
          console.log("There is no clothing for bottom winter!");
      }
      winteroutfit.shoes = calcLayerClothing(wintermodel["wintermodel_shoes"], clothing, charac, false);
      if (winteroutfit.shoes == 0) {

          console.log("There is no clothing for shoes winter!");
      }
          // Gib das komplette Outfit zurück
          return winteroutfit;
    }

    function calcLayerClothing(model, clothing, charac, single) {
    var result = [];

    // Durchlauf der einzelnen Stoffarten
    for (var single_charac in charac) {
      // Überprüfung ob Eigenschaften der jeweiligen Stoffart die Bedingungen des Modells erfüllen
            if ((model.respiratory_activity_low <= charac[single_charac].respiratory_activity && model.respiratory_activity_high >= charac[single_charac].respiratory_activity) || (model.respiratory_activity_high == 0 && model.respiratory_activity_low == 0)) {
                if ((model.warmth_low <= charac[single_charac].warmth && model.warmth_high >= charac[single_charac].warmth) || (model.warmth_high == 0 && model.warmth_low == 0)) {
                    if ((model.moisture_pickup_low <= charac[single_charac].moisture_pickup && model.moisture_pickup_high >= charac[single_charac].moisture_pickup) || (model.moisture_pickup_high == 0 && model.moisture_pickup_low == 0)) {
                        // Durchlaufe Kleidungsarten, die für ein bestimmtes Modell gültig sind
                        for (var art in model.art) {
                            // Sollte das Flag single gesetzt sein liefert die Funktion passende Modells für ein bestimmtes Kleidungsstück
                            // Wenn die Art und der Stoff des Kleigunsstücks für das gewählte Modell gültig sind
                            if (single && clothing.art == model.art[art] && clothing.fabric == charac[single_charac].name) {
                                // Speichert die ID des Kleidungsstücks und die Modelle für das es gültig ist
                                result.push({
                                    id: clothing.id,
                                    model: model.model
                                });
                            }
                        }
                        // Wenn single nicht gesetzt werden Kleidungsstücke ermittelt, die für einen bestimmten Anwendungszweck geeignet sind.
                        if (!single) {
                            // Durchlaufe die verfügbaren Kleidungsstücke
                            for (var single_clothing in clothing) {
                                // Durchlaufe die gültigen Kleidungsarten
                                for (var art in model.art) {
                                    // Wenn die Art und der Stoff des Kleidungsstück passend ist
                                    if (clothing[single_clothing].art == model.art[art] && clothing[single_clothing].fabric == charac[single_charac].name) {
                                        // Füge passende Kleidungsstücke "result" hinzu
                                        result.push(clothing[single_clothing].id);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
            // Sende das Ergebniss der jeweiligen Operation
            return result;
        }
    };

    module.exports = calcOutfit;
