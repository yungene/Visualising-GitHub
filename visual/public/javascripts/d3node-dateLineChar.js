const D3Node = require('d3-node');

function dateLine({
  data,
  dateData,
  selector: _selector = '#chart',
  container: _container = `
  <div id="container">
  <h2>Line Chart</h2>
  <div id="chart"></div>
  </div>
  `,
  style: _style = '',
  width: _width = 960,
  height: _height = 500,
  margin: _margin = { top: 40, right: 20, bottom: 60, left: 30 },
  lineWidth: _lineWidth = 1.5,
  lineColor: _lineColor = 'steelblue',
  lineColors: _lineColors = ['steelblue'],
  isCurve: _isCurve = true,
  tickSize: _tickSize = 5,
  tickPadding: _tickPadding = 5,
} = {}) {
  const d3n = new D3Node({
    selector: _selector,
    svgStyles: _style,
    container: _container,
  });

  const d3 = d3n.d3;

  const width = _width - _margin.left - _margin.right;
  const height = _height - _margin.top - _margin.bottom;

  const svg = d3n.createSVG(_width, _height)
  .append('g')
  .attr('transform', `translate(${_margin.left}, ${_margin.top})`);

  const g = svg.append('g');

  var myScale = d3.scaleLinear()
  .domain([10,12000])
  .range([0, 1200]);

  //console.log("Map 10 to " + myScale(10));
    //console.log("Map 10000 to " + myScale(10000));


    const { allKeys } = data;
    console.log("Extent function on " + {allKeys});
    const xScale = d3.scaleTime()
    .domain(allKeys ? d3.extent(allKeys) : d3.extent(data, d => d.key))
    .range([0, width]);
    const yScale = d3.scaleLinear()
    .domain(allKeys ? [d3.min(data, d => d3.min(d, v => v.value)), d3.max(data, d => d3.max(d, v => v.value))] : d3.extent(data, d => d.value))
    .rangeRound([height, 0]);
    const xAxis = d3.axisBottom(xScale)
    .tickSize(_tickSize)
    .tickFormat(d3.timeFormat("%y-%m-%d"))
    .tickPadding(_tickPadding);
    const yAxis = d3.axisLeft(yScale)
    .tickSize(_tickSize)
    .tickPadding(_tickPadding);

    const lineChart = d3.line()
    .x(function (d) { var x = xScale(d.key);
                        //console.log("x is " + x + " key is " + d.key);
                        return x; })
    .y(function (d) { var y = yScale(d.value);
                        //console.log("y is " + y + " value is " + d.value);
                        return y; });

    if (_isCurve) lineChart.curve(d3.curveBasis);

    g.append('g')
    .attr('transform', `translate(0, ${height})`)
    .call(xAxis);

    g.append('g').call(yAxis);

    g.append('g')
    .attr('fill', 'none')
    .attr('stroke-width', _lineWidth)
    .selectAll('path')
    .data(allKeys ? data : [data])
    .enter().append("path")
    .attr('stroke', (d, i) => i < _lineColors.length ? _lineColors[i] : _lineColor)
    .attr('d', lineChart);

    for(var i = 0; i < dateData.length; i++){
      console.log(dateData[i].key);
      svg
      .append("line")
      .attr("x1",xScale(dateData[i].key))
  //<<== change your code here
      .attr("y1", 0)
      .attr("x2", xScale(dateData[i].key))  //<<== and here
      .attr("y2", height)
      .style("stroke-width", 1)
      .style("stroke", "red")
      .style("fill", "none");

      svg
      .append("text")
       .style("text-anchor", "start")
       .style("font-size", "11px") 
       .text(dateData[i].value)
        .attr('transform', (d,j)=>{
          var y = 0 - (_margin.top/3);
          var x =  xScale(dateData[i].key);
        return 'translate( '+x+' , '
        +y+'),'+ 'rotate(90)';})
       .attr("x",0)
       .attr("y", 0);
     }

  // Add labels
  svg.append("text")      // text label for the x-axis
  .attr("x", width / 2 )
  .attr("y",  height + _margin.bottom/2)
  .style("text-anchor", "middle")
  .text("Date");

  svg.append("text")      // text label for the y-axis
  .attr("y",30 - _margin.left)
  .attr("x",50 - (height / 2))
  .attr("transform", "rotate(-90)")
  .style("text-anchor", "end")
  .style("font-size", "16px")
  .text("Active team size");

  svg.append("text")      // text label for chart Title
  .attr("x", width / 2 )
  .attr("y", 0 - (_margin.top/2))
  .style("text-anchor", "middle")
  .style("font-size", "16px") 
  .style("text-decoration", "underline") 
  .text("Active team size vs Time (2 commits in last 30 days threshold)");
  return d3n;
}

module.exports = dateLine;
