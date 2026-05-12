package com.example.posts

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

class PostRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }
    }

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): List<Post> {
        try {
            val response = client.get("https://jsonplaceholder.typicode.com/posts") {
                parameter("_page", page)
                parameter("_limit", limit)
                if (userId != null) {
                    parameter("userId", userId)
                }
            }

            return when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.NotFound -> throw Exception("Posts não encontrados (404)")
                HttpStatusCode.InternalServerError -> throw Exception("Erro no servidor (500)")
                else -> throw Exception("Erro inesperado: ${response.status.value}")
            }
        } catch (e: RedirectResponseException) {
            throw Exception("Erro de redirecionamento: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            throw Exception("Erro no cliente: ${e.response.status.description}")
        } catch (e: ServerResponseException) {
            throw Exception("Erro no servidor: ${e.response.status.description}")
        } catch (e: HttpRequestTimeoutException) {
            throw Exception("Tempo de conexão esgotado. Verifique sua internet.")
        } catch (e: IOException) {
            throw Exception("Falha na conexão. Verifique sua internet.")
        } catch (e: Exception) {
            throw e // Re-lança exceções já tratadas (404, 500, etc.)
        }
    }
}

