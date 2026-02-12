package com.seyone22.cook.ui.screen.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seyone22.cook.R
import com.seyone22.cook.data.model.RecipeImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderImage(
    modifier: Modifier = Modifier,
    images: List<RecipeImage?>, title: String
) {
    if (images.isEmpty()) {
        val image: Painter = painterResource(id = R.drawable.placeholder)
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { images.size },
            preferredItemWidth = 600.dp,
            itemSpacing = 16.dp,
            modifier = modifier.padding(bottom = 16.dp)
        ) { i ->
            AsyncImage(
                model = images[i]?.imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
            )
        }
    }
}
