package com.example.flashappcs4520

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashappcs4520.ui.ArticleViewModel
import com.example.flashappcs4520.ui.FeedScreen
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashAppCS4520Theme {
                val articleViewModel: ArticleViewModel = viewModel()
                FeedScreen(articleViewModel = articleViewModel)
            }
        }
    }
}