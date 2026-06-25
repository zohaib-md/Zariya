package com.project.zariya.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.compose.ui.graphics.Color

class ZariyaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ZariyaWidgetContent()
        }
    }

    @Composable
    private fun ZariyaWidgetContent() {
        val bgColor = ColorProvider(Color(0xFF161B22))
        val tealColor = ColorProvider(Color(0xFF00BFA6))
        val greyColor = ColorProvider(Color(0xFF8B949E))
        val whiteColor = ColorProvider(Color(0xFFE6EDF3))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .padding(16.dp)
                .cornerRadius(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Zariya",
                style = TextStyle(
                    color = tealColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "Next Dose",
                style = TextStyle(color = greyColor, fontSize = 12.sp)
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "No upcoming doses",
                style = TextStyle(
                    color = whiteColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today",
                    style = TextStyle(color = greyColor, fontSize = 12.sp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = "0/0 doses",
                    style = TextStyle(
                        color = tealColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
