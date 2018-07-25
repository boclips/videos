var fs = require('fs'),
    xml2js = require('xml2js');

var parser = new xml2js.Parser();


const fileName = process.argv[2];

console.log('Input file', fileName);


function onFileRead(error, data) {

    if(error) {
        console.error('Error reading file', error);
        return;
    }

    parser.parseString(data, function (err, result) {
        console.dir(result);
        console.log('Done');
    });
}

fs.readFile(fileName, onFileRead);