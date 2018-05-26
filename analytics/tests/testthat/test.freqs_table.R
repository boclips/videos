context("freqs_table")

library(testthat)

chars = c("x", "a", "y", "x", "a", "x")

test_that("returns query counts by email descending",{
  freqs = chars %>% freqs_table

  expect_equivalent(freqs$value, c("x", "a", "y"))
  expect_equivalent(freqs$count, c(3, 2, 1))
})

test_that("truncates results when row limit specified", {
  freqs = chars %>% freqs_table(limit = 2)

  expect_equivalent(freqs$value, c("x", "a"))
  expect_equivalent(freqs$count, c(3, 2))
})

test_that("truncates results when limit specified", {
  freqs = chars %>% freqs_table(min.count = 3)

  expect_equivalent(freqs$value, "x")
  expect_equivalent(freqs$count, 3)
})

test_that("turns values into factors when valuesAsFactors true",{
  freqs = chars %>% freqs_table(valuesAsFactors = TRUE, min.count = 2)

  expect_equal(freqs$value, factor(c("x", "a")))
  #expect_equivalent(freqs$value)
  expect_equivalent(freqs$count, c(3, 2))
})
