package com.cpen321.squadup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.cpen321.squadup.ui.viewmodels.NewsViewModel
import com.cpen321.squadup.data.remote.dto.HobbyNews
import com.cpen321.squadup.data.remote.dto.NewsArticle

@Composable
fun NewsScreen(
    newsViewModel: NewsViewModel,
    selectedHobbies: List<String>,
    modifier: Modifier = Modifier
) {
    val uiState by newsViewModel.uiState.collectAsState()

    LaunchedEffect(selectedHobbies) {
        if (selectedHobbies.isNotEmpty()) {
            newsViewModel.fetchNews(selectedHobbies)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "News",
                style = MaterialTheme.typography.headlineMedium
            )

            if (!uiState.isLoading) {
                Button(
                    onClick = { newsViewModel.fetchNews(selectedHobbies) }
                ) {
                    Text("Refresh")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                LoadingView()
            }

            uiState.error != null -> {
                ErrorView(
                    error = uiState.error!!,
                    onRetry = {
                        newsViewModel.clearError()
                        newsViewModel.fetchNews(selectedHobbies)
                    }
                )
            }

            uiState.newsData.isNotEmpty() -> {
                FormattedNewsView(newsData = uiState.newsData)
            }

            else -> {
                NoHobbiesView()
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading news...")
        }
    }
}

@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun NoHobbiesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hobbies selected",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Please select some hobbies to fetch news",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormattedNewsView(newsData: List<HobbyNews>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(newsData) { hobbyNews ->
            HobbyNewsCard(hobbyNews = hobbyNews)
        }
    }
}

@Composable
private fun HobbyNewsCard(hobbyNews: HobbyNews) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = hobbyNews.hobby,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (hobbyNews.articles.isEmpty()) {
                Text(
                    text = "No articles found for this hobby",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                hobbyNews.articles.forEach { article ->
                    NewsArticleItem(article = article)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun NewsArticleItem(article: NewsArticle) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(article.url) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatDate(article.publishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        if (parts.size == 3) {
            "${parts[1]}/${parts[2]}/${parts[0]}"
        } else {
            dateString
        }
    } catch (e: ArrayIndexOutOfBoundsException) {
        dateString
    }
}