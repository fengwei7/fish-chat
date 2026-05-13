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
import java.nio.file.Path
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

    // ==================== Auth ====================

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

    @Throws(ApiException::class)
    fun register(request: RegisterRequest): Boolean {
        val path = "/auth/register"
        val body = gson.toJson(request)
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        return parseResult<Boolean>(respBody, "POST", path) ?: false
    }

    @Throws(ApiException::class)
    fun logout() {
        val path = "/auth/logout"
        logRequest("POST", path)
        val (status, respBody) = post(path, "")
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    // ==================== User ====================

    @Throws(ApiException::class)
    fun getProfile(): UserDTO? {
        val path = "/user/profile"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parseResult<UserDTO>(respBody, "GET", path)
    }

    @Throws(ApiException::class)
    fun updateProfile(request: UpdateProfileRequest): UserDTO? {
        val path = "/user/profile"
        val body = gson.toJson(request)
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        return parseResult<UserDTO>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun getUserByCode(code: String): UserDTO? {
        val path = "/user/$code"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parseResult<UserDTO>(respBody, "GET", path)
    }

    fun getOnlineUsers(page: Int = 0, size: Int = 20): List<String> {
        val path = "/user/online?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    fun searchUsers(keyword: String, page: Int = 0, size: Int = 20): List<UserDTO> {
        val encoded = java.net.URLEncoder.encode(keyword, "UTF-8").replace("+", "%20")
        val path = "/user/search?keyword=$encoded&pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ==================== Friends ====================

    fun getFriends(page: Int = 0, size: Int = 100): List<FriendDTO> {
        val path = "/friends?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    @Throws(ApiException::class)
    fun addFriend(friendCode: String, remark: String? = null) {
        val path = "/friends"
        val body = gson.toJson(FriendRequest(friendCode, remark))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun acceptFriend(friendCode: String) {
        val path = "/friends/accept"
        val body = gson.toJson(FriendAcceptRequest(friendCode))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun removeFriend(friendCode: String) {
        val path = "/friends/remove"
        val body = gson.toJson(FriendRemoveRequest(friendCode))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    fun getFriendRequests(page: Int = 0, size: Int = 20): List<FriendDTO> {
        val path = "/friends/requests?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    fun searchFriends(keyword: String, page: Int = 0, size: Int = 20): List<FriendDTO> {
        val encoded = java.net.URLEncoder.encode(keyword, "UTF-8").replace("+", "%20")
        val path = "/friends/search?keyword=$encoded&pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ==================== Groups ====================

    @Throws(ApiException::class)
    fun createGroup(name: String, avatar: String? = null): GroupDTO? {
        val path = "/groups"
        val body = gson.toJson(CreateGroupRequest(name, avatar))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        return parseResult<GroupDTO>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun getGroup(code: String): GroupDTO? {
        val path = "/groups/$code"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parseResult<GroupDTO>(respBody, "GET", path)
    }

    @Throws(ApiException::class)
    fun dismissGroup(code: String) {
        val path = "/groups/$code/dismiss"
        logRequest("POST", path)
        val (status, respBody) = post(path, "")
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun addGroupMember(groupCode: String, userCode: String) {
        val path = "/groups/$groupCode/members"
        val body = gson.toJson(GroupMemberRequest(userCode))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun removeGroupMember(groupCode: String, userCode: String) {
        val path = "/groups/$groupCode/members/remove"
        val body = gson.toJson(GroupMemberRequest(userCode))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    fun getMyGroups(page: Int = 0, size: Int = 100): List<GroupDTO> {
        val path = "/groups/my?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    fun searchGroups(keyword: String, page: Int = 0, size: Int = 20): List<GroupDTO> {
        val encoded = java.net.URLEncoder.encode(keyword, "UTF-8").replace("+", "%20")
        val path = "/groups/search?keyword=$encoded&pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ==================== Channels ====================

    @Throws(ApiException::class)
    fun createChannel(name: String, avatar: String? = null, description: String? = null): ChannelDTO? {
        val path = "/channels"
        val body = gson.toJson(CreateChannelRequest(name, avatar, description))
        logRequest("POST", path)
        val (status, respBody) = post(path, body)
        logResponse("POST", path, status, respBody)
        return parseResult<ChannelDTO>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun getChannel(code: String): ChannelDTO? {
        val path = "/channels/$code"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parseResult<ChannelDTO>(respBody, "GET", path)
    }

    @Throws(ApiException::class)
    fun subscribeChannel(code: String) {
        val path = "/channels/$code/subscribe"
        logRequest("POST", path)
        val (status, respBody) = post(path, "")
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    @Throws(ApiException::class)
    fun unsubscribeChannel(code: String) {
        val path = "/channels/$code/unsubscribe"
        logRequest("POST", path)
        val (status, respBody) = post(path, "")
        logResponse("POST", path, status, respBody)
        parseResult<Any?>(respBody, "POST", path)
    }

    fun getMyChannels(page: Int = 0, size: Int = 100): List<ChannelDTO> {
        val path = "/channels/my?pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    fun searchChannels(keyword: String, page: Int = 0, size: Int = 20): List<ChannelDTO> {
        val encoded = java.net.URLEncoder.encode(keyword, "UTF-8").replace("+", "%20")
        val path = "/channels/search?keyword=$encoded&pageNum=$page&pageSize=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return parsePageResult(respBody, "GET", path)
    }

    // ==================== Messages ====================

    fun getHistory(roomCode: String, page: Int = 0, size: Int = 20): MessageHistoryData? {
        val encoded = java.net.URLEncoder.encode(roomCode, "UTF-8")
            .replace("+", "%20")
        val path = "/messages/$encoded?page=$page&size=$size"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return try {
            parseResult<MessageHistoryData>(respBody, "GET", path)
        } catch (e: ApiException) { null }
    }

    fun syncMessages(roomCode: String, after: Long): List<ChatMessageDTO> {
        val encoded = java.net.URLEncoder.encode(roomCode, "UTF-8")
            .replace("+", "%20")
        val path = "/messages/$encoded/sync?after=$after"
        logRequest("GET", path)
        val (status, respBody) = get(path)
        logResponse("GET", path, status, respBody)
        return try {
            parseResult<List<ChatMessageDTO>>(respBody, "GET", path) ?: emptyList()
        } catch (e: ApiException) { emptyList() }
    }

    // ==================== File ====================

    @Throws(ApiException::class)
    fun uploadFile(filePath: Path): UploadFileResponse? {
        val path = "/file/upload"
        val boundary = "----FishChatBoundary${System.currentTimeMillis()}"

        val fileBytes = java.nio.file.Files.readAllBytes(filePath)
        val fileName = filePath.fileName.toString()

        val bodyBuilder = StringBuilder()
        bodyBuilder.append("--$boundary\r\n")
        bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
        bodyBuilder.append("Content-Type: application/octet-stream\r\n\r\n")

        val headerBytes = bodyBuilder.toString().toByteArray(Charsets.UTF_8)
        val footerBytes = "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)

        val bodyBytes = headerBytes + fileBytes + footerBytes

        val req = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl(path)))
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .header("fish-token", token)
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
            .build()

        logRequest("POST", path)
        val resp = http.send(req, HttpResponse.BodyHandlers.ofString())
        val respBody = resp.body()
        logResponse("POST", path, resp.statusCode(), respBody)

        return parseResult<UploadFileResponse>(respBody, "POST", path)
    }

    fun downloadFile(fileName: String): ByteArray? {
        val encoded = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        val path = "/file/download/$encoded"
        logRequest("GET", path)
        val req = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl(path)))
            .header("fish-token", token)
            .timeout(Duration.ofSeconds(60))
            .GET()
            .build()
        val resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray())
        logResponse("GET", path, resp.statusCode(), "<binary ${resp.body().size} bytes>")
        return if (resp.statusCode() == 200) resp.body() else null
    }

    // ==================== HTTP helpers ====================

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
