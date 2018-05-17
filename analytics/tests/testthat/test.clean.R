context("clean")

library(testthat)

test_that("lowercases characters",{
  clean("aB") %>% expect_equivalent("ab")
})

test_that("removes special characters",{
  clean("hello!#,\"$%[]()-") %>% expect_equivalent("hello")
})

test_that("does not remove digits", {
  clean("one1 2 3") %>% expect_equivalent("one1 2 3")
})

test_that("removes duplicate spaces", {
  clean("a        a") %>% expect_equivalent("a a")
})
