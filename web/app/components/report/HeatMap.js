import React  from 'react';
import Base   from '../Base';
import AmCharts from '@amcharts/amcharts3-react';

class HeatMap extends Base {
  constructor(props) {
    super(props);   
  
    this.state = {
      references: [],
      graphs: [],
      sourceData: []
    }
  } 
  
 componentWillReceiveProps() {
    //console.log(this.props.report)
    var ref = [];
    for(i in this.props.report){
      ref.push(this.props.report[i].reference)
    }
    var refs = ref.filter(function(elem, index, self) {
        return index == self.indexOf(elem);
    });
    var comp = [];
    for(i in this.props.report){
      comp.push(this.props.report[i].comparing)
    }    
   
    var count = {};
    for(var i = 0; i < ref.length; i++){
      if(!(ref[i] in count))count[ref[i]] = 0;
        count[ref[i]]++;
    }
    console.log(count);
   
   
    var sourceData = [];
    for (var i = 0; i < refs.length; i++) {
      var dataPoint = {
        reference: refs[i] 
      }

      // generate value for each ref
      for (var h = 0; h <= refs.length; h++) {
        dataPoint['value' + h] = Math.round(this.props.report[i].similarity  * 100) / 100
      }

      sourceData.push(dataPoint);
    }

    var colors = ['#b52929', '#9d3226', '#974c29', '#915c2c', '#8a6730', '#837134', '#7b7a38','#73833c', '#698b41', '#5d9145', '#3c763d'];
    for (i in sourceData) {
      for (var h = 0; h <= refs.length; h++) {
        console.log(colors[Math.floor(sourceData[i]['value' + h]/10)]);
        sourceData[i]['color' + h] = colors[Math.floor((Math.random() * 11) + 1)-1];//Math.floor(sourceData[i]['value' + h]/10)];
        sourceData[i]['hour' + h] = 1;
      }
    }
    
    var graphs = [];
    for (var h = 0; h <= refs.length; h++) {
      graphs.push({
        "balloonText": "Similarity percentaje: [[value" + h + "]]%",
        "fillAlphas": 1,
        "lineAlpha": 0,
        "type": "column",
        "colorField": "color" + h,
        "valueField": "hour" + h
      });
    } 
    
    this.setState({
      references: refs,
      graphs: graphs,
      sourceData: sourceData
    });
  }
  
  render() {
      return (
          React.createElement(AmCharts.React, {
            style: {
              width: "100%",
              height: "500px"
            },
            options: {
              "dataProvider": this.state.sourceData,
              "type": "serial",
              "valueAxes": [{
                "stackType": "regular",
                "axisAlpha": 0.3,
                "gridAlpha": 0,
                "maximum": 15
              }],
              "graphs": this.state.graphs,
              "columnWidth": 1,
              "categoryField": "reference",
              "categoryAxis": {
                "gridPosition": "start",
                "axisAlpha": 0,
                "gridAlpha": 0,
                "position": "left"
              }
            }
          })
      );
  }
}

export default HeatMap;