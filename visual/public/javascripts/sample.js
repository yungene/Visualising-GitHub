const fs = require('fs');
const d3 = require('d3-node')().d3;
const d3nBar = require('d3node-barchart');
//const d3nBar = require('../');


var sample = function (csvString) {
  const data = d3.csvParse(csvString);
  return d3nBar({ data: data }).svgString();

};


module.exports = {
  sample: sample
};
