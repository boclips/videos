package com.boclips.videos.service.presentation.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.bson.types.ObjectId


object CategoryMappingValidator {

    fun validate(file: ByteArray): CategoryValidationResult {
//        val reader = CsvMapper().enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE)
//        val schema = reader.schemaFor(CategoryMappingMetadata::class.java).withoutHeader().withColumnReordering(true)
//            .withNullValue("")
//        val items =
//            reader.reader(schema).readValues<CategoryMappingMetadata>(file)
//                .readAll()

//        val mapper = CsvMapper().enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);
//        val schema = mapper.schemaFor(CategoryMappingMetadata::class.java); // schema from 'Pojo' definition
//        val it: MappingIterator<CategoryMappingMetadata> =
//            mapper.readerFor(CategoryMappingMetadata::class.java).with(schema).readValues(file);
//        val all = it.readAll();
//        var result: CategoryValidationResult = CategoryValidationResult.Valid(all.size)

        val schema = CsvSchema.builder()
            .addColumn("Thema code (where possible)")
            .addColumn("ID")
            .build()
        val bootstrapSchema = CsvSchema.emptySchema().withHeader()
        val mapper: ObjectMapper = CsvMapper()
        val all = mapper.readerFor(Pojo::class.java).with(bootstrapSchema).readValue(csv)
        all.mapIndexed { index, categoryMappingMetadata ->
            {
                if (categoryMappingMetadata.videoId.isNullOrEmpty()) {
                    result = CategoryValidationResult.MissingVideoId(rowIndex = index)
                } else {
                    try {
                        ObjectId(categoryMappingMetadata.videoId.toString())
                    } catch (ex: IllegalArgumentException) {
                        result = CategoryValidationResult.InvalidVideoId(
                            rowIndex = index,
                            invalidId = categoryMappingMetadata.videoId!!
                        )
                    }
                }

            }
        }
        return result
    }
}
