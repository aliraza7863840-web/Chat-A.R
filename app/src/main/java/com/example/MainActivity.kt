package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.paisachat.ui.Chat3DActivityScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    if (FirebaseApp.getApps(this).isEmpty()) {
      val options = FirebaseOptions.Builder()
        .setApplicationId("1:1082531818166:android:c39d6e51285866fa268")
        .setProjectId("my-chat-5a268")
        .setDatabaseUrl("https://my-chat-5a268-default-rtdb.firebaseio.com/")
        .setStorageBucket("my-chat-5a268.appspot.com")
        .setApiKey("AIzaSyD-paisachat-custom-live-db") // dummy API key to satisfy SDK initialization requirements
        .build()
      FirebaseApp.initializeApp(this, options)
    }
    
    enableEdgeToEdge()
    setContent {
      Chat3DActivityScreen(onBackPressed = { finish() })
    }
  }
}

