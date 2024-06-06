package com.stream.baba.data

sealed interface Resource<T>{
    data class Success<T>(val value: T) : Resource<T>
    data class Failure(val errorString: String) : Resource<Nothing>
    object Loading : Resource<Nothing>
}