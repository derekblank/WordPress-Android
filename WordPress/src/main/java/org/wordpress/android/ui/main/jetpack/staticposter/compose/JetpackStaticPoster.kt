package org.wordpress.android.ui.main.jetpack.staticposter.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wordpress.android.R
import org.wordpress.android.ui.compose.components.MainTopAppBar
import org.wordpress.android.ui.compose.components.NavigationIcons
import org.wordpress.android.ui.compose.components.PrimaryButton
import org.wordpress.android.ui.compose.components.SecondaryButton
import org.wordpress.android.ui.compose.theme.AppTheme
import org.wordpress.android.ui.compose.theme.JpColorPalette
import org.wordpress.android.ui.compose.utils.uiStringText
import org.wordpress.android.ui.main.jetpack.staticposter.UiData
import org.wordpress.android.ui.main.jetpack.staticposter.UiState
import org.wordpress.android.ui.main.jetpack.staticposter.toContentUiState

@Composable
fun JetpackStaticPoster(
    uiState: UiState.Content,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) = with(uiState) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                MainTopAppBar(
                    title = null,
                    navigationIcon = NavigationIcons.BackIcon.takeIf { showTopBar },
                    onNavigationIconClick = onBackClick,
                )
            }
        },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth(),
            ) {
                Image(
                    painterResource(R.drawable.ic_wordpress_jetpack_logo),
                    stringResource(R.string.icon_desc),
                    Modifier.height(65.dp)
                )
                Text(
                    stringResource(R.string.wp_jp_static_poster_title, uiStringText(featureName)),
                    style = MaterialTheme.typography.h1.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                )
                Text(
                    stringResource(R.string.wp_jp_static_poster_message),
                    style = MaterialTheme.typography.body1.copy(fontSize = 17.sp),
                )
                Text(
                    stringResource(R.string.wp_jp_static_poster_footnote),
                    style = MaterialTheme.typography.body1.copy(colorResource(R.color.gray_50), 17.sp),
                )
            }
            PrimaryButton(
                stringResource(R.string.wp_jp_static_poster_button_primary),
                onPrimaryClick,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = JpColorPalette().primary,
                    contentColor = JpColorPalette().onPrimary,
                ),
                padding = PaddingValues(bottom = 15.dp),
                textStyle = MaterialTheme.typography.body1.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            )
            SecondaryButton(
                stringResource(R.string.wp_jp_static_poster_button_secondary),
                onSecondaryClick,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = JpColorPalette().primary,
                ),
                padding = PaddingValues(0.dp),
                textStyle = MaterialTheme.typography.body1.copy(fontSize = 17.sp),
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painterResource(R.drawable.ic_external_v2),
                    stringResource(R.string.icon_desc),
                    tint = colorResource(R.color.jetpack_green_40)
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewJetpackStaticPoster() {
    AppTheme {
        Box {
            val uiState = UiData.STATS.toContentUiState()
            JetpackStaticPoster(uiState)
        }
    }
}
