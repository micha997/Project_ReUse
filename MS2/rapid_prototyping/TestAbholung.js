var async = require('async');

var Object1 = 
	{
		userID: 123,
		Koordinaten: 10,
		ZeitVon: 1800,
		ZeitBis: 1900,
		Infos: "affe"
	};

var Object2 = 
	{
		userID: 123,
		Koordinaten: 10,
		ZeitVon: 1800,
		ZeitBis: 1900,
		Infos: "stein"
	};
	
var Object3 = 
	{
		userID: 173,
		Koordinaten: 14,
		ZeitVon: 1400,
		ZeitBis: 1600,
		Infos: "Giraffe"
	};
	
var Object4 = 
	{
		userID: 563,
		Koordinaten: 20,
		ZeitVon: 1300,
		ZeitBis: 1500,
		Infos: "asu"
	};
	
var Object5 = 
	{
		userID: 675,
		Koordinaten: 17,
		ZeitVon: 1200,
		ZeitBis: 1500,
		Infos: "Krause"
	};

var Object6 = 
	{
		userID: 103,
		Koordinaten: 19,
		ZeitVon: 1800,
		ZeitBis: 1900,
		Infos: "Giraffe"
	};	
	
var yourKoordinate = 8;
var dirtyArray = [Object1,Object2,Object3,Object4,Object5,Object6];

async.waterfall([
			function(callback){
				var cleanArray = [];
				//Objekte mit der selben UserID werden hier zusammmengefuegt
				while(dirtyArray.length>0){
					var tmpUserID = dirtyArray[0].userID;
					var newArray = [];
					newArray.push(dirtyArray[0]);
					dirtyArray.splice(0,1);
					for(var k = 0;dirtyArray.length>k;k++){
						if(tmpUserID == dirtyArray[k].userID){
							newArray.push(dirtyArray[k]);
							dirtyArray.splice(k,1);
							k--;
						}
					}
					var newObject = combineObjects(newArray);
					cleanArray.push(newObject);
				}
				callback(null,cleanArray);
			},
			function(cleanArray, callback){
				var NewArray = [];
				var index = 0;
				//Hier werden die Objekte nach ihrer Uhrzeit und Dauer sortiert
				while(cleanArray.length>0){
					var earliestTime = 10000;
					var shortestPeriod = 10000;
					for(var i = 0;cleanArray.length>i;i++){
						var tmpEarliest = cleanArray[i].ZeitVon;
						var tmpPeriod = cleanArray[i].ZeitBis - cleanArray[i].ZeitVon;
						if((earliestTime > tmpEarliest) || (shortestPeriod > tmpPeriod) && (earliestTime > tmpEarliest)){
							earliestTime = tmpEarliest;
							shortestPeriod = tmpPeriod;
							index = i;
						}
					}
					NewArray.push(cleanArray[index]);
					cleanArray.splice(index,1);
				}
				callback(null,NewArray);
			},
			function(cleanArray, callback){
				var wayArray = [];
				//Hier werden die Objekte sortiert nach Distaz sortiert, falls aneinanderliegende Objekte die selbe Uhrzeit und Dauer haben
				while(cleanArray.length>0){
					var cleanArrayCpy = [];
					cleanArrayCpy = cleanArrayCpy.concat(cleanArray);
					var tmpArray = [];
					tmpArray.push(cleanArrayCpy[0]);
					cleanArrayCpy.splice(0,1);
					for(var i = 0;cleanArrayCpy.length>i;i++){
						if(tmpArray[0].ZeitVon == cleanArrayCpy[i].ZeitVon && Math.abs(tmpArray[0].ZeitBis - cleanArrayCpy[i].ZeitBis) <= 100 ){
							tmpArray.push(cleanArrayCpy[i]);
							cleanArrayCpy.splice(i,1);
							i--;
						}
					}
					if(tmpArray.length > 1){
						var nIndex = getShortestWay(yourKoordinate,tmpArray);
						yourKoordinate = cleanArray[nIndex].Koordinaten;
						wayArray.push(cleanArray[nIndex]);
						cleanArray.splice(nIndex,1);
					}else{
						yourKoordinate = cleanArray[0].Koordinaten;
						wayArray.push(cleanArray[0]);
						cleanArray.splice(0,1);
					}
				}
				callback(null,wayArray);
			},
			function(wayArray, callback){
			//Hier werden die Termine zur Abholung festgelegt (Richtige Distanzen + benötigte Zeit von Google Maps sollten hier genutzt werden)
				for(var i = 0;wayArray.length-1>i;i++){
					if(wayArray[i].ZeitVon == wayArray[i+1].ZeitVon && wayArray[i].ZeitBis == wayArray[i+1].ZeitBis){
						wayArray[i].Termin = wayArray[i].ZeitVon + 20;
						wayArray[i+1].Termin = wayArray[i+1].ZeitBis - 20;
					}else if((wayArray[i+1].ZeitVon - wayArray[i].ZeitBis)>0){
						wayArray[i].Termin = wayArray[i].ZeitBis - 20;
						wayArray[i+1].Termin = wayArray[i+1].ZeitBis;
					}else if(wayArray[i].ZeitVon != wayArray[i+1].ZeitVon || wayArray[i].ZeitBis != wayArray[i+1].ZeitBis){
						wayArray[i].Termin = wayArray[i].ZeitBis == wayArray[i+1].ZeitBis ? 
							wayArray[i].ZeitBis - 100 : wayArray[i].ZeitBis - 20;
						wayArray[i+1].Termin = wayArray[i].ZeitBis == wayArray[i+1].ZeitBis ?
							wayArray[i+1].ZeitBis - 20 : wayArray[i+1].ZeitBis - (wayArray[i+1].ZeitBis - wayArray[i].ZeitBis) + 20;
					}
				}
				callback(null,wayArray);
			}
			],function(err, NewArray){
				yourKoordinate = 8;
				for(var m = 0;NewArray.length>m;m++){
					console.log(m + ". Termin:");
					console.log("Von:" + NewArray[m].ZeitVon);
					console.log("Bis:" + NewArray[m].ZeitBis);
					console.log("Uhrzeit:" + NewArray[m].Termin);
					console.log("Distanz:" + Math.abs(NewArray[m].Koordinaten - yourKoordinate));
					yourKoordinate = NewArray[m].Koordinaten;
			}
		});

