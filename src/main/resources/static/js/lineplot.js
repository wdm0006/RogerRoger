var forceRedraw = function(element){

    if (!element) { return; }

    var n = document.createTextNode(' ');
    var disp = element.style.display;  // don't worry about previous display style

    element.appendChild(n);
    element.style.display = 'none';

    setTimeout(function(){
        element.style.display = disp;
        n.parentNode.removeChild(n);
    },20); // you can play with this timeout to make it as short as possible
}

function line_plot(ts_data) {
    if (ts_data.length > 0) {
      var keys = [];
      for (var k in ts_data[0]) {
        if (k != 'time_stamp') {
          keys.push(k);
        }
      }
    };
    var param = keys[0];
    var x = [];
    var y = [];

    ts_data.forEach(function(d) {
        d[param] = d[param][0]
        d['time_stamp'] = d['time_stamp'][0]
    });

    ts_data = ts_data.map(function(obj) {
       return {
          x: obj['time_stamp'],
          y: obj[param]
       }
    });

    var plot_data = [
        {
            values: ts_data,
            key: param,
            color: '#ff7f0e'
        }
    ];

    return plot_data
};

function parse_fn(data) {
    console.log('entered parse')
    console.log(data);
    var plot_data = line_plot(data);
    console.log(plot_data);

    // form the div as we want to
    var div_id = plot_data[0].key;
    div_id = div_id.replace(/\W/g, '')
    var width = 800, height = 300;

    nv.addGraph(function() {
        var chart = nv.models.lineChart()
                    .margin({
                        top: 20,
                        right: 20,
                        bottom: 20,
                        left: 100
                    })
                    .width(width)
                    .height(height)
                    .useInteractiveGuideline(true);

        // set the x-axis to be time series
        chart.xAxis
          .tickFormat(function(d) {
            return d3.time.format('%d/%m %H:%M:%S')(new Date(d * 1000))
        });

        // only show 2 digit precision on y-axis
        chart.yAxis.tickFormat(d3.format(',.2f'));

        d3.select('#' + div_id + ' svg')
           .datum(plot_data)
           .transition()
           .duration(500)
           .call(chart)
           .style({ 'width': width, 'height': height });

        nv.utils.windowResize(chart.update);
        return chart;
    });
};

d3_queue.queue().defer(d3.json, "/metrics/elasticsearch_stats_cluster_stats_indices_docs_count").await(parse_fn)
d3_queue.queue().defer(d3.json, "/metrics/elasticsearch_stats_cluster_stats_indices_store_size_in_bytes").await(parse_fn)
d3_queue.queue().defer(d3.json, "/metrics/top_stats_memory_available").await(parse_fn)
d3_queue.queue().defer(d3.json, "/metrics/top_stats_cpu_system_load_average").await(parse_fn)


