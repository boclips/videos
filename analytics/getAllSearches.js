let cursor = db.analytics.requests.aggregate([
    {
        $match: {referrer: {$regex: ".*/search/.+"}, user: {$ne: null}}
    },
    {
        $project: {
            referrer: 1,
            _id: 0,
            user: 1,
            date_created: {$dateToString: {format: "%Y-%m-%dT%H:%M:00Z", date: "$date_created"}}
        }
    },
    {
        $group: {_id: {referrer: '$referrer', user: '$user', date_created: '$date_created'}}
    }
]);

const cleanUrl = (input) => decodeURI(input);
print('[');
while (cursor.hasNext()) {
    let el = cursor.next();

    let extractSearchKeywordRegex = /(?:https?:\/\/)?([^\/]+)\/search\/([^\?]*)(.*)/g;
    let matches = extractSearchKeywordRegex.exec(el._id.referrer);
    el.theme = cleanUrl(matches[1]);
    el.keyword = cleanUrl(matches[2]);
    el.user = el._id.user ? el._id.user.valueOf() : null;
    el.date_created = el._id.date_created ? el._id.date_created : null;
    el.modifiers = cleanUrl(matches[3]);
    el.modifiers = el.modifiers ? el.modifiers.replace('?', '').split('&') : [];

    delete el._id;
    print(tojson(el),",")
}
print(']');
