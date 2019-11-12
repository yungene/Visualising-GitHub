const fs = require('fs');
const output = require('d3node-output');
const d3 = require('d3-node')().d3;
//const d3nLine = require('d3node-linechart');
const d3nLine = require('../javascripts/d3node-linechart');


var getLine = function (csvString) {

	const data = d3.csvParse(csvString,d3.autoType);
	console.log(data);
	return d3nLine({ data: data,
		isCurve: false,
		width: 1200,
		height: 700,
		margin: { top: 20, right: 40, bottom: 60, left: 60 }}).svgString();

};

module.exports = {
	getLine: getLine
};