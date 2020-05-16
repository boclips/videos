package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CollectionFilterCriteriaTest {
    @Test
    fun `attaches all filters for collection`() {
        val collectionQuery = CollectionQuery(
            ageRangeMax = 10,
            ageRangeMin = 5,
            resourceTypes = setOf("Lesson Guide")
        )

        val boolQuery = CollectionFilterCriteria.allCriteria(collectionQuery)

        Assertions.assertThat(boolQuery.toString()).isEqualTo(
            """
                {
                  "bool" : {
                    "filter" : [
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
                          "boost" : 1.0,
                          "_name" : "age-ranges-filter"
                        }
                      },
                      {
                        "bool" : {
                          "should" : [
                            {
                              "terms" : {
                                "attachmentTypes" : [
                                  "Lesson Guide"
                                ],
                                "boost" : 1.0
                              }
                            }
                          ],
                          "adjust_pure_negative" : true,
                          "boost" : 1.0,
                          "_name" : "attachment-types-filter"
                        }
                      },
                      {
                        "bool" : {
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
