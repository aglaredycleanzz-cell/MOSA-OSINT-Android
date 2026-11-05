package com.mosa.osint

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

private val ForensicsAccent = Color(0xFF35D0AE)
private val ForensicsPanel = Color(0xFF111B23)
private val ForensicsPanelAlt = Color(0xFF172630)
private val ForensicsMuted = Color(0xFF91A4B2)
private val ForensicsWarning = Color(0xFFFFC36B)

private data class WhatsAppSummary(
    val fileName: String,
    val fileBytes: Int,
    val sha256: String,
    val messages: Int,
    val senders: Int,
    val senderCounts: Map<String, Int>,
    val attachmentMarkers: Int,
    val urls: Int,
    val firstTimestamp: String?,
    val lastTimestamp: String?
)

@Composable
fun WhatsAppForensicsScreen(onReportGenerated: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var caseReference by remember { mutableStateOf("") }
    var authorized by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<WhatsAppSummary?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            busy = true
            error = null
            summary = null
            scope.launch {
                try {
                    summary = withContext(Dispatchers.IO) {
                        analyzeSelectedWhatsAppExport(context, uri)
                    }
                } catch (exception: Exception) {
                    error = exception.message ?: "تعذر تحليل الملف المحدد."
                } finally {
                    busy = false
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ForensicsSectionTitle(
            title = "تحليل تصدير محادثة واتساب",
            description = "استورد ملف TXT اختاره المحقق من الجهاز. يجري التحليل محلياً دون الاتصال بواتساب أو بخادم خارجي."
        )
        Card(colors = CardDefaults.cardColors(containerColor = ForensicsPanelAlt), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = ForensicsAccent)
                    Spacer(Modifier.size(8.dp))
                    Text("نطاق الوصول", color = ForensicsAccent, fontWeight = FontWeight.Bold)
                }
                Text(
                    "هذه الوحدة لا تفتح قاعدة بيانات واتساب، ولا تتجاوز قفل الهاتف أو التشفير. تحلل فقط ملف التصدير الذي يختاره المستخدم.",
                    color = Color.White, fontSize = 12.sp, lineHeight = 18.sp
                )
                Text(
                    "لا تحفظ MOSA نصوص الرسائل أو جهات الاتصال المستخرجة؛ يحفظ ملخصاً إحصائياً وبصمة الملف فقط.",
                    color = ForensicsMuted, fontSize = 11.sp, lineHeight = 17.sp
                )
            }
        }
        OutlinedTextField(
            value = caseReference,
            onValueChange = { caseReference = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("رقم القضية / مرجع التفويض") },
            singleLine = true
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = authorized, onCheckedChange = { authorized = it })
            Text(
                "أؤكد أن لدي تفويضاً صالحاً لفحص هذا الملف وأن نطاق الفحص موثق.",
                color = ForensicsMuted, fontSize = 11.sp, lineHeight = 16.sp
            )
        }
        Button(
            onClick = { filePicker.launch(arrayOf("text/plain", "application/octet-stream")) },
            enabled = authorized && caseReference.isNotBlank() && !busy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ForensicsAccent, contentColor = Color(0xFF080D12)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.FolderOpen, null)
            Spacer(Modifier.size(8.dp))
            Text("اختيار ملف تصدير واتساب TXT", fontWeight = FontWeight.Bold)
        }
        if (busy) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ForensicsAccent)
                Spacer(Modifier.size(10.dp))
                Text("جارٍ تحليل الملف على هذا الجهاز…", color = ForensicsMuted, fontSize = 12.sp)
            }
        }
        error?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2024))) {
                Text(message, color = Color(0xFFFFB5B5), modifier = Modifier.padding(12.dp), fontSize = 12.sp)
            }
        }
        summary?.let { item ->
            Card(colors = CardDefaults.cardColors(containerColor = ForensicsPanel), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = ForensicsAccent)
                        Spacer(Modifier.size(8.dp))
                        Text("نتيجة التحليل", color = ForensicsAccent, fontWeight = FontWeight.Bold)
                    }
                    ForensicsMetric("الملف", item.fileName)
                    ForensicsMetric("حجم الملف", item.fileBytes.toString() + " بايت")
                    ForensicsMetric("الرسائل المؤرخة", item.messages.toString())
                    ForensicsMetric("عدد المرسلين المميزين", item.senders.toString())
                    ForensicsMetric("مؤشرات المرفقات", item.attachmentMarkers.toString())
                    ForensicsMetric("الروابط المكتشفة", item.urls.toString())
                    ForensicsMetric("أول طابع زمني", item.firstTimestamp ?: "غير متاح")
                    ForensicsMetric("آخر طابع زمني", item.lastTimestamp ?: "غير متاح")
                    Text("توزيع الرسائل حسب المرسل", color = ForensicsAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (item.senderCounts.isEmpty()) {
                        Text("لم يتم التعرف على أسماء مرسلين في صيغة الملف.", color = ForensicsMuted, fontSize = 11.sp)
                    } else {
                        item.senderCounts.entries.sortedByDescending { entry -> entry.value }.take(12).forEach { entry ->
                            ForensicsMetric(entry.key, entry.value.toString())
                        }
                    }
                    Text("SHA-256", color = ForensicsAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(item.sha256, color = Color.White, fontSize = 10.sp, lineHeight = 15.sp)
                    Text(
                        "المؤشرات إحصائية ولا تثبت هوية المرسل أو صحة محتوى الرسائل. احتفظ بالملف الأصلي وسجل مصدره وسلسلة حيازته.",
                        color = ForensicsWarning, fontSize = 10.sp, lineHeight = 15.sp
                    )
                    Button(
                        onClick = {
                            val report = buildString {
                                appendLine("MOSA DIGITAL FORENSICS — WHATSAPP EXPORT SUMMARY")
                                appendLine("Case reference: " + caseReference.trim())
                                appendLine("File: " + item.fileName)
                                appendLine("Size bytes: " + item.fileBytes)
                                appendLine("SHA-256: " + item.sha256)
                                appendLine("Timestamped messages: " + item.messages)
                                appendLine("Distinct senders parsed: " + item.senders)
                                appendLine("Attachment markers: " + item.attachmentMarkers)
                                appendLine("URLs detected: " + item.urls)
                                appendLine("First timestamp: " + (item.firstTimestamp ?: "not available"))
                                appendLine("Last timestamp: " + (item.lastTimestamp ?: "not available"))
                                item.senderCounts.entries.sortedByDescending { entry -> entry.value }.take(12)
                                    .forEach { appendLine("Sender label '" + it.key + "': " + it.value) }
                                appendLine("Analysis performed locally; source message text was not retained by MOSA.")
                                appendLine("Limitations: exported text only; no direct WhatsApp access, deleted-message recovery, account takeover, or cryptographic bypass.")
                            }
                            onReportGenerated(report)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForensicsAccent, contentColor = Color(0xFF080D12))
                    ) {
                        Text("إضافة الملخص إلى سجل التقارير", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceTriageScreen(onReportGenerated: (String) -> Unit) {
    var caseReference by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var androidVersion by remember { mutableStateOf("") }
    var powerState by remember { mutableStateOf("غير محدد") }
    var screenState by remember { mutableStateOf("غير محدد") }
    var lockState by remember { mutableStateOf("غير محدد") }
    var damageNotes by remember { mutableStateOf("") }
    var authorized by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        ForensicsSectionTitle(
            "التقييم الأولي للهاتف المضبوط",
            "سجّل الحالة الظاهرة للجهاز قبل إحالته إلى مختبر الفحص. هذه الوحدة لا تحاول فتح القفل أو تعديل الجهاز."
        )
        OutlinedTextField(caseReference, { caseReference = it }, modifier = Modifier.fillMaxWidth(), label = { Text("رقم القضية / مرجع التفويض") }, singleLine = true)
        OutlinedTextField(model, { model = it }, modifier = Modifier.fillMaxWidth(), label = { Text("الشركة والطراز") }, singleLine = true)
        OutlinedTextField(androidVersion, { androidVersion = it }, modifier = Modifier.fillMaxWidth(), label = { Text("إصدار Android (إن كان معروفاً)") }, singleLine = true)
        OutlinedTextField(powerState, { powerState = it }, modifier = Modifier.fillMaxWidth(), label = { Text("حالة التشغيل") }, singleLine = true)
        OutlinedTextField(screenState, { screenState = it }, modifier = Modifier.fillMaxWidth(), label = { Text("حالة الشاشة") }, singleLine = true)
        OutlinedTextField(lockState, { lockState = it }, modifier = Modifier.fillMaxWidth(), label = { Text("حالة القفل الظاهرة") }, singleLine = true)
        OutlinedTextField(damageNotes, { damageNotes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("وصف التلف والملاحظات") }, minLines = 3)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = authorized, onCheckedChange = { authorized = it })
            Text("تم تسجيل الجهاز ضمن إجراء مخول، وهذه ملاحظات الفحص الأولي.", color = ForensicsMuted, fontSize = 11.sp)
        }
        Card(colors = CardDefaults.cardColors(containerColor = ForensicsPanelAlt)) {
            Text(
                "تنبيه حفظ الأدلة: لا تجرّب رموزاً عشوائية، ولا تنفّذ إعادة ضبط مصنع أو تحديثاً أو تفليشاً. وثّق الحالة الظاهرة واتبع إجراءات الجهة المختصة لحفظ الجهاز.",
                modifier = Modifier.padding(12.dp), color = ForensicsWarning, fontSize = 11.sp, lineHeight = 17.sp
            )
        }
        Button(
            onClick = {
                val report = listOf(
                    "MOSA DIGITAL FORENSICS — DEVICE TRIAGE",
                    "Case reference: " + caseReference.trim(),
                    "Manufacturer/model: " + model.ifBlank { "Not recorded" },
                    "Android version: " + androidVersion.ifBlank { "Unknown" },
                    "Power state: " + powerState.trim(),
                    "Screen state: " + screenState.trim(),
                    "Lock state: " + lockState.trim(),
                    "Damage/notes: " + damageNotes.ifBlank { "None recorded" },
                    "This is an examiner-entered visual triage record, not a device extraction result."
                ).joinToString("\n")
                onReportGenerated(report)
            },
            enabled = authorized && caseReference.isNotBlank() && model.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ForensicsAccent, contentColor = Color(0xFF080D12))
        ) {
            Text("إنشاء سجل التقييم الأولي", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ForensicsSectionTitle(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(description, color = ForensicsMuted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun ForensicsMetric(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = ForensicsMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}

private fun analyzeSelectedWhatsAppExport(context: Context, uri: Uri): WhatsAppSummary {
    val fileName = queryDisplayName(context, uri)
    require(fileName.lowercase(Locale.ROOT).endsWith(".txt")) {
        "النسخة الأولى تدعم ملف تصدير واتساب النصي TXT فقط."
    }
    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        readBounded(input, 20 * 1024 * 1024)
    } ?: throw IllegalStateException("تعذر قراءة الملف الذي اخترته.")
    require(bytes.isNotEmpty()) { "الملف فارغ." }

    val text = String(bytes, Charsets.UTF_8).removePrefix("\uFEFF")
    val parsed = parseWhatsAppText(text)
    require(parsed.first > 0) {
        "لم يتم التعرف على طوابع زمنية بصيغة تصدير واتساب المعتادة. تحقق من اختيار ملف TXT صحيح."
    }
    val sha256 = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { byte -> "%02x".format(byte) }

    return WhatsAppSummary(
        fileName = fileName,
        fileBytes = bytes.size,
        sha256 = sha256,
        messages = parsed.first,
        senders = parsed.second.size,
        senderCounts = parsed.second,
        attachmentMarkers = Regex("(?i)<media omitted>|<attached:|image omitted|video omitted|audio omitted|document omitted")
            .findAll(text).count(),
        urls = Regex("(?i)https?://[^\\s]+|www\\.[^\\s]+").findAll(text).count(),
        firstTimestamp = parsed.third.firstOrNull(),
        lastTimestamp = parsed.third.lastOrNull()
    )
}

private fun parseWhatsAppText(text: String): Triple<Int, Map<String, Int>, List<String>> {
    val commonHeader = Regex(
        """^\uFEFF?[\u200E\u200F]*(?:\[)?(\d{1,4}[./-]\d{1,2}[./-]\d{2,4}),?\s+(\d{1,2}:\d{2}(?::\d{2})?(?:\s*[APap][Mm])?)(?:\])?\s+-\s+(.*)$"""
    )
    val bracketHeader = Regex(
        """^\uFEFF?[\u200E\u200F]*\[(\d{1,4}[./-]\d{1,2}[./-]\d{2,4}),\s*(\d{1,2}:\d{2}(?::\d{2})?(?:\s*[APap][Mm])?)\]\s+(.*)$"""
    )
    val senderPattern = Regex("""^([^:]{1,80}):\s?.*$""")
    val senderCounts = linkedMapOf<String, Int>()
    val timestamps = mutableListOf<String>()
    var messageCount = 0

    text.lineSequence().forEach { line ->
        val match = commonHeader.find(line) ?: bracketHeader.find(line)
        if (match != null) {
            messageCount++
            timestamps += match.groupValues[1] + " " + match.groupValues[2]
            val sender = senderPattern.find(match.groupValues[3])?.groupValues?.get(1)?.trim()
            if (!sender.isNullOrBlank()) {
                senderCounts[sender] = (senderCounts[sender] ?: 0) + 1
            }
        }
    }
    return Triple(messageCount, senderCounts, timestamps)
}

private fun queryDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index) ?: "whatsapp-export.txt"
        }
    }
    return "whatsapp-export.txt"
}

private fun readBounded(input: InputStream, maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "حجم الملف يتجاوز الحد المسموح به وهو 20 ميغابايت." }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}
