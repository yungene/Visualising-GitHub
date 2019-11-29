var express = require('express');
var router = express.Router();

/* GET users listing. */
router.get('/', function(req, res, next) {
    var splitArr = [];
  if (req.app.locals.dropdownVals.length == 0) {
    splitArr = ["no-input", "no-input", -1, -1];
  } else {
    splitArr = req.app.locals.dropdownVals[0].split(",");
  }
  readGraph(function(graph) {
    console.log("Read graph success\n")
    res.render('users', {
      title: 'J.C. Users-Demo',
      graph: graph
    });
  });
});


// Returns an array of size 2, array[0] is CSV string for active_team_size vs time points,  array[1] is CSV string for date vs release_name points
function readGraph(callBackFun) {
  let stringQueryNodes = "SELECT node_id, user_name,followers,following FROM nodes;";
  let stringQueryEdges = "SELECT out_node_id AS u, in_node_id AS v, weight FROM edges;";
  var stringCSV1 = "key,value,backfill,threshold\n";
  var stringCSV2 = "key,value\n";
  db.query(stringQueryNodes, (err1, result1) => {
    if (err1) {
      console.log(err1);
      res.redirect('/error');
    }
    db.query(stringQueryEdges, (err2, result2) => {
      if (err2) {
        console.log(err2);
        res.redirect('/error');
      }

      var graph = {};
      graph.nodes = [];
      graph.edges = [];
      for (var i = 0; i < result1.length; i++) {
        var row = result1[i];
        var node = {};
        node.id = +row.node_id;
        node.name = row.user_name;
        node.followers = +row.followers;
        node.following = +row.following;
        graph["nodes"].push(node);
      };

      for (var i = 0; i < result2.length; i++) {
        var row = result2[i];
        var edge = {};
        edge.source = +row.u;
        edge.target = +row.v;
        edge.weight = +row.weight;
        graph["edges"].push(edge);
      };
      console.log(JSON.stringify(graph));
      callBackFun(graph);
    });
  });
};

module.exports = router;
