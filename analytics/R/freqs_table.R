freqs_table = function(values, limit.rows = NULL, min.count = 1, valuesAsFactors = FALSE) {

  freqs = values %>%
    table %>%
    data.frame() %>%
    set_colnames(c("value", "count")) %>%
    .[order(-.$count),] %>%
    freqs_table_filter(limit.rows = limit.rows, min.count = min.count)

  normalize = if(valuesAsFactors) factor else as.character
  freqs$value = normalize(freqs$value)

  freqs
}
