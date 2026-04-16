package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.OrangePrimary
import com.example.myapplication.ui.theme.StarYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class FoodItem(
    val id: Int,
    val name: String,
    val price: String,
    val rating: Float,
    val description: String,
    val category: String,
    val ingredients: List<String> = listOf("Fresh ingredients", "Organic spices", "Chef's secret sauce")
)

val foodList = listOf(
    FoodItem(1, "Classic Cheeseburger", "$12.99", 4.5f, "Juicy beef patty with melted cheddar, crisp lettuce, tomato, and our signature sauce on a toasted brioche bun.", "Burgers"),
    FoodItem(2, "Margherita Pizza", "$14.50", 4.8f, "Traditional thin-crust pizza topped with fresh mozzarella, sun-ripened tomatoes, and organic basil leaves.", "Pizza"),
    FoodItem(3, "Grilled Salmon", "$22.00", 4.7f, "Atlantic salmon fillet grilled to perfection, served with seasonal roasted vegetables and lemon butter sauce.", "Main"),
    FoodItem(4, "Caesar Salad", "$10.99", 4.2f, "Crisp romaine hearts tossed in creamy Caesar dressing with garlic croutons and shaved Parmesan cheese.", "Salads"),
    FoodItem(5, "Pasta Carbonara", "$16.50", 4.6f, "Classic Roman pasta with crispy pancetta, black pepper, and a rich pecorino egg sauce.", "Pasta"),
    FoodItem(6, "Sushi Platter", "$28.00", 4.9f, "Premium selection of nigiri and maki rolls, including spicy tuna, California roll, and fresh salmon.", "Japanese")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                
                LaunchedEffect(Unit) {
                    delay(2000)
                    currentScreen = "home"
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                if (targetState == "detail" || initialState == "detail") {
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    fadeIn() togetherWith fadeOut()
                                }
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                "splash" -> SplashScreen()
                                "home" -> RestaurantHomeScreen(
                                    onFoodSelected = { food ->
                                        selectedFood = food
                                        currentScreen = "detail"
                                    }
                                )
                                "detail" -> selectedFood?.let { food ->
                                    FoodDetailScreen(
                                        food = food,
                                        onBack = { currentScreen = "home" },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(OrangePrimary, Color(0xFFFF8A65))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🍴",
                        fontSize = 60.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Gourmet Express",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantHomeScreen(onFoodSelected: (FoodItem) -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    var pendingFood by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MENU", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = OrangePrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Popular Dishes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(foodList) { food ->
                    FoodCard(food = food) {
                        pendingFood = food
                        isLoading = true
                    }
                }
            }

            if (isLoading) {
                LoadingOverlay(foodName = pendingFood?.name ?: "") {
                    isLoading = false
                    pendingFood?.let { onFoodSelected(it) }
                }
            }
        }
    }
}

@Composable
fun FoodCard(food: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(food.category) {
                        "Burgers" -> "🍔"
                        "Pizza" -> "🍕"
                        "Main" -> "🥩"
                        "Salads" -> "🥗"
                        "Pasta" -> "🍝"
                        "Japanese" -> "🍣"
                        else -> "🍲"
                    },
                    fontSize = 80.sp
                )
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = OrangePrimary
                ) {
                    Text(
                        text = food.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        text = food.price,
                        style = MaterialTheme.typography.titleMedium,
                        color = OrangePrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = food.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBar(rating = food.rating)
                    
                    Surface(
                        shape = CircleShape,
                        color = OrangePrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", color = OrangePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodDetailScreen(food: FoodItem, onBack: () -> Unit, snackbarHostState: SnackbarHostState) {
    BackHandler(onBack = onBack)
    val scope = rememberCoroutineOwnerScope()
    var userRating by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when(food.category) {
                    "Burgers" -> "🍔"
                    "Pizza" -> "🍕"
                    "Main" -> "🥩"
                    "Salads" -> "🥗"
                    "Pasta" -> "🍝"
                    "Japanese" -> "🍣"
                    else -> "🍲"
                },
                fontSize = 120.sp
            )
            
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = food.price,
                    style = MaterialTheme.typography.headlineSmall,
                    color = OrangePrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingBar(rating = food.rating)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Free Delivery",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Green.copy(alpha = 0.7f),
                    modifier = Modifier
                        .background(Color.Green.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "About this dish",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = food.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            food.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(OrangePrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(ingredient, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Rating Section
            Text(
                "Rate this dish",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingBarInteractive(
                rating = userRating,
                onRatingChanged = { 
                    userRating = it
                    scope.launch {
                        snackbarHostState.showSnackbar("Thank you for rating: $it stars!")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { 
                    scope.launch {
                        snackbarHostState.showSnackbar("${food.name} added to cart!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Add to Cart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun rememberCoroutineOwnerScope() = rememberCoroutineScope()

@Composable
fun RatingBar(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = StarYellow,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = rating.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
    }
}

@Composable
fun RatingBarInteractive(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating) StarYellow else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(index + 1) }
            )
        }
    }
}

@Composable
fun LoadingOverlay(foodName: String, onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = OrangePrimary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Preparing",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantHomeScreenPreview() {
    MyApplicationTheme {
        RestaurantHomeScreen(onFoodSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FoodCardPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodCard(food = foodList[0], onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodDetailScreenPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    MyApplicationTheme {
        FoodDetailScreen(food = foodList[0], onBack = {}, snackbarHostState = snackbarHostState)
    }
}
