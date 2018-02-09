import AmCharts from '@amcharts/amcharts3-react';
function identityColor(identity)
  {
    var colors = ['#FFFFFF', '#FF0000', '#FF9100', '#F2FF00', '#9DFF00', '#00FF00'];
    // WHITE
    var color = colors[0];
     

    // RED 
    if ( identity != -1 && identity <= 8 )
    {
      color = colors[1];
    }
    // ORANGE 
    if ( identity > 8 && identity <= 15  )
    {
      color = colors[2];
    }
    // YELLOW
    if ( identity > 15 && identity <= 23 )
    {
      color = colors[3];
    }
    // GREEN
    if ( identity > 23 && identity <= 31 )
    {
      color = colors[4];
    }
    // GREEN MATE
    if ( identity > 31)
    {
      color = colors[5];
    }

    return color;
  }

function repeatColor(repeat)
{
  var colors = ['#FFFFFF', '#FF0000', '#FF9100', '#F2FF00', '#9DFF00', '#00FF00'];
  // WHITE
  var color = colors[0];
   

  // RED 
  if ( repeat ==  0 )
  {
    color = colors[1];
  }
  // ORANGE 
  if ( repeat > 0 && repeat <= 2  )
  {
    color = colors[2];
  }
  // YELLOW
  if ( repeat > 2 && repeat <= 6 )
  {
    color = colors[3];
  }
  // GREEN
  if ( repeat > 6 && repeat <= 15 )
  {
    color = colors[4];
  }
  // GREEN MATE
  if ( repeat > 15)
  {
    color = colors[5];
  }

  return color;
}

  function contIdentity(obj) {
   var count=0;
   for(var prop in obj) {
      if (prop.indexOf('Identity') != -1 )
      {
         ++count;
      }
   }
   return count;
}
function contRepeat(obj) {
   var count=0;
   for(var prop in obj) {
      if (prop.indexOf('Repeat') != -1 )
      {
         ++count;
      }
   }
   return count;
}
function deleteDups(array){
  var unique = [];
  $.each(array, function(i, el){
      if($.inArray(el, unique) === -1) unique.push(el);
  });
   return unique;
}


function getReferences(data){

  var regExp = new RegExp(('^>.*'),"gim");
  var matches = null;
  var i = 0;
  var sequences = [];
  while (matches = regExp.exec( data )){
    sequences[i] = matches[0];
    i++;
  }
  return sequences;
}


function createIdentityDataChart(CSVElements, charData, graphs, references, comparingName){
  
  var data = [];
  var j = 0;
  var CSVReferences = [];
  var uniqueCSVReferences = [];
  var uniqueReferences = [];
  var cont = 0;
  
  if ( charData.length == 0 )
  {
    for ( var i = 0; i < references.length; i++)
    {
      var reference = references[i].slice(1, 14)+'...';
      charData[i] = {Reference: reference}
    }
  }

  for( var k = 0; k < charData.length; k++)
  {
    var maxIdentity = 0;
    for (var l = 1; l < CSVElements.length-1; l++)
    {
        var CSVReference = CSVElements[l][1].slice(0, 13)+"...";
        if ( CSVReference == charData[k].Reference )
        {
          if ( CSVElements[l][2] > maxIdentity)
          {
            maxIdentity = CSVElements[l][2];
          }
        }
    }
    var number = contIdentity(charData[k]);
    charData[k]['Identity'+ number]=maxIdentity;
    var color = identityColor(maxIdentity);
    charData[k]['Color' + number] = color;
    charData[k]['Comparing' + number] = 1;
    charData[k]['ComparingName' + number] = comparingName;
   
  }// end of for Reference 

 
  var graphs = [];
  for (var i = 0; i <= charData.length; i++) {
    graphs.push({
      "balloonText": "<b>Similarity: [[Identity" + i + "]]% </b></br>Analysis: [[ComparingName" + i + "]] </br> Reference protein: [[Reference]]",
      "fillAlphas": 1,
      "lineAlpha": 0.1,
      "type": "column",
      "colorField": "Color" + i,
      "valueField": "Comparing" + i
    });
  }

  data = {
    sourceData: charData,
    graphs: graphs,
    references: references
  };
  
  return data
}



function createRepeatChart(CSVElements, charData, graphs, references, comparingName){
  var data = [];
  var j = 0;
  var CSVReferences = [];
  var uniqueCSVReferences = [];
  var uniqueReferences = [];
  var cont = 0;
  if ( charData.length == 0 )
  {
    for ( var i = 0; i < references.length; i++)
    {
      var reference = references[i].slice(1, 14)+'...';
      charData[i] = {Reference: reference}
    }
  }

  for( var k = 0; k < charData.length; k++)
  {
    var cont = 0;
    for (var l = 1; l < CSVElements.length-1; l++)
    {
        var CSVReference = CSVElements[l][1].slice(0, 13)+"...";
        if ( CSVReference == charData[k].Reference )
        {
          cont++;
        }
    }
    var number = contRepeat(charData[k]);
    charData[k]['Repeat'+ number]=cont;
    var color = repeatColor(cont);
    charData[k]['Color' + number] = color;
    charData[k]['Comparing' + number] = 1;
    charData[k]['ComparingName' + number] = comparingName;
   
  }// end of for Reference 

 
  var graphs = [];
  for (var i = 0; i <= charData.length; i++) {
    graphs.push({
      "balloonText": "<b>Matches: [[Repeat" + i + "]] </b></br>Analysis: [[ComparingName" + i + "]] </br> Reference protein: [[Reference]]",
      "fillAlphas": 1,
      "lineAlpha": 0.1,
      "type": "column",
      "colorField": "Color" + i,
      "valueField": "Comparing" + i
    });
  }

  data = {
    sourceData: charData,
    graphs: graphs,
    references: references
  };
  
  return data;

}

export { createIdentityDataChart, getReferences, createRepeatChart };