package com.mosa.osint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChainOfCustodyScreen(events: String, onCopy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("سجل أحداث الأدلة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "يسجل MOSA محلياً وقت إنشاء ملخص التحليل، ومرجع القضية، واسم المصدر وبصمته عند توفرها.",
            color = Color(0xFF91A4B2), fontSize = 12.sp, lineHeight = 18.sp
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF172630)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Description, null, tint = Color(0xFF35D0AE))
                Text(
                    "حدود السجل في هذه النسخة",
                    color = Color(0xFF35D0AE), fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
                Text(
                    "هذا سجل أحداث محلي أولي داخل SharedPreferences. ليس سجلاً غير قابل للتعديل، ولا يوقّع الأحداث رقمياً، ولا يكفي وحده لإثبات سلسلة حيازة قضائية. يلزم في النسخة التشغيلية تخزين محمي، توقيع رقمي، هوية فاحص موثقة، ومزامنة مع مخزن أدلة مركزي معتمد.",
                    color = Color(0xFFFFC36B), fontSize = 11.sp, lineHeight = 17.sp
                )
            }
        }
        if (events.isBlank()) {
            Text("لا توجد أحداث مسجلة بعد. أضف ملخص تحليل أو سجّل تقييماً أولياً لجهاز.", color = Color(0xFF91A4B2), fontSize = 12.sp)
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111B23)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    events,
                    modifier = Modifier.padding(14.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 16.sp
                )
            }
            Button(
                onClick = onCopy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF35D0AE),
                    contentColor = Color(0xFF080D12)
                )
            ) {
                Text("نسخ سجل الأحداث", fontWeight = FontWeight.Bold)
            }
        }
    }
}
