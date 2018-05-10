const fs = require('fs');
const data = fs.readFileSync('all-searches.json');
const _ = require('lodash');
const jsonexport = require('jsonexport');

const jsonInput = JSON.parse(data);

function getSearchQueryCountByKey(groupKey, searchEntries) {
    const groupCount = _.groupBy(searchEntries, groupKey);

    const output = [];
    _.forEach(groupCount, (value, key) => {
        let o = {};
        o[groupKey] = key;
        o["count"] = value.length;
        output.push(o);
    });

    return _.reverse(_.sortBy(output, 'count', -1));
}

function wordHistogram(searchEntries) {
    searchEntries = _.map(searchEntries, entry => {
        entry.numberOfWords = entry.keyword.split(' ').length;
        return entry;
    });
    return getSearchQueryCountByKey('numberOfWords', searchEntries);
}

// console.log(getSearchQueryCountByKey('THEME', jsonInput));
// console.log(_.slice(getSearchQueryCountByKey('user', jsonInput), 0, 30));
// console.log(wordHistogram(jsonInput));

jsonexport(jsonInput, function (err, csv) {
    if (err) return console.log(err);
    console.log(csv);
});

