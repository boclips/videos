context("email_freqs")

library(testthat)

source("mock_searches.R")

test_that("returns query counts by email descending",{
  searches = mock_searches()

  freqs = searches %>% email_freqs()

  expect_equivalent(freqs$email, c("john@gmail.com", "clara@hotmail.com", "dave@gmail.com"))
  expect_equivalent(freqs$count, c(2, 1, 1))
})

test_that("truncates results when row limit specified", {
  searches = mock_searches()

  freqs = searches %>% email_freqs(limit = 2)

  expect_equivalent(freqs$email, c("john@gmail.com", "clara@hotmail.com"))
  expect_equivalent(freqs$count, c(2, 1))
})

test_that("truncates results when limit specified", {
  searches = mock_searches()

  freqs = searches %>% email_freqs(min.count = 2)

  expect_equivalent(freqs$email, "john@gmail.com")
  expect_equivalent(freqs$count, 2)
})

