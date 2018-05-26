stopwords = tm::stopwords(kind = "en")

clean_words = function(text) {
  text %>% clean_phrases %>% strsplit(" ") %>% unlist %>% subset(!. %in% stopwords)
}
