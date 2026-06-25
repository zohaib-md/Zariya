package com.project.zariya.feature.profile.presentation.add_edit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.theme.ZariyaGradientStart
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val listState = rememberLazyListState()
    
    // Find initial index
    val initialIndex = remember { items.indexOf(selectedValue).takeIf { it >= 0 } ?: 0 }
    
    LaunchedEffect(initialIndex) {
        listState.scrollToItem(initialIndex)
    }

    val itemHeight = 64.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val visibleItems = 5
    val height = itemHeight * visibleItems

    // Update value when scrolling stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val snappedIndex = listState.firstVisibleItemIndex
            if (snappedIndex in items.indices) {
                val newValue = items[snappedIndex]
                if (newValue != selectedValue) {
                    onValueChange(newValue)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2)),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val text = items[index]
                
                val isCenter by remember {
                    derivedStateOf {
                        index == listState.firstVisibleItemIndex
                    }
                }
                
                val fontSize = if (isCenter) 36.sp else 24.sp
                val fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal
                val color = if (isCenter) ZariyaPrimary else ZariyaTextSecondary.copy(alpha = 0.4f)
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = color
                    )
                }
            }
        }
        
        // Add label next to the center item
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = ZariyaTextPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 60.dp, y = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalRulerPicker(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    unit: String,
    modifier: Modifier = Modifier
) {
    val items = range.toList()
    val listState = rememberLazyListState()
    
    val initialIndex = remember { items.indexOf(selectedValue).takeIf { it >= 0 } ?: 0 }
    
    LaunchedEffect(initialIndex) {
        listState.scrollToItem(initialIndex)
    }

    val itemWidth = 24.dp
    val itemWidthPx = with(LocalDensity.current) { itemWidth.toPx() }
    
    var componentWidth by remember { mutableStateOf(0) }
    val visibleItemsCount = if (componentWidth > 0) (componentWidth / itemWidthPx).toInt() else 11
    val halfWidth = visibleItemsCount / 2
    
    // Update value when scrolling stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && componentWidth > 0) {
            val snappedIndex = listState.firstVisibleItemIndex
            if (snappedIndex in items.indices) {
                val newValue = items[snappedIndex]
                if (newValue != selectedValue) {
                    onValueChange(newValue)
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value Display
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = selectedValue.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = ZariyaTextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = ZariyaTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .onSizeChanged { componentWidth = it.width },
            contentAlignment = Alignment.TopCenter
        ) {
            // Center indicator
            Canvas(modifier = Modifier.fillMaxHeight().width(4.dp).align(Alignment.TopCenter)) {
                drawLine(
                    color = ZariyaGradientStart,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height * 0.6f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            
            LazyRow(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                contentPadding = PaddingValues(horizontal = itemWidth * halfWidth),
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                items(items.size) { index ->
                    val value = items[index]
                    val isMultipleOfTen = value % 10 == 0
                    val isMultipleOfFive = value % 5 == 0
                    
                    val lineLength = when {
                        isMultipleOfTen -> 60.dp
                        isMultipleOfFive -> 40.dp
                        else -> 24.dp
                    }
                    
                    val color = if (isMultipleOfTen) ZariyaTextPrimary else ZariyaTextSecondary.copy(alpha = 0.5f)
                    val strokeWidth = if (isMultipleOfTen) 2.dp else 1.dp
                    
                    Column(
                        modifier = Modifier.width(itemWidth),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Canvas(modifier = Modifier.height(lineLength).fillMaxWidth()) {
                            drawLine(
                                color = color,
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = strokeWidth.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        
                        if (isMultipleOfTen || isMultipleOfFive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = value.toString(),
                                fontSize = 12.sp,
                                color = if (value == selectedValue) ZariyaGradientStart else color,
                                fontWeight = if (value == selectedValue) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
