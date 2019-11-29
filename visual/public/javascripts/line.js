const fs = require('fs');
const output = require('d3node-output');
const d3 = require('d3-node')().d3;
//const d3nLine = require('d3node-linechart');
const d3nLine = require('../javascripts/d3node-linechart');
const d3nDateLine = require('../javascripts/d3node-dateLineChar');

var getLine = function(csvString) {

	const data = d3.csvParse(csvString, d3.autoType);
	console.log(data);
	return d3nLine({
		data: data,
		isCurve: false,
		width: 1200,
		height: 700,
		margin: {
			top: 20,
			right: 40,
			bottom: 60,
			left: 60
		}
	}).svgString();

};

// Build a line with date on X-axis
var getDateLine = function(csvArr) {
	const dataDateVSSize = d3.csvParse(csvArr[0], function(d) {
		return {
			key: new Date(d.key), // lowercase and convert "Year" to Date
			value: +d.value // lowercase and convert "Length" to number
		};
	});
	const dataDateVSRelease = d3.csvParse(csvArr[1], function(d) {
		return {
			key: new Date(d.key), // lowercase and convert "Year" to Date
			value: d.value // lowercase and convert "Length" to number
		};
	});
	//console.log(dataDateVSSize);
	console.log(dataDateVSRelease);
	return d3nDateLine({
		data: dataDateVSSize,
		dateData: dataDateVSRelease,
		isCurve: true,
		width: 1200,
		height: 700,
		margin: {
			top: 80,
			right: 40,
			bottom: 60,
			left: 60
		}
	}).svgString();

};

module.exports = {
	getLine: getLine,
	getDateLine: getDateLine
};