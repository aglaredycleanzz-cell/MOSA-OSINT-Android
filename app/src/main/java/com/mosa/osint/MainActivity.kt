package com.mosa.osint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080D12)
private val Panel = Color(0xFF111B23)
private val Accent = Color(0xFF35D0AE)
private val Muted = Color(0xFF91A4B2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MosaApp() }
    }
}

@Composable
fun MosaApp() {
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("اختر وحدة للبدء") }
    var selected by remember { mutableStateOf("Phone intelligence") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(containerColor = Bg) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Accent.copy(alpha = .13f), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Shield, null, tint = Accent,
                            modifier = Modifier.padding(12.dp).size(30.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("MOSA OSINT", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("ANDROID INTELLIGENCE WORKSPACE", color = Muted, fontSize = 10.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("v0.1", color = Accent, fontSize = 12.sp)
                }
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("OSINT WORKSPACE", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("تحليل المعلومات المتاحة علناً", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("استخدم مصادر عامة ومصرحاً بها. لا يصل التطبيق إلى بيانات واتساب الخاصة ولا يتجاوز إعدادات الخصوصية.",
                            color = Muted, fontSize = 13.sp)
                    }
                }
                Text("INTELLIGENCE MODULES", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                ModuleCard("Phone intelligence", "التحقق المحلي من بنية الرقم", Icons.Default.Phone, selected == "Phone intelligence") {
                    selected = "Phone intelligence"; message = "أدخل الرقم بصيغة دولية."
                }
                ModuleCard("Public profile research", "تنظيم البحث عن الحسابات العامة", Icons.Default.PersonSearch, selected == "Public profile research") {
                    selected = "Public profile research"; message = "وحدة المصادر العامة ستضاف في مرحلة لاحقة."
                }
                ModuleCard("Business verification", "مراجعة المعلومات العامة للأعمال", Icons.Default.Business, selected == "Business verification") {
                    selected = "Business verification"; message = "وحدة التحقق من الأعمال قيد التطوير."
                }
                ModuleCard("Privacy audit", "قائمة تحقق لإعدادات الخصوصية", Icons.Default.Security, selected == "Privacy audit") {
                    selected = "Privacy audit"; message = "وحدة تدقيق الخصوصية قيد التطوير."
                }
                if (selected == "Phone intelligence") {
                    Text("SEARCH INPUT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رقم الهاتف مع مفتاح الدولة") },
                        placeholder = { Text("+968XXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, focusedLabelColor = Accent, cursorColor = Accent
                        )
                    )
                }
                Button(
                    onClick = {
                        message = if (selected == "Phone intelligence") {
                            val cleaned = phone.filter { it.isDigit() || it == '+' }
                            when {
                                cleaned.isBlank() -> "أدخل رقم هاتف أولاً."
                                !cleaned.startsWith("+") -> "استخدم الصيغة الدولية، مثال: +968XXXXXXXX."
                                cleaned.count { it.isDigit() } !in 8..15 -> "تحقق من عدد أرقام الهاتف."
                                else -> "صيغة الرقم مقبولة مبدئياً. لم يتم البحث عن هوية صاحبه."
                            }
                        } else message
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
                ) {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("RUN CHECK", fontWeight = FontWeight.Bold)
                }
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("ACTIVITY", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(message, color = Color.White, fontSize = 14.sp)
                        Text("التحقق الحالي محلي من تنسيق الرقم فقط ولا يثبت هوية صاحبه.",
                            color = Muted, fontSize = 11.sp)
                    }
                }
                Text("MOSA OSINT • LOCAL PROTOTYPE", color = Muted, fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (active) Color(0xFF17332F) else Panel),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}
