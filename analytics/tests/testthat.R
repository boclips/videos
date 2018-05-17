Sys.setenv("R_TESTS" = "")

library(testthat)
library(analytics)

test_check("analytics")
