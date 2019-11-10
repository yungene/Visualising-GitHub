var express = require('express');
var app = express();
var router = express.Router();

var fixtureData = require('../public/sample_data/fixture_data.json');
var barChartHelper = require('../public/javascripts/bar_chart_helper');

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express',
  						fixtureData: fixtureData, 
  						barChartHelper: barChartHelper});
});

module.exports = router;
