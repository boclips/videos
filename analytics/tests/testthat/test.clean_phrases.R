context("clean_phrases")

library(testthat)

test_that("lowercases characters",{
  clean_phrases("aB") %>% expect_equivalent("ab")
})

test_that("removes special characters",{
  clean_phrases("hello!#,\"$%[]()-") %>% expect_equivalent("hello")
})

test_that("does not remove digits", {
  clean_phrases("one1 2 3") %>% expect_equivalent("one1 2 3")
})

test_that("removes duplicate spaces", {
  clean_phrases("a        a") %>% expect_equivalent("a a")
})
