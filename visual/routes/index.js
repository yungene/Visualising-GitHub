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
  readTeamSizePoints("CS3012-SWENG","yungene", function (csvArr){
  console.log("Read DB success\n")
  console.log("CSVARR")
  console.log(csvArr);
  res.render('index', { title: 'Express',
    fixtureData: fixtureData, 
    barChartHelper: barChartHelper,
    sample: sample,
    csvString: csvString,
    line: line,
    repoName: "Default",
    csvArr: csvArr});
  });
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
  res.redirect('/');
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
});

router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express',
    fixtureData: fixtureData, 
    barChartHelper: barChartHelper,
    sample: sample,
    csvString: csvString,
    line: line,
    repoName: "Default"});
});

// Returns an array of size 2, array[0] is CSV string for active_team_size vs time points,  array[1] is CSV string for date vs release_name points
function readTeamSizePoints( stringRepoName, stringRepoOwner, callBackFun){
  let stringQuery1 = "SELECT date, team_size FROM active_team_size_vs_time WHERE repo_name=\'{0}\' AND repo_owner=\'{1}\' ORDER BY date ASC\n".format(stringRepoName,stringRepoOwner);
  console.log(stringQuery1);
  let stringQuery2 = "SELECT date, release_name FROM release_table WHERE repo_name=\'{0}\' AND repo_owner=\'{1}\' ORDER BY date ASC\n".format(stringRepoName,stringRepoOwner);
  console.log(stringQuery2);
  var stringCSV1 = "key,value\n";
  var stringCSV2 = "key,value\n";
  db.query(stringQuery1, (err1, result1) => {
    if (err1) {
      console.log(err1);
      res.redirect('/error');
    }
    db.query(stringQuery2, (err2, result2) => {
      if (err2) {
        console.log(err2);
        res.redirect('/error');
      }
      for (var i = 0; i < result1.length; i++) {
       var row = result1[i];
       //console.log("row out ");
      // console.log(row);
       stringCSV1 = stringCSV1.concat("{0},{1}\n".format(row.date,row.team_size));
     };
     //console.log(result1);
     //console.log(stringCSV1);

     for (var i = 0; i < result2.length; i++) {
       var row = result2[i];
       stringCSV1 = stringCSV1.concat("{0},{1}\n".format(row.date,row.release_name));
     };

     callBackFun( [stringCSV1,stringCSV2]);
   });
  });
};

module.exports = router;
