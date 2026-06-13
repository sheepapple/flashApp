package com.example.flashappcs4520.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flashappcs4520.common.Article
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme
import java.net.URI

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Header: title + "source · date" meta line (padded).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = article.webTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = metaLine(article),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Full-bleed image (no horizontal padding so it spans edge to edge).
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.webTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            )

            // Summary at the bottom (placeholder until the Gemini step fills it in).
            Text(
                text = article.summary ?: "Summary coming soon…",
                fontSize = 18.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

/** Builds a "source · date" line, deriving the source from the article URL's host. */
private fun metaLine(article: Article): String {
    val source = runCatching { URI(article.webUrl).host?.removePrefix("www.") }
        .getOrNull() ?: "News"
    return "$source · ${article.formattedDate()}"
}

@Preview(showBackground = true)
@Composable
fun ArticleCardPreview() {
    val mockArticle = Article(
        webTitle = "World Cup football and T20 cricket galore, plus F1 in Barcelona – follow with us ",
        summary = null,
        imageUrl = "https://media.guim.co.uk/fdc906e59f18b1f42ac9263c76f7c5da318bf8ee/0_0_5000_4000/500.jpg",
        webUrl = "https://www.theguardian.com/sport/2026/jun/12/world-cup-football-and-t20-cricket-galore-plus-f1-in-barcelona-follow-with-us",
        publishedAt = "2026-06-12 16:41:25+00"
    )
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleCard(article = mockArticle)
        }
    }
}