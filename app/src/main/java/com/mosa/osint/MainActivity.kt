package com.mosa.osint

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.IDN
import java.net.URI
import java.util.Locale

private val Bg = Color(0xFF080D12)
private val Panel = Color(0xFF111B23)
private val Panel2 = Color(0xFF172630)
private val Accent = Color(0xFF35D0AE)
private val Muted = Color(0xFF91A4B2)
private val Warn = Color(0xFFFFC36B)
private val Red = Color(0xFFFF7979)

private enum class Module(val title: String, val subtitle: String) {
    HOME("الرئيسية", "مساحة العمل"),
    PHONE("رقم الهاتف", "فحص الصيغة الدولية"),
    USERNAME("اسم المستخدم", "بحث موجّه في المصادر العامة"),
    DOMAIN("النطاق", "معلومات النطاق العامة"),
    URL("تحليل الرابط", "فحص مؤشرات الروابط"),
    BUSINESS("الأعمال", "البحث عن بيانات الأعمال العامة"),
    PRIVACY("الخصوصية", "قائمة حماية حسابك"),
    REPORT("التقارير", "آخر نتيجة محفوظة")
}

private data class SearchLink(val label: String, val url: String)
private data class AnalysisResult(val heading: String, val lines: List<String>, val caution: String? = null)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MosaApp() }
    }
}

