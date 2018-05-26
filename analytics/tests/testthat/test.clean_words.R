context("clean_words")

library(testthat)

test_that("lowercases characters",{
  clean_words("aB") %>% expect_equivalent("ab")
})

test_that("removes special characters",{
  clean_words("hello!#,\"$%[]()-") %>% expect_equivalent("hello")
})

test_that("does not remove digits", {
  clean_words("a123") %>% expect_equivalent("a123")
})

test_that("flattens the words", {
  clean_words(c("one two three", "two", "three")) %>% expect_equivalent(c("one", "two", "three", "two", "three"))
})

test_that("removes stop words", {
  clean_words("one and two") %>% expect_equivalent(c("one", "two"))
})
