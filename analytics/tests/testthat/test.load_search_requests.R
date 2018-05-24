context("load_search_requests")

library(testthat)

test_that("reads and parses data",{
  searches = load_search_requests("searches-sample.csv", "users-sample.csv", after = as.Date('2018-01-01'))

  expect_equivalent(searches$theme, factor(c("boclips.com", "watchboclips.com", "pearson.boclips.com")))
  expect_equivalent(searches$query, c("", "ghosts", "ted"))
  expect_equivalent(searches$email, c("bob@example.com", "charlie@example.com", "dave@example.com"))
})

