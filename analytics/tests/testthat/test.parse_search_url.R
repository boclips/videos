context("parse_search_url")

library(testthat)

urls = c(
  "http://boclips.com/search/?typename=360%20VR%20Immersive",
  "http://watchboclips.com/search/racism",
  "https://www.pearson.boclips.com/search/programming?typeId=4",
  "http://boclips.com/search/cyber%20space"
)

test_that("extracts theme",{
  df = parse_search_url(urls)
  expect_equivalent(df$theme, factor(c("boclips.com", "watchboclips.com", "pearson.boclips.com", "boclips.com")))
})

test_that("extracts query",{
  df = parse_search_url(urls)
  expect_equivalent(df$query, c("", "racism", "programming", "cyber space"))
})
