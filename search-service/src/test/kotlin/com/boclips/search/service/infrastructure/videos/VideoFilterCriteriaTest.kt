package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoFilterCriteriaTest {
    @Test
    fun `creates video filters given query`() {
        val videoQuery = VideoQueryFactory.aRandomExample()

        assertThat(VideoFilterCriteria.allCriteria(videoQuery).toString()).isEqualTo(
            """
            {
              "bool" : {
                "must" : [
                  {
                    "bool" : {
                      "should" : [
                        {
                          "range" : {
                            "durationSeconds" : {
                              "from" : 20,
                              "to" : 100,
                              "include_lower" : true,
                              "include_upper" : true,
                              "boost" : 1.0
                            }
                          }
                        },
                        {
                          "range" : {
                            "durationSeconds" : {
                              "from" : 600,
                              "to" : 900,
                              "include_lower" : true,
                              "include_upper" : true,
                              "boost" : 1.0
                            }
                          }
                        }
                      ],
                      "adjust_pure_negative" : true,
                      "minimum_should_match" : "1",
                      "boost" : 1.0
                    }
                  },
                  {
                    "range" : {
                      "releaseDate" : {
                        "from" : "2014-01-30",
                        "to" : "2015-01-30",
                        "include_lower" : true,
                        "include_upper" : true,
                        "boost" : 1.0
                      }
                    }
                  },
                  {
                    "bool" : {
                      "should" : [
                        {
                          "match_phrase" : {
                            "subjectIds" : {
                              "query" : "subject-123",
                              "slop" : 0,
                              "zero_terms_query" : "NONE",
                              "boost" : 1.0
                            }
                          }
                        }
                      ],
                      "adjust_pure_negative" : true,
                      "boost" : 1.0,
                      "_name" : "subjects-filter"
                    }
                  },
                  {
                    "term" : {
                      "promoted" : {
                        "value" : true,
                        "boost" : 1.0
                      }
                    }
                  }
                ],
                "filter" : [
                  {
                    "term" : {
                      "source" : {
                        "value" : "boclips",
                        "boost" : 1.0
                      }
                    }
                  },
                  {
                    "bool" : {
                      "must" : [
                        {
                          "range" : {
                            "ageRangeMin" : {
                              "from" : 5,
                              "to" : 10,
                              "include_lower" : true,
                              "include_upper" : false,
                              "boost" : 1.0
                            }
                          }
                        },
                        {
                          "range" : {
                            "ageRangeMax" : {
                              "from" : 5,
                              "to" : 10,
                              "include_lower" : false,
                              "include_upper" : true,
                              "boost" : 1.0
                            }
                          }
                        }
                      ],
                      "adjust_pure_negative" : true,
                      "boost" : 1.0
                    }
                  }
                ],
                "adjust_pure_negative" : true,
                "boost" : 1.0
              }
            }
        """.trimIndent()
        )
    }
}
