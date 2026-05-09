package com.fish.chat.plugin.intellijplugin.service

import com.fish.chat.plugin.intellijplugin.model.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class ApiException(val code: Int, message: String) : Exception(message)

class FishChatApiClient(
    var serverUrl: String = "http://localhost:8080"
) {
    private val log = Logger.getInstance(FishChatApiClient::class.java)
    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    private val gson = Gson()

    var token: String = ""

    private fun apiUrl(path: String): String = "$serverUrl$path"

    private fun logRequest(method: String, path: String) {
        val msg = "[FishChat] --> $method $path  token=${if (token.isNotEmpty()) token.take(16)+"..." else "(empty)"}"
        log.info(msg)
        println(msg)
    }

    private fun logResponse(method: String, path: String, status: Int, body: String) {
        val preview = if (body.length > 300) body.take(300) + "..." else body
        val msg = "[FishChat] <-- $method $path  status=$status  body=$preview"
        log.info(msg)
        println(msg)
    }

    private inline fun <reified T> parseResult(json: String, method: String, path: String): T? {
        try {
            val type = object : TypeToken<ApiResult<T>>() {}.type
            val result: ApiResult<T> = gson.fromJson(json, type)
            if (result.code == 200) return result.data
            val errMsg = "[FishChat] API error: $method $path -> code=${result.code} message=${result.message}"
            log.warn(errMsg)
            println(errMsg)
            throw ApiException(result.code, result.message)
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            val msg = tryExtractErrorMessage(json)
            val errMsg = "[FishChat] Parse error for $method $path: ${e.message}, server says: $msg"
            log.warn(errMsg)
            println(errMsg)
            throw ApiException(-1, msg ?: "Response parse error: ${e.message}")
        }
    }

    private fun tryExtractErrorMessage(json: String): String? {
        return try {
            val obj = JsonParser.parseString(json).asJsonObject
            val error = obj.get("error")?.asString
            val message = obj.get("message")?.asString
            val status = obj.get("status")?.asInt
            when {
                !error.isNullOrEmpty() -> "[$status] $error"
                !message.isNullOrEmpty() -> message
                else -> null
            }
        } catch (e: Exception) { null }
    }

    // ---- Auth ----

    @Throws(ApiException::class)
    fun login(username: String, password: String): AuthDTO {
        val path = "/auth/login"
        val body = gson.toJson(LoginRequest(username, password))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        return parseResult<AuthDTO>(respBody, "POST", path)
            ?: throw ApiException(-1, "Login failed")
    }

    // ---- Friends ----

    fun getFriends(page: Int = 0, size: Int = 100): List<FriendDTO> {
        val path = "/friends?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ---- Groups ----

    fun getMyGroups(page: Int = 0, size: Int = 100): List<GroupDTO> {
        val path = "/groups/my?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ---- Channels ----

    fun getMyChannels(page: Int = 0, size: Int = 100): List<ChannelDTO> {
        val path = "/channels/my?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ---- Messages ----

    fun getHistory(roomCode: String, page: Int = 0, size: Int = 20): MessageHistoryData? {
        val encoded = java.net.URLEncoder.encode(roomCode, "UTF-8")
            .replace("+", "%20")  // URLEncoder encodes space as +, keep it URL-safe
        val path = "/messages/$encoded?page=$page&size=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return try {
            parseResult<MessageHistoryData>(respBody, "GET", path)
        } catch (e: ApiException) { null }
    }

    // ---- HTTP helpers ----

    private fun get(path: String): Pair<Int, String> {
        val req = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl(path)))
            .header("fish-token", token)
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build()
        val resp = http.send(req, HttpResponse.BodyHandlers.ofString())
        return resp.statusCode() to resp.body()
    }

    private fun post(path: String, body: String): Pair<Int, String> {
        val req = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl(path)))
            .header("Content-Type", "application/json")
            .header("fish-token", token)
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val resp = http.send(req, HttpResponse.BodyHandlers.ofString())
        return resp.statusCode() to resp.body()
    }

    private inline fun <reified T> parsePageResult(json: String, method: String, path: String): List<T> {
        return try {
            val type = object : TypeToken<ApiResult<PageResult<T>>>() {}.type
            val result: ApiResult<PageResult<T>> = gson.fromJson(json, type)
            if (result.code == 200) result.data?.data ?: emptyList()
            else {
                val msg = "[FishChat] Page API error: $method $path -> code=${result.code} message=${result.message}"
                log.warn(msg)
                println(msg)
                emptyList()
            }
        } catch (e: Exception) {
            val msg = "[FishChat] Page parse error for $method $path: ${e.message}"
            log.warn(msg)
            println(msg)
            emptyList()
        }
    }
}
