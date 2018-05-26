freqs_table_filter = function(freqs, limit.rows = NULL, min.count = 1) {
  if(!is.null(limit.rows)) {
    freqs = head(freqs, limit.rows)
  }

  freqs = subset(freqs, count >= min.count)

  freqs
}
