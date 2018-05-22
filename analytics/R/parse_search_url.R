parse_search_url = function(urls) {
  pattern = "(?:.*://)?(?:www\\.)?([^/]+)/search/([^?]*)[?]?.*"
  columns = data.frame(str_match(urls, pattern))
  colnames(columns) = c("url", "theme", "query")
  columns$query = url_decode(columns$query)
  subset(columns, select=c(theme, query))
}
