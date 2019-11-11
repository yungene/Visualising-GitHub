var express = require('express');
var app = express();
var router = express.Router();
const fs = require('fs');


var fixtureData = require('../public/sample_data/fixture_data.json');
var barChartHelper = require('../public/javascripts/bar_chart_helper');
var sample = require('../public/javascripts/sample');
var line = require('../public/javascripts/line');
var repoName = "CS3012"

const path = require("path");
const csvString = fs.readFileSync(path.resolve(__dirname,'../public/sample_data/data.csv')).toString();
/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express',
  						fixtureData: fixtureData, 
  						barChartHelper: barChartHelper,
  						sample: sample,
  						csvString: csvString,
  						line: line,
  						repoName: "DEfault"});
});

router.get('/:repoName', function(req, res, next) {
  res.render('index', { title: 'Express',
  						fixtureData: fixtureData, 
  						barChartHelper: barChartHelper,
  						sample: sample,
  						csvString: csvString,
  						line: line,
  						repoName: req.params.repoName});
});

router.post("/test/submit", function(req, res, next) {
	repoName = req.body.repoName;
	res.redirect('/' + repoName);
})

module.exports = router;
