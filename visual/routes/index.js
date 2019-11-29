var express = require('express');
var app = express();
var router = express.Router();
const fs = require('fs');
const d3 = require('d3-node')().d3;


var fixtureData = require('../public/sample_data/fixture_data.json');
var barChartHelper = require('../public/javascripts/bar_chart_helper');
var sample = require('../public/javascripts/sample');
var line = require('../public/javascripts/line');
var repoName = "CS3012"

const path = require("path");
/* GET home page. */
router.get('/', function(req, res, next) {
  var splitArr = [];
  if (req.app.locals.dropdownVals.length == 0) {
    splitArr = ["no-input", "no-input",-1,-1];
  } else {
    splitArr = req.app.locals.dropdownVals[0].split(",");
  }
  readTeamSizePoints(splitArr[1], splitArr[0],splitArr[2],splitArr[3], function(csvArr) {
    console.log("Read DB success\n")
    //console.log("CSVARR")
    //console.log(csvArr);
    const dataDateVSSize = d3.csvParse(csvArr[0], function(d) {
      return {
        key: new Date(d.key), // lowercase and convert "Year" to Date
        value: +d.value, // lowercase and convert "Length" to number
        backfill: +d.backfill,
        threshold: +d.threshold
      };
    });
    var backfill= "unknown";
    if(dataDateVSSize[0]){
      backfill = dataDateVSSize[0].backfill;
    }

    res.render('index', {
      title: 'J.C. Demo',
      repoName: splitArr[1],
      repoOwner: splitArr[0],
      dataFromNode: csvArr[0],
      dateDataFromNode: csvArr[1],
      dropdownVals: req.app.locals.dropdownVals,
      backfill: splitArr[2],
      threshold: splitArr[3]
    });
  });
});

String.prototype.format = function() {
  var formatted = this;
  for (var i = 0; i < arguments.length; i++) {
    var regexp = new RegExp('\\{' + i + '\\}', 'gi');
    formatted = formatted.replace(regexp, arguments[i]);
  }
  return formatted;
};

router.get('/:repoName', function(req, res, next) {
  var splitArr = req.body.repoName.split(",");
  readTeamSizePoints(splitArr[1], splitArr[0],splitArr[2],splitArr[3], function(csvArr) {
    console.log("Read DB success\n")
    res.render('index', {
      title: 'J.C. Demo',
      repoName: splitArr[1],
      repoOwner: splitArr[0],
      dataFromNode: csvArr[0],
      dateDataFromNode: csvArr[1],
      dropdownVals: req.app.locals.dropdownVals,
      backfill:splitArr[2],
      threshold:splitArr[3]
    });
  });

});

router.post("/test/submit", function(req, res, next) {
  repoName = req.body.repoName;
  console.log(repoName);
  var splitArr = req.body.repoName.split(",");
  readTeamSizePoints(splitArr[1], splitArr[0],splitArr[2],splitArr[3], function(csvArr) {
    console.log("Read DB success\n")
    res.render('index', {
      title: 'J.C. Demo',
      repoName: splitArr[1],
      repoOwner: splitArr[0],
      dataFromNode: csvArr[0],
      dateDataFromNode: csvArr[1],
      dropdownVals: req.app.locals.dropdownVals,
      backfill:splitArr[2],
      threshold:splitArr[3]
    });
  });
});

// Returns an array of size 2, array[0] is CSV string for active_team_size vs time points,  array[1] is CSV string for date vs release_name points
function readTeamSizePoints(stringRepoName, stringRepoOwner, backfillInt, thresholdInt, callBackFun) {
  let stringQuery1 = " ";
  if (backfillInt < 0 || thresholdInt < 0) {
    stringQuery1 = "SELECT date, team_size,time_delta,threshold  FROM active_team_size_vs_time WHERE repo_name=\'{0}\' AND repo_owner=\'{1}\' ORDER BY date ASC\n".format(stringRepoName, stringRepoOwner);
  } else {
    stringQuery1 = "SELECT date, team_size,time_delta,threshold  FROM active_team_size_vs_time WHERE repo_name=\'{0}\' AND repo_owner=\'{1}\' AND time_delta=\'{2}\' AND threshold=\'{3}\' ORDER BY date ASC\n".format(stringRepoName, stringRepoOwner, backfillInt,thresholdInt);
  }
  console.log(stringQuery1);
  let stringQuery2 = "SELECT date, release_name FROM release_table WHERE repo_name=\'{0}\' AND repo_owner=\'{1}\' ORDER BY date ASC\n".format(stringRepoName, stringRepoOwner);
  console.log(stringQuery2);
  var stringCSV1 = "key,value,backfill,threshold\n";
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
        stringCSV1 = stringCSV1.concat("{0},{1},{2},{3}\n".format(row.date, row.team_size, row.time_delta,row.threshold));
      };

      for (var i = 0; i < result2.length; i++) {
        var row = result2[i];
        stringCSV2 = stringCSV2.concat("{0},{1}\n".format(row.date, row.release_name));
      };

      callBackFun([stringCSV1, stringCSV2]);
    });
  });
};

module.exports = router;