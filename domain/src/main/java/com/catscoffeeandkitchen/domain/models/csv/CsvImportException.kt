package com.catscoffeeandkitchen.domain.models.csv

sealed class CsvImportException(message: String): RuntimeException(message)

class DatabaseError(message: String): CsvImportException(message)
class CsvFormatError(message: String): CsvImportException(message)

