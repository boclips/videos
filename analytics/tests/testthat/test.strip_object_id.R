context("strip_object_id")

library(testthat)

test_that("return hexadecimal value",{
  stripped = strip_object_id("ObjectId(58adc37c1810c71f62b2afd3)")

  expect_equal(stripped, "58adc37c1810c71f62b2afd3")
})
