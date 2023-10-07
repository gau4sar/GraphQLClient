package com.example.graphqlclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.graphqlclient.Client.apolloClient
import com.example.graphqlclient.ui.theme.GraphQLClientTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // MutableState to hold the GraphQL data
    private val characterNames = mutableStateOf<List<String>>(emptyList())
    private val isLoading = mutableStateOf<Boolean>(false)
    private val token = mutableStateOf("")
    private val isLogin = mutableStateOf<Boolean>(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphQLClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    if (isLogin.value) {
                        ShowTokenPage()
                    } else {
                        LoginScreen()
                    }
                }
            }
        }
    }

    @Composable
    private fun ShowTokenPage() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "token : " + token.value,
            )

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLogin.value = performLogin(email)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading.value // Disable the button while loading
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp))
                    } else {
                        Text("Login")
                    }
                }
            }
        }
    }

    private suspend fun performLogin(email: String): Boolean {
        val response = try {
            isLoading.value = true
            apolloClient.mutation(LoginMutation(email = email)).execute()
        } catch (e: ApolloException) {
            Log.w("Login", "Failed to login", e)

            showToast("Failed to login $e")
            return false
        } finally {
            isLoading.value = false
        }
        if (response.hasErrors()) {
            Log.w("Login", "Failed to login: ${response.errors?.get(0)?.message}")

            showToast("Failed to login: ${response.errors?.get(0)?.message}")
            return false
        }
        response.data?.login?.token?.let {
            token.value = it
        }
        if (token.value.isEmpty()) {
            showToast("Failed to login: no token returned by the backend")
            Log.w("Login", "Failed to login: no token returned by the backend")
            return false
        }

        return true
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GraphQLClientTheme {
        Greeting("Android")
    }
}