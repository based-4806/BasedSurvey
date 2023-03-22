$(document).ready(function() {
    $('.mc').each(function(i, obj) {
        let data = {};
        for (let i = 0; i < parseInt(obj.dataset.options); i++) {
            data[obj.dataset['option' + i]] = obj.dataset['response' + i];
        }

        //https://d3-graph-gallery.com/graph/pie_annotation.html
        // set the dimensions and margins of the graph
        const width = 450,
            height = 450,
            margin = 40;

        // The radius of the pieplot is half the width or half the height (smallest one). I subtract a bit of margin.
        const radius = Math.min(width, height) / 2 - margin

        // append the svg object to the div
        const svg = d3.select(`#${$(obj).attr("id")}`)
            .append("svg")
                .attr("width", width)
                .attr("height", height)
            .append("g")
                .attr("transform", `translate(${width / 2}, ${height / 2})`);

        // set the color scale
        const color = d3.scaleOrdinal()
            .range(d3.schemeSet2);

        // Compute the position of each group on the pie:
        const pie = d3.pie()
            .value(function(d) {return d[1]})
        const data_ready = pie(Object.entries(data))
        // Now I know that group A goes from 0 degrees to x degrees and so on.

        // shape helper to build arcs:
        const arcGenerator = d3.arc()
            .innerRadius(0)
            .outerRadius(radius)

        // Build the pie chart: Basically, each part of the pie is a path that we build using the arc function.
        svg
            .selectAll('mySlices')
            .data(data_ready)
            .join('path')
            .attr('d', arcGenerator)
            .attr('fill', function(d){ return(color(d.data[0])) })
            .attr("stroke", "black")
            .style("stroke-width", "2px")
            .style("opacity", 0.7)

        // Now add the annotation. Use the centroid method to get the best coordinates
        svg
            .selectAll('mySlices')
            .data(data_ready)
            .join('text')
            .text(function(d){ return "grp " + d.data[0]})
            .attr("transform", function(d) { return `translate(${arcGenerator.centroid(d)})`})
            .style("text-anchor", "middle")
            .style("font-size", 17)
    });
    $('.r').each(function(i, obj) {
        let qID = `${$(obj).attr("id")}`;
        $("#" + qID + "+table").remove();

        //https://d3-graph-gallery.com/graph/histogram_binSize.html
        const margin = {top: 10, right: 30, bottom: 30, left: 40},
            width = 460 - margin.left - margin.right,
            height = 400 - margin.top - margin.bottom;

        d3.select(`#${qID}`)
            .append("input")
                .attr("type", "number")
                .attr("min", "1")
                .attr("max", "100")
                .attr("value", "7")
                .attr("id", `nBin${qID}`);

        const svg = d3.select(`#${qID}`)
            .append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
            .append("g")
                .attr("transform",
                    `translate(${margin.left},${margin.top})`);

        // get the data
        //id are in the format r#, remove first character
        d3.json(window.location.origin + `/questions/${qID.substring(1)}`).then( function(data) {
            // X axis: scale and draw:
            const x = d3.scaleLinear()
                .domain([parseInt(data.low), parseInt(data.high)])
                .range([0, width]);
            data = data.responses;
            svg.append("g")
                .attr("transform", `translate(0,${height})`)
                .call(d3.axisBottom(x));

            // Y axis: initialization
            const y = d3.scaleLinear()
                .range([height, 0]);
            const yAxis = svg.append("g")

            // A function that builds the graph for a specific value of bin
            function update(nBin) {

                // set the parameters for the histogram
                const histogram = d3.histogram()
                    .value(function(d) { return d; })   // I need to give the vector of value
                    .domain(x.domain())  // then the domain of the graphic
                    .thresholds(x.ticks(nBin)); // then the numbers of bins

                // And apply this function to data to get the bins
                const bins = histogram(data);

                // Y axis: update now that we know the domain
                y.domain([0, d3.max(bins, function(d) { return d.length; })]);   // d3.hist has to be called before the Y axis obviously
                yAxis
                    .transition()
                    .duration(1000)
                    .call(d3.axisLeft(y));

                // Join the rect with the bins data
                const u = svg.selectAll("rect")
                    .data(bins)

                // Manage the existing bars and eventually the new ones:
                u
                    .join("rect") // Add a new rect for each new elements
                    .transition() // and apply changes to all of them
                    .duration(1000)
                    .attr("x", 1)
                    .attr("transform", function(d) { return `translate(${x(d.x0)}, ${y(d.length)})`})
                    .attr("width", function(d) { return x(d.x1) - x(d.x0) -1 ; })
                    .attr("height", function(d) { return height - y(d.length); })
                    .style("fill", "#69b3a2")

            }


            // Initialize with 7 bins
            update(7)


            // Listen to the button -> update if user change it
            d3.select(`#nBin${qID}`).on("input", function() {
                update(+this.value);
            });

        });

    });
});