//Hier sind die Funktionen seperat nochmal aufgefuehrt
		
//Fuegt Items mit der selben UserID zusammen
function cleanUP(dirtyArray){
	var cleanArray = [];
	while(dirtyArray.length>0){
		var tmpUserID = dirtyArray[0].userID;
		var newArray = [];
		newArray.push(tmpUserID);
		dirtyArray.splice(0,1);
		for(var k = 0;dirtyArray.length>k;k++){
			if(tmpUserID == dirtyArray[k].userID){
				newArray.push(dirtyArray[k]);
				dirtyArray.splice(k,1);
			}
		}
		var newObject = combineObjects(newArray);
		cleanArray.push(newObject);
	}
	return cleanArray;
}

//Funktion zum zusammenfuegen von mehreren Items
function combineObjects(combArray){
	if(combArray.length == 1){
		var newObject = 
			{
				userID: combArray[0].userID,
				Koordinaten: combArray[0].Koordinaten,
				ZeitVon: combArray[0].ZeitVon,
				ZeitBis: combArray[0].ZeitBis,
				Objekte:
					[
						{
							Infos: "Klasse"
						},
						{
							Infos: "Hase"
						}
					]
			};
		return newObject;
	}else if(combArray.length > 1){
		var newObject = 
			{
				userID: combArray[0].userID,
				Koordinaten: combArray[0].Koordinaten,
				ZeitVon: combArray[0].ZeitVon,
				ZeitBis: combArray[0].ZeitBis,
				Objekte: []
			};
		for(var i = 0;combArray.length>i;i++){
			newObject.Objekte.push(combArray[i].Infos);
		}
		return newObject;
	}
}

//Sortierung nach den Uhrzeiten
function sortTimes(ArrayTimes){
	var NewArray = [];
	var earliestTime = null;
	var shortestPeriod = null;
	var index = 0;
	while(ArrayTimes.length>0){
		for(var i = 0;ArrayTimes.length>i;i++){
			var tmpEarliest = ArrayTimes[i].ZeitVon;
			var tmpPeriod = ArrayTimes[i].ZeitBis - ArrayTimes[i].ZeitVon;
			if((earliestTime == null || earliestTime > tmpEarliest) && (shortestPeriod == null || shortestPeriod > tmpPeriod)){
				earliestTime = tmpEarliest;
				shortestPeriod = tmpPeriod;
				index = i;
			}
		}
		NewArray.push(ArrayTimes[index]);
		ArrayTimes.splice(index,1);
	}
	return NewArray;
}

function setDates(ArrayDates){
	var wayArray = [];
	while(ArrayDates.length>0){
		var tmpArray = [];
		tmpArray.push(ArrayDates[0]);
		ArrayDates.splice(0,1);
		for(var i = 0;ArrayDates.length>i;i++){
			if(tmpArray[0].ZeitVon == ArrayDates[i].ZeitVon && tmpArray[0].ZeitBis == ArrayDates[i].ZeitBis){
				tmpArray.push(ArrayDates[i]);
				ArrayDates.splice(i,1);
			}
		}
		if(tmpArray.length > 1){
			var shortArray = getShortestWay(yourKoordinate,tmpArray);
			wayArray = wayArray.concat(shortArray);
		}else{
			wayArray = wayArray.concat(tmpArray);
		}
	}
	return wayArray;
}

function getShortestWay(startKoordinate,KoordinatenArray){
	var shortestDistance = null;
	var index = 0;
	for(var i=0;KoordinatenArray.length>i;i++){
		var tmpDistance = Math.abs(KoordinatenArray[i].Koordinaten - startKoordinate);
		if(shortestDistance == null || shortestDistance > tmpDistance){
			shortestDistance = tmpDistance;
			index = i;
		}
	}
	return index;
}