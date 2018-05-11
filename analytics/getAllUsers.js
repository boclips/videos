let cursor = db.users.aggregate([
    {
        $match: {}
    },
    {
        $project: {
            _id: 0,
            id: "$_id",
            email: 1,
            name: 1,
            surname: 1,
        }
    }
]);

const fields = ["id", "email", "name", "surname"];
print(fields);

while (cursor.hasNext()) {
    const user = cursor.next();
    user.id = user.id.valueOf();
    const userCsvLine = fields
        .map(key => "\"" + user[key].replace("\"", "\\\"") + "\"")
        .join(",");

    print(userCsvLine)
}