@Composable
private fun MosaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mosa_osint", Context.MODE_PRIVATE) }
    var module by remember { mutableStateOf(Module.HOME) }
    var input by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AnalysisResult?>(null) }
    var links by remember { mutableStateOf<List<SearchLink>>(emptyList()) }
    var report by remember { mutableStateOf(prefs.getString("last_report", "") ?: "") }
    val privacyItems = listOf(
        "تفعيل التحقق بخطوتين في واتساب",
        "مراجعة الأجهزة المرتبطة وتسجيل الخروج من غير المعروف",
        "قصر ظهور الصورة الشخصية على جهات الاتصال",
        "تقييد آخر ظهور وحالة الاتصال",
        "تحديد من يمكنه إضافتي إلى المجموعات",
        "تفعيل قفل التطبيق بالبصمة أو رمز الجهاز",
        "مراجعة تشفير النسخ الاحتياطية",
        "عدم مشاركة رمز التسجيل أو رمز PIN مع أي شخص"
    )
    val checked = remember {
        mutableStateMapOf<String, Boolean>().apply {
            privacyItems.forEachIndexed { index, label ->
                this[label] = prefs.getBoolean("privacy_$index", false)
            }
        }
    }

    fun saveReport(text: String) {
        report = text
        prefs.edit().putString("last_report", text).apply()
    }

    fun resetModule(next: Module) {
        module = next
        input = ""
        result = null
        links = emptyList()
        consent = false
        if (next == Module.REPORT) {
            report = prefs.getString("last_report", "") ?: ""
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Accent,
        background = Bg,
        surface = Panel,
        onPrimary = Bg,
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Scaffold(containerColor = Bg) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Header()
                ModuleStrip(current = module, onSelect = ::resetModule)

                when (module) {
                    Module.HOME -> HomeScreen(onSelect = ::resetModule, reportExists = report.isNotBlank())
                    Module.PHONE -> {
                        InputScreen(
                            title = "فحص رقم الهاتف",
                            description = "يتحقق MOSA من شكل الرقم وترتيبه فقط. لا يبحث عن مالك الرقم ولا يثبت أن الرقم نشط أو مسجل في واتساب.",
                            input = input,
                            onInput = { input = it },
                            placeholder = "+968 9XXXXXXX",
                            keyboardType = KeyboardType.Phone,
                            buttonLabel = "تحليل الصيغة",
                            onRun = {
                                links = emptyList()
                                val cleaned = input.trim().replace(Regex("[\\s().-]"), "")
                                val valid = Regex("^\\+[1-9][0-9]{7,14}$").matches(cleaned)
                                val lines = if (valid) listOf(
                                    "الصيغة: مقبولة مبدئياً وفق نمط E.164.",
                                    "الرقم بعد التنظيف: $cleaned",
                                    "عدد الأرقام: ${cleaned.count { it.isDigit() }}",
                                    "لا يتضمن هذا الفحص تحديد هوية المالك أو حالة حساب واتساب."
                                ) else listOf(
                                    "الصيغة غير مطابقة للنمط الدولي E.164.",
                                    "اكتب + ثم مفتاح الدولة ثم الرقم، من 8 إلى 15 رقماً إجمالاً.",
                                    "مثال شكلي: +968XXXXXXXX"
                                )
                                AnalysisResult(if (valid) "صيغة رقم مقبولة" else "تحتاج إلى تصحيح", lines,
                                    "هذا فحص تنسيق محلي فقط، وليس تحققاً من ملكية الرقم أو نشاطه.")
                            }
                        )
                    }
                    Module.USERNAME -> {
                        InputScreen(
                            title = "البحث عن اسم مستخدم عام",
                            description = "يُنشئ روابط بحث يفتحها المستخدم بنفسه في محرك البحث. لا يجمع التطبيق نتائج الحسابات ولا يتجاوز تسجيل الدخول أو إعدادات الخصوصية.",
                            input = input,
                            onInput = { input = it },
                            placeholder = "مثال: public_handle",
                            buttonLabel = "إنشاء روابط البحث",
                            consent = consent,
                            onConsent = { consent = it },
                            consentText = "أؤكد أن البحث يقتصر على معلومات عامة ولغرض مشروع، وأن لدي موافقة عند الحاجة."
                        ) {
                            val handle = input.trim().removePrefix("@")
                            if (!Regex("^[A-Za-z0-9._-]{2,40}$").matches(handle)) {
                                links = emptyList()
                                AnalysisResult("اسم مستخدم غير صالح", listOf("استخدم 2 إلى 40 حرفاً أو رقماً أو . _ - فقط."))
                            } else if (!consent) {
                                links = emptyList()
                                AnalysisResult("يلزم تأكيد الاستخدام المشروع", listOf("فعّل مربع الإقرار قبل إنشاء روابط البحث."))
                            } else {
                                val q = Uri.encode("\"$handle\"")
                                links = listOf(
                                    SearchLink("بحث عام على Google", "https://www.google.com/search?q=$q"),
                                    SearchLink("Instagram", "https://www.google.com/search?q=site%3Ainstagram.com%2F+$q"),
                                    SearchLink("TikTok", "https://www.google.com/search?q=site%3Atiktok.com%2F%40+$q"),
                                    SearchLink("GitHub", "https://www.google.com/search?q=site%3Agithub.com%2F+$q"),
                                    SearchLink("YouTube", "https://www.google.com/search?q=site%3Ayoutube.com%2F%40+$q"),
                                    SearchLink("X", "https://www.google.com/search?q=site%3Ax.com%2F+$q")
                                )
                                AnalysisResult("روابط بحث جاهزة", listOf(
                                    "تم تجهيز ${links.size} عمليات بحث مفتوحة.",
                                    "افتح كل مصدر على حدة وقيّم النتائج يدوياً.",
                                    "تطابق اسم المستخدم لا يثبت أن الحسابات تعود إلى الشخص نفسه."
                                ), "النتائج تأتي من محرك البحث، وقد تكون قديمة أو غير مكتملة.")
                            }
                        }
                    }
                    Module.DOMAIN -> {
                        InputScreen(
                            title = "فحص نطاق إنترنت",
                            description = "تحليل محلي لاسم النطاق مع روابط لمصادر السجلات العامة وشهادات TLS المنشورة.",
                            input = input,
                            onInput = { input = it },
                            placeholder = "example.com",
                            buttonLabel = "تحليل النطاق"
                        ) {
                            val host = normalizeHost(input)
                            if (host == null) {
                                links = emptyList()
                                AnalysisResult("النطاق غير صالح", listOf("أدخل اسم نطاق صحيحاً مثل example.com دون مسار أو نص إضافي."))
                            } else {
                                val ascii = IDN.toASCII(host)
                                links = listOf(
                                    SearchLink("سجل النطاق RDAP", "https://rdap.org/domain/$ascii"),
                                    SearchLink("شهادات TLS المنشورة", "https://crt.sh/?q=%25.$ascii"),
                                    SearchLink("بحث ويب عن النطاق", "https://www.google.com/search?q=${Uri.encode("\"$ascii\"")}")
                                )
                                AnalysisResult("تم تحليل اسم النطاق", listOf(
                                    "النطاق: $host",
                                    "الصيغة ASCII: $ascii",
                                    "عدد مستويات الاسم: ${ascii.split('.').size}",
                                    if (ascii.contains("xn--")) "تنبيه: يحتوي الاسم على ترميز IDN/Punycode؛ تحقق من الأحرف المتشابهة." else "لم يُكتشف ترميز Punycode في اسم النطاق.",
                                    "روابط RDAP وشهادات TLS متاحة للمراجعة اليدوية."
                                ), "وجود سجل أو شهادة لا يعني أن الموقع آمن أو موثوق.")
                            }
                        }
                    }
                    Module.URL -> {
                        InputScreen(
                            title = "تحليل رابط قبل فتحه",
                            description = "يفحص الرابط محلياً بحثاً عن مؤشرات شائعة للتصيد. لا يفتح الرابط ولا يرسل بياناته لأي خادم.",
                            input = input,
                            onInput = { input = it },
                            placeholder = "https://example.com/path",
                            keyboardType = KeyboardType.Uri,
                            buttonLabel = "فحص الرابط"
                        ) {
                            analyzeUrl(input).also { links = emptyList() }
                        }
                    }
                    Module.BUSINESS -> {
                        InputScreen(
                            title = "بحث عن نشاط تجاري",
                            description = "ينشئ بحثاً عن الاسم التجاري وموقعه في نتائج الويب والخرائط. لا يصدر شهادة قانونية ولا يؤكد صلاحية السجل التجاري.",
                            input = input,
                            onInput = { input = it },
                            placeholder = "اسم النشاط + المدينة",
                            buttonLabel = "إنشاء عمليات البحث",
                            consent = consent,
                            onConsent = { consent = it },
                            consentText = "أستخدم هذه الروابط لمراجعة معلومات تجارية منشورة علناً وبشكل مشروع."
                        ) {
                            if (input.trim().length < 3) {
                                links = emptyList()
                                AnalysisResult("أدخل اسماً أو وصفاً أوضح", listOf("اكتب ثلاثة أحرف على الأقل."))
                            } else if (!consent) {
                                links = emptyList()
                                AnalysisResult("يلزم تأكيد الاستخدام المشروع", listOf("فعّل مربع الإقرار لإنشاء عمليات البحث."))
                            } else {
                                val query = Uri.encode(input.trim())
                                links = listOf(
                                    SearchLink("البحث في خرائط Google", "https://www.google.com/maps/search/?api=1&query=$query"),
                                    SearchLink("بحث ويب عن النشاط", "https://www.google.com/search?q=$query"),
                                    SearchLink("البحث عن سجل/ترخيص رسمي", "https://www.google.com/search?q=${Uri.encode(input.trim() + " سجل تجاري ترخيص رسمي عمان")}")
                                )
                                AnalysisResult("تم تجهيز البحث التجاري", listOf(
                                    "عبارة البحث: ${input.trim()}",
                                    "افتح الخرائط وقارن الاسم والعنوان وبيانات الاتصال.",
                                    "طابق رقم السجل مع الجهة الحكومية المختصة قبل اتخاذ قرار."
                                ), "لا تعتبر نتائج الخرائط أو محركات البحث إثباتاً قانونياً للترخيص.")
                            }
                        }
                    }
                    Module.PRIVACY -> PrivacyScreen(items = privacyItems, checked = checked) { index, value ->
                        checked[privacyItems[index]] = value
                        prefs.edit().putBoolean("privacy_$index", value).apply()
                    }
                    Module.REPORT -> ReportScreen(report = report, onShare = { shareText(context, report) }, onCopy = { copyText(context, report) })
                }

                if (resultForModule(module, result = null) == null) { /* keep layout stable */ }
                if (module !in listOf(Module.HOME, Module.PRIVACY, Module.REPORT)) {
                    // The result is stored by the module input surface through its callback.
                    CurrentResultHost(
                        result = result,
                        links = links,
                        onOpen = { openUrl(context, it) },
                        onShare = { text ->
                            saveReport(text)
                            shareText(context, text)
                        },
                        onCopy = { text -> copyText(context, text) }
                    )
                }
                Footer()
            }
        }
    }
}

