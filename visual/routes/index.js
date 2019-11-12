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
  						repoName: "Default"});
});

const { Parser } = require('json2csv');
const fields = ['key', 'value'];
const json2csvParser = new Parser({ fields });

String.prototype.format = function() {
    var formatted = this;
    for (var i = 0; i < arguments.length; i++) {
        var regexp = new RegExp('\\{'+i+'\\}', 'gi');
        formatted = formatted.replace(regexp, arguments[i]);
    }
    return formatted;
};

router.get('/:repoName', function(req, res, next) {
  // res.render('index', { title: 'Express',
  // 						fixtureData: fixtureData, 
  // 						barChartHelper: barChartHelper,
  // 						sample: sample,
  // 						csvString: csvString,
  // 						line: line,
  // 						repoName: req.params.repoName});
  let query = "SELECT time_val, commit_size FROM commit_size_vs_time ORDER BY time_val ASC;"; // query database to get all the players

        // execute query
        db.query(query, (err, result) => {
            if (err) {
            	console.log(err);
                res.redirect('/');
            }

            var csvString = "key,value\n";
            for (var i = 0; i < result.length; i++) {
    			     var row = result[i];
    			     csvString = csvString.concat("{0},{1}\n".format(row.time_val,row.commit_size))
			       }
            console.log(result);
            console.log(csvString);
            console.log(json2csvParser.parse(result));
            res.render('index', { title: 'Express',
  						fixtureData: fixtureData, 
  						barChartHelper: barChartHelper,
  						sample: sample,
  						csvString: csvString,
  						line: line,
  						repoName: "Default"});
        });
});

router.post("/test/submit", function(req, res, next) {
	repoName = req.body.repoName;
	res.redirect('/' + repoName);
})

module.exports = router;
