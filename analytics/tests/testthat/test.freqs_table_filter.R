context("freqs_table_filter")

library(testthat)

test_that("does nothing when no row limit or min count specified",{
  freqs = data.frame(value = c("a", "b", "c"), count = c(3, 2, 1), stringsAsFactors = FALSE)
  filtered_freqs = freqs %>% freqs_table_filter

  expect_equivalent(filtered_freqs, freqs)
})

test_that("truncates results when row limit specified", {
  freqs = data.frame(value = c("a", "b", "c"), count = c(3, 2, 1), stringsAsFactors = FALSE)
  filtered_freqs = freqs %>% freqs_table_filter(limit.rows = 2)

  expect_equivalent(filtered_freqs$value, c("a", "b"))
  expect_equivalent(filtered_freqs$count, c(3, 2))
})

test_that("removes rows when min count specified", {
  freqs = data.frame(value = c("a", "b", "c"), count = c(3, 2, 1), stringsAsFactors = FALSE)
  filtered_freqs = freqs %>% freqs_table_filter(min.count = 3)

  expect_equivalent(filtered_freqs$value, "a")
  expect_equivalent(filtered_freqs$count, 3)
})