@Composable
private fun InputScreen(
    title: String,
    description: String,
    input: String,
    onInput: (String) -> Unit,
    placeholder: String,
    buttonLabel: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    consent: Boolean? = null,
    onConsent: ((Boolean) -> Unit)? = null,
    consentText: String = "",
    onRun: () -> AnalysisResult
) {
    val ctx = LocalContext.current
    val prefs = remember(ctx) { ctx.getSharedPreferences("mosa_osint", Context.MODE_PRIVATE) }
    var latest by remember(title) { mutableStateOf<AnalysisResult?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title, description)
        OutlinedTextField(
            value = input,
            onValueChange = { onInput(it); latest = null },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Muted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                focusedLabelColor = Accent,
                cursorColor = Accent,
                unfocusedBorderColor = Muted.copy(alpha = .45f)
            )
        )
        if (consent != null && onConsent != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = onConsent, colors = CheckboxDefaults.colors(checkedColor = Accent))
                Text(consentText, color = Muted, fontSize = 11.sp)
            }
        }
        Button(
            onClick = {
                val computed = onRun()
                latest = computed
                val reportText = buildString {
                    appendLine("MOSA OSINT REPORT")
                    appendLine("Module: $title")
                    appendLine("Result: ${computed.heading}")
                    computed.lines.forEach { appendLine("- $it") }
                    computed.caution?.let { appendLine("Caution: $it") }
                    appendLine("Generated locally on this device.")
                }
                prefs.edit().putString("last_report", reportText).apply()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(8.dp))
            Text(buttonLabel, fontWeight = FontWeight.Bold)
        }
        latest?.let { InlineResult(it) }
    }
}

