package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoQuery
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class FilterDecoratorTest {

    @Test
    fun `attaches all filters`() {
        val boolQuery = QueryBuilders.boolQuery()
        val videoQuery = VideoQuery(
            phrase = "some phrase",
            ageRangeMax = 10,
            ageRangeMin = 5,
            subjectIds = setOf("subject-123"),
            maxDuration = Duration.ofSeconds(100),
            minDuration = Duration.ofSeconds(20),
            source = SourceType.BOCLIPS,
            includeTags = listOf("include-tag"),
            excludeTags = listOf("exclude-tag"),
            releaseDateFrom = LocalDate.of(2014, 1, 30),
            releaseDateTo = LocalDate.of(2015, 1, 30),
            promoted = true
        )

        FilterDecorator(boolQuery).apply(videoQuery)

        assertThat(boolQuery.toString()).isEqualTo(
            """
{
  "bool" : {
    "must" : [
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
              "bool" : {
                "must" : [
                  {
                    "range" : {
                      "ageRangeMin" : {
                        "from" : null,
                        "to" : 5,
                        "include_lower" : true,
                        "include_upper" : true,
                        "boost" : 1.0
                      }
                    }
                  },
                  {
                    "range" : {
                      "ageRangeMax" : {
                        "from" : 5,
                        "to" : null,
                        "include_lower" : true,
                        "include_upper" : true,
                        "boost" : 1.0
                      }
                    }
                  }
                ],
                "adjust_pure_negative" : true,
                "boost" : 1.0
              }
            },
            {
              "bool" : {
                "must" : [
                  {
                    "range" : {
                      "ageRangeMin" : {
                        "from" : 5,
                        "to" : null,
                        "include_lower" : true,
                        "include_upper" : true,
                        "boost" : 1.0
                      }
                    }
                  },
                  {
                    "range" : {
                      "ageRangeMin" : {
                        "from" : null,
                        "to" : 10,
                        "include_lower" : true,
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
          "boost" : 1.0
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
        "bool" : {
          "must" : [
            {
              "term" : {
                "tags" : {
                  "value" : "include-tag",
                  "boost" : 1.0
                }
              }
            }
          ],
          "adjust_pure_negative" : true,
          "boost" : 1.0
        }
      },
      {
        "term" : {
          "source" : {
            "value" : "boclips",
            "boost" : 1.0
          }
        }
      }
    ],
    "must_not" : [
      {
        "terms" : {
          "tags" : [
            "exclude-tag"
          ],
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
