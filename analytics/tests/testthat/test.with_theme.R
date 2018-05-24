context("with_theme")

library(testthat)

source("mock_searches.R")

test_that("filters out rows where theme doesn't match",{
  searches = mock_searches()

  filtered_searches = searches %>% with_theme("boclips.com")

  expect_equal(filtered_searches$query, c("philosophy", "", "science"))
})

