package com.fish.chat.plugin.intellijplugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fish.chat.plugin.intellijplugin.AppState
import com.fish.chat.plugin.intellijplugin.Screen
import com.fish.chat.plugin.intellijplugin.service.ApiException
import com.fish.chat.plugin.intellijplugin.bindWsListeners
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

private val InputBg = Color(0xFF3C3F41)
private val InputBorder = Color(0xFF555555)
private val TextColor = Color(0xFFBBBBBB)
private val ErrorColor = Color(0xFFF44336)
private val DimColor = Color(0xFF777777)

@Composable
fun LoginPanel(state: AppState) {
    var server by remember { mutableStateOf(state.api.serverUrl) }
    var wsUrl by remember { mutableStateOf(state.wsUrl) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var connecting by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val inputStyle = TextStyle(color = TextColor, fontSize = 13.sp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Fish Chat", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Text("Server URL", color = DimColor)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = server,
            onValueChange = { server = it },
            textStyle = inputStyle,
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBg, RoundedCornerShape(4.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )

        Spacer(Modifier.height(12.dp))
        Text("WebSocket URL", color = DimColor)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = wsUrl,
            onValueChange = { wsUrl = it },
            textStyle = inputStyle,
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBg, RoundedCornerShape(4.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )

        Spacer(Modifier.height(12.dp))
        Text("Username", color = DimColor)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = username,
            onValueChange = { username = it },
            textStyle = inputStyle,
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBg, RoundedCornerShape(4.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )

        Spacer(Modifier.height(12.dp))
        Text("Password", color = DimColor)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = password,
            onValueChange = { password = it },
            textStyle = inputStyle,
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBg, RoundedCornerShape(4.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )

        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = {
                connecting = true
                statusMsg = "Connecting..."
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            state.api.serverUrl = server
                            state.wsUrl = wsUrl
                            val auth = state.api.login(username, password)
                            val token = auth.token ?: ""
                            state.api.token = token
                            state.currentUser = auth
                            state.saveCredentials(server, token, auth)

                            state.ws.connect(wsUrl, token)
                            bindWsListeners(state)
                        }
                        state.screen = Screen.CHAT_LIST
                    } catch (e: ApiException) {
                        statusMsg = e.message ?: "Login failed"
                    } catch (e: Exception) {
                        statusMsg = "Error: ${e.message}"
                    } finally {
                        connecting = false
                    }
                }
            },
            enabled = !connecting && username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (connecting) "Connecting..." else "Connect")
        }

        if (statusMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                statusMsg,
                color = if (statusMsg.contains("Error") || statusMsg.contains("failed")) ErrorColor else TextColor
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("v1.0", color = DimColor)
    }
}
