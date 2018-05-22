context("strip_object_id")

library(testthat)

test_that("removes the wrapper text",{
  stripped = strip_object_id("ObjectId(58adc37c1810c71f62b2afd3)")

  expect_equal(stripped, "58adc37c1810c71f62b2afd3")
})

