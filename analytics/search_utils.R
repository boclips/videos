library(quanteda)

clean = function(text) {
  tokens(text, remove_punct = TRUE) %>%
    tokens_tolower %>%
    sapply(FUN=paste0, collapse=" ")
}