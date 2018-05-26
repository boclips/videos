context("email_domain")

library(testthat)

emails = c(
  "john@example.com",
  "dave@gmail.com",
  "bob@gmail.com"
)

test_that("extracts domain",{
  domains = email_domain(emails)
  expect_equivalent(domains, c("example.com", "gmail.com", "gmail.com"))
})
