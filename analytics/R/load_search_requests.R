read_search_csv = function(file) {
  read.csv(
    file,
    col.names = c("url", "user", "date"),
    colClasses = c("character", "character", "Date")
  )
}

load_search_requests = function(file, after_date) {
  searches = read_search_csv(file)
  searches = subset(searches, date > after_date)
  searches = cbind(searches, parse_search_url(searches$url))
  searches = subset(searches, select = c(theme, query, user))
  searches$user = strip_object_id(searches$user)
  searches = unique(searches)
  searches
}
