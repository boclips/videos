context("load_search_requests")

library(testthat)

test_that("reads and parses data",{
  searches = load_search_requests("searches-sample.csv", as.Date('2018-01-01'))

  expected_searches = data.frame(
    theme = c("boclips.com", "watchboclips.com", "pearson.boclips.com"),
    query = c("", "ghosts", "ted"),
    user = c("58adc37c1810c71f62b2afd3", "58adc37c1810c71f62b2afd4", "58adc37c1810c71f62b2afd5")
  )

  expect_equivalent(searches$theme, factor(c("boclips.com", "watchboclips.com", "pearson.boclips.com")))
  expect_equivalent(searches$query, c("", "ghosts", "ted")) # TODO email
  expect_equivalent(searches$user, c("58adc37c1810c71f62b2afd3", "58adc37c1810c71f62b2afd4", "58adc37c1810c71f62b2afd5"))
})

