library(testthat)
source("search_utils.R")

context("search_utils tests")

test_that("clean lowercases characters",{
  clean("aB") %>% expect_equivalent("ab")
})

test_that("clean removes special characters",{
  clean("hello!#,\"$%[]()-") %>% expect_equivalent("hello")
})

test_that("clean does not remove digits", {
  clean("one1 2 3") %>% expect_equivalent("one1 2 3")
})

test_that("clean removes duplicate spaces", {
  clean("a        a") %>% expect_equivalent("a a")
})