@Composable
private fun CurrentResultHost(
    result: AnalysisResult?,
    links: List<SearchLink>,
    onOpen: (String) -> Unit,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    // Output is generated in InputScreen. This host intentionally contains source links and safety notes.
    if (links.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("مصادر البحث", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            links.forEach { item ->
                OutlinedButton(
                    onClick = { onOpen(item.url) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = .5f))
                ) {
                    Icon(Icons.Default.OpenInNew, null, tint = Accent)
                    Spacer(Modifier.width(8.dp))
                    Text(item.label)
                }
            }
        }
    }
}

@Composable
private fun InlineResult(result: AnalysisResult) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel2), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(result.heading, color = Accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            result.lines.forEach { line ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", color = Accent, modifier = Modifier.padding(end = 7.dp))
                    Text(line, color = Color.White, fontSize = 13.sp)
                }
            }
            result.caution?.let {
                Divider(color = Muted.copy(alpha = .25f))
                Text("تنبيه: $it", color = Warn, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Accent.copy(alpha = .14f), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Shield, null, tint = Accent, modifier = Modifier.padding(12.dp).size(30.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("MOSA OSINT", fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text("ANDROID INTELLIGENCE WORKSPACE", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Surface(color = Color(0xFF17332F), shape = RoundedCornerShape(50)) {
            Text("v1.0", color = Accent, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
        }
    }
}

@Composable
private fun ModuleStrip(current: Module, onSelect: (Module) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Module.entries.forEach { item ->
            FilterChip(
                selected = current == item,
                onClick = { onSelect(item) },
                label = { Text(item.title, fontSize = 11.sp) },
                leadingIcon = if (current == item) ({ Icon(Icons.Default.Check, null, modifier = Modifier.size(15.dp)) }) else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF17332F),
                    selectedLabelColor = Accent,
                    selectedLeadingIconColor = Accent
                )
            )
        }
    }
}

@Composable
private fun HomeScreen(onSelect: (Module) -> Unit, reportExists: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("OPEN-SOURCE INTELLIGENCE", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("حلّل المعلومات العامة، وتحقق من المؤشرات، واحتفظ بتقريرك.", fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 27.sp)
                Text("أدوات محلية وبحث يفتحه المستخدم بنفسه. لا اختراق، ولا تجاوز للخصوصية، ولا ادعاء بالوصول إلى بيانات واتساب المخفية.",
                    color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
            }
        }
        Text("وحدات التحليل", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        val modules = listOf(
            Triple(Module.PHONE, Icons.Default.Phone, "فحص صيغة E.164 دون كشف هوية صاحب الرقم"),
            Triple(Module.USERNAME, Icons.Default.PersonSearch, "روابط بحث في نتائج الحسابات العامة"),
            Triple(Module.DOMAIN, Icons.Default.Language, "RDAP وسجلات شهادات TLS للنطاقات"),
            Triple(Module.URL, Icons.Default.Link, "مؤشرات محلية لاحتمالات الرابط المشبوه"),
            Triple(Module.BUSINESS, Icons.Default.Business, "بحث تجاري في الويب والخرائط"),
            Triple(Module.PRIVACY, Icons.Default.Security, "قائمة تدقيق لحماية حسابك وأجهزتك")
        )
        modules.forEach { (target, icon, desc) ->
            Card(onClick = { onSelect(target) }, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(13.dp)) {
                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = Accent, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(target.title, fontWeight = FontWeight.SemiBold)
                        Text(desc, color = Muted, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
        if (reportExists) {
            OutlinedButton(onClick = { onSelect(Module.REPORT) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Description, null)
                Spacer(Modifier.width(8.dp))
                Text("عرض آخر تقرير محفوظ")
            }
        }
    }
}

@Composable
private fun PrivacyScreen(items: List<String>, checked: SnapshotStateMap<String, Boolean>, onChecked: (Int, Boolean) -> Unit) {
    val completed = items.count { checked[it] == true }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("تدقيق الخصوصية الشخصية", "قائمة إرشادية لحماية حسابك أنت. العلامات تحفظ محلياً على الجهاز.")
        Card(colors = CardDefaults.cardColors(containerColor = Panel2)) {
            Column(Modifier.padding(14.dp)) {
                Text("$completed من ${items.size} إجراءات مكتملة", color = Accent, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { completed.toFloat() / items.size },
                    modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
                    color = Accent,
                    trackColor = Muted.copy(alpha = .25f)
                )
            }
        }
        items.forEachIndexed { index, label ->
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked[label] == true,
                        onCheckedChange = { onChecked(index, it) },
                        colors = CheckboxDefaults.colors(checkedColor = Accent)
                    )
                    Text(label, color = Color.White, fontSize = 12.sp)
                }
            }
        }
        Text("لا يقرأ التطبيق إعدادات واتساب تلقائياً؛ هذه قائمة مراجعة ذاتية فقط.", color = Warn, fontSize = 11.sp)
    }
}

