package com.example.graphqlclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.graphqlclient.Client.apolloClient
import com.example.graphqlclient.ui.theme.GraphQLClientTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // MutableState to hold the GraphQL data
    private val characterNames = mutableStateOf<List<String>>(emptyList())
    private val isLoading = mutableStateOf<Boolean>(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphQLClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val scope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Button to trigger API request
                        Button(
                            onClick = {
                                scope.launch {
                                    fetchData()
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
                                    Text("Fetch Data")
                                }
                            }
                        }

                        // Display the list when data is available
                        if (characterNames.value.isNotEmpty()) {
                            GraphQLList(characterNames.value)
                        }
                    }
                }
            }
        }
    }

    // Composable function to display the list of character names
    @Composable
    fun GraphQLList(names: List<String>) {
        LazyColumn {
            items(names.size) { index ->
                Text(text = names[index])
            }
        }
    }

    // Function to trigger the GraphQL API request

    private suspend fun fetchData() {
        isLoading.value = true
        val response = apolloClient.query(Query()).execute()

        // Extract character names from the response
        val names = response.data?.characters?.results?.mapNotNull { it?.name } ?: emptyList()

        // Update the MutableState with character names
        characterNames.value = names

        Log.d("GraphQLClient", "Success ${response.data}")
        isLoading.value = false
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