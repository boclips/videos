email_freqs = function(searches, limit.rows = 1000, min.count = 1) {
  # compute frequencies:
  freqs = searches$email %>% table %>% data.frame()
  colnames(freqs) = c("email", "count")
  freqs$email = as.character(freqs$email)

  # sort and filter:
  freqs[order(-freqs$count),] %>% subset(count >= min.count) %>% head(limit.rows)
}
