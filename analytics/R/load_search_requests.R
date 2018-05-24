read_searches_csv = function(file) {
  searches = read.csv(
    file,
    col.names = c("url", "user", "date"),
    colClasses = c("character", "character", "Date")
  )
  searches
}

read_users_csv = function(file) {
  users = read.csv(
    file,
    col.names = c("id", "email", "name", "surname"),
    colClasses = "character"
  )
  users
}

load_search_requests = function(searches_csv, users_csv, after) {
  searches = read_searches_csv(searches_csv)
  searches = subset(searches, date > after)
  searches = cbind(searches, parse_search_url(searches$url))
  searches = subset(searches, select = c("theme", "query", "user"))
  searches = unique(searches)

  users = read_users_csv(users_csv)

  searches = merge(searches, users, by.x = "user", by.y = "id")

  searches = subset(searches, !grepl("boclips.com", email))

  subset(searches, select = c("theme", "query", "email"))
}
