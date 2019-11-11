const fs = require('fs');
const output = require('d3node-output');
const d3 = require('d3-node')().d3;
const d3nLine = require('d3node-linechart');

var getLine = function (csvString) {

  const data = d3.csvParse(csvString);
  return d3nLine({ data: data }).svgString();

};

module.exports = {
  getLine: getLine
};