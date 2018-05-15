library(testthat)
source("search_utils.R")

context("search_utils tests")

test_that("sanitize_query lowercases characters",{
  expect_equal(sanitize_query("aBCd"), "abcd")
})