@Composable
private fun ReportScreen(report: String, onShare: () -> Unit, onCopy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("التقرير المحلي", "آخر نتيجة تحليل محفوظة على هذا الجهاز.")
        if (report.isBlank()) {
            Text("لا توجد تقارير بعد. نفّذ تحليلاً ثم افتح هذه الصفحة.", color = Muted, fontSize = 13.sp)
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Text(report, modifier = Modifier.padding(14.dp), color = Color.White, fontSize = 12.sp, lineHeight = 19.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onShare, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)) {
                    Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("مشاركة")
                }
                OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(6.dp)); Text("نسخ")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun Footer() {
    Divider(color = Muted.copy(alpha = .18f))
    Text("MOSA OSINT • يعمل محلياً ولا يتجاوز خصوصية الآخرين", color = Muted, fontSize = 10.sp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
}

private fun normalizeHost(raw: String): String? {
    val value = raw.trim().lowercase(Locale.ROOT).removePrefix("https://").removePrefix("http://").substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(':').trimEnd('.')
    if (value.isBlank() || value.contains(' ') || value.length > 253) return null
    val ascii = try { IDN.toASCII(value) } catch (_: Exception) { return null }
    val labels = ascii.split('.')
    if (labels.size < 2 || labels.any { it.isBlank() || it.length > 63 || !Regex("^[a-zA-Z0-9-]+$").matches(it) || it.startsWith("-") || it.endsWith("-") }) return null
    return value
}

private fun analyzeUrl(raw: String): AnalysisResult {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return AnalysisResult("أدخل رابطاً", listOf("لا يوجد رابط لفحصه."))
    val prepared = if (trimmed.contains("://")) trimmed else "https://$trimmed"
    val uri = try { URI(prepared) } catch (_: Exception) {
        return AnalysisResult("تعذر تحليل الرابط", listOf("تحقق من صيغة الرابط والأحرف المستخدمة."))
    }
    val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: ""
    val host = uri.host?.lowercase(Locale.ROOT)
    if (host.isNullOrBlank()) return AnalysisResult("الرابط لا يحتوي على اسم مضيف صالح", listOf("قد يكون الرابط ناقصاً أو يحتوي على صيغة غير صحيحة."))
    val flags = mutableListOf<String>()
    if (scheme != "https") flags += "الرابط لا يستخدم HTTPS."
    if (uri.rawUserInfo != null || trimmed.substringAfter("://", "").substringBefore('/').contains('@')) flags += "وجود @ في جزء المضيف قد يُستخدم لإخفاء الوجهة الحقيقية."
    if (host.startsWith("xn--") || host.contains(".xn--")) flags += "اسم المضيف يتضمن Punycode؛ انتبه للأحرف المتشابهة."
    if (Regex("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$").matches(host)) flags += "الرابط يستخدم عنوان IP مباشرة بدلاً من اسم نطاق."
    if (host in listOf("bit.ly", "tinyurl.com", "t.co", "rb.gy", "is.gd", "cutt.ly")) flags += "الرابط مختصر؛ الوجهة النهائية غير ظاهرة من النص."
    if (trimmed.length > 180) flags += "الرابط طويل بصورة غير معتادة."
    if (uri.port != -1 && uri.port !in listOf(80, 443)) flags += "يستخدم منفذ اتصال غير افتراضي."
    if (host.count { it == '-' } >= 3) flags += "اسم المضيف يحتوي على عدة شرطات."
    if (flags.isEmpty()) flags += "لم تُكتشف مؤشرات شائعة في الفحص المحلي."
    val level = when {
        flags.any { it.contains("@") || it.contains("IP") || it.contains("مختصر") } -> "مؤشرات تستحق الحذر"
        flags.size >= 3 -> "عدة مؤشرات تحتاج إلى مراجعة"
        else -> "مؤشرات محدودة"
    }
    return AnalysisResult(level, listOf(
        "المضيف: $host",
        "البروتوكول: $scheme",
        "طول الرابط: ${trimmed.length} حرفاً"
    ) + flags, "هذا فحص إرشادي لا يثبت أن الرابط خبيث أو آمن. لا تفتح الروابط غير الموثوقة.")
}

private fun resultForModule(module: Module, result: AnalysisResult?): AnalysisResult? = result

private fun openUrl(context: Context, value: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
    } catch (_: Exception) {
        Toast.makeText(context, "تعذر فتح الرابط. تحقق من وجود متصفح.", Toast.LENGTH_SHORT).show()
    }
}

private fun copyText(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("MOSA OSINT", value))
    Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, value: String) {
    if (value.isBlank()) return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "MOSA OSINT Report")
        putExtra(Intent.EXTRA_TEXT, value)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة التقرير"))
}
