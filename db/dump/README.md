# Dumping the mysql database

## Dump the table to XML

Run in a mysql container (see the volume dir):

    docker run --rm -it -v /Users/bothy/videos-dump/:/dump mysql /bin/bash

Dump to a file:

    mysqldump  --column-statistics=0 -h HOSTNAME -u USER --password=PASSWORD --databases bo-production --tables metadata_orig --result-file=/dump/all-videos.sql
    
    
    
## Convert SQL to CSV

    ./mysqldump_to_csv.py < ~/videos-dump/all-videos.sql > all-videos.csv
    
Prepend headers

    cat header.csv all-videos.csv > all-videos-with-header.csv

    
## Import CSV to ES

    python csv_to_elastic.py \
        --elastic-address 'https://search-test-search-gvzmbjdq7khuhjdtb33zbldpqu.eu-west-1.es.amazonaws.com:80/' \
        --csv-file all-videos-with-header.csv \
        --elastic-index 'videos-all' \
        --datetime-field=date \
        --json-struct '{
            "id" : "%id%",
            "source" : "%source%",
            "unique_id" : "%unique_id%",
            "namespace" : "%namespace%",
            "title" : "%title%",
            "description" : "%description%",
            "date" : "%date%",
            "duration" : "%duration%",
            "keywords" : "%keywords%",
            "price_category" : "%price_category%",
            "sounds" : "%sounds%",
            "color" : "%color%",
            "location" : "%location%",
            "country" : "%country%",
            "state" : "%state%",
            "city" : "%city%",
            "region" : "%region%",
            "alternative_id" : "%alternative_id%",
            "alt_source" : "%alt_source%",
            "restrictions" : "%restrictions%",
            "type_id" : "%type_id%",
            "reference_id" : "%reference_id%"
        }'