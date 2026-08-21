package com.ooredoost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ooredoost.app.ui.MainViewModel
import com.ooredoost.app.ui.theme.CyberBlue
import com.ooredoost.app.ui.theme.CyberGreen
import com.ooredoost.app.ui.theme.DarkBackground
import com.ooredoost.app.ui.theme.DarkSurface
import com.ooredoost.app.ui.theme.DarkSurfaceElevated
import com.ooredoost.app.ui.theme.GlassBorder
import com.ooredoost.app.ui.theme.GoldenYellow
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.TextMuted
import com.ooredoost.app.ui.theme.TextPrimary
import com.ooredoost.app.ui.theme.TextSecondary

@Composable
fun SetupScreen(
    viewModel: MainViewModel,
    onSetupComplete: () -> Unit
) {
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "🔧",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "إعداد الصلاحيات",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "خطوة واحدة فقط عبر الكمبيوتر (مرة واحدة)",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Permission Status
        if (hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, CyberGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "✅ الصلاحية ممنوحة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                        Text(
                            text = "التطبيق جاهز للاستخدام",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.setSetupCompleted()
                    onSetupComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OoredooRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "متابعة إلى التطبيق",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Setup Steps
            SetupStep(
                number = 1,
                icon = Icons.Rounded.PhoneAndroid,
                title = "تفعيل خيارات المطور",
                description = "اذهب إلى الإعدادات → حول الهاتف → اضغط على \"رقم البناء\" 7 مرات",
                color = GoldenYellow
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupStep(
                number = 2,
                icon = Icons.Rounded.Usb,
                title = "تفعيل تصحيح USB",
                description = "اذهب إلى الإعدادات → خيارات المطور → تصحيح USB (USB Debugging) → تفعيل",
                color = CyberBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupStep(
                number = 3,
                icon = Icons.Rounded.Terminal,
                title = "تشغيل أمر ADB",
                description = "وصّل الجوال بالكمبيوتر وافتح Terminal واكتب الأمر التالي:",
                color = CyberGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ADB Command Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, CyberGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "adb shell pm grant com.ooredoost.app android.permission.WRITE_SECURE_SETTINGS",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            textDirection = TextDirection.Ltr
                        ),
                        color = CyberGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "adb shell pm grant com.ooredoost.app android.permission.MODIFY_PHONE_STATE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            textDirection = TextDirection.Ltr
                        ),
                        color = CyberBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Check Permission Button
            Button(
                onClick = { viewModel.checkPermission() },
                colors = ButtonDefaults.buttonColors(containerColor = OoredooRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "تحقق من الصلاحية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note about Infinix
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CyberBlue.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "💡 ملاحظة لمستخدمي Infinix Hot 50i",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تأكد من تحميل ADB Driver المناسب لجهاز Infinix. يمكنك تحميله من موقع Infinix الرسمي أو استخدام Universal ADB Driver.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupStep(
    number: Int,
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
