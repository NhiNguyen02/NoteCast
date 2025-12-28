package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R
import com.example.notecast.presentation.theme.SubTitleColor
import com.example.notecast.presentation.theme.TabButton3Brush
import com.example.notecast.presentation.viewmodel.NoteAudioViewModel
import com.example.notecast.presentation.ui.notetext.ActionChip

@Composable
fun NoteInfoAndActions(
    isProcessing: Boolean,
    hasMindMap: Boolean,
    currentStep: NoteAudioViewModel.ProcessingStep,
    onRegenerateAll: () -> Unit,
) {
    // Map step -> progress percent & description
    val (progressPercent, description) = when (currentStep) {
        NoteAudioViewModel.ProcessingStep.TRANSCRIBING -> 0 to "Đang nhận diện giọng nói..."
        NoteAudioViewModel.ProcessingStep.NORMALIZING  -> 25 to "Đang chuẩn hóa văn bản..."
        NoteAudioViewModel.ProcessingStep.SUMMARIZING  -> 60 to "Đang tóm tắt và trích xuất từ khóa..."
        NoteAudioViewModel.ProcessingStep.MINDMAP      -> 85 to "Đang tạo Mindmap..."
        NoteAudioViewModel.ProcessingStep.DONE         -> 100 to if (hasMindMap) "Đã xử lý xong (kèm Mindmap)" else "Đã xử lý xong"
    }

    // Stepper: Transcribing → Normalizing → Summarizing → Mindmap → Done
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StepItem(label = "ASR", step = NoteAudioViewModel.ProcessingStep.TRANSCRIBING, current = currentStep)
        StepDivider()
        StepItem(label = "Normalize", step = NoteAudioViewModel.ProcessingStep.NORMALIZING, current = currentStep)
        StepDivider()
        StepItem(label = "Summary", step = NoteAudioViewModel.ProcessingStep.SUMMARIZING, current = currentStep)
        StepDivider()
        StepItem(label = "Mindmap", step = NoteAudioViewModel.ProcessingStep.MINDMAP, current = currentStep)
    }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isProcessing && currentStep != NoteAudioViewModel.ProcessingStep.DONE) description else "Đã xử lý xong",
                style = MaterialTheme.typography.bodySmall,
                color = SubTitleColor,
            )
            if (isProcessing || currentStep == NoteAudioViewModel.ProcessingStep.DONE) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SubTitleColor,
                )
            }
        }

        // ActionChip(label, leadingIcon, onClick, isLoading, backgroundBrush, labelColor)
        ActionChip(
            "Chạy lại enrichment",
            painterResource(R.drawable.arrow_up_down),
            onClick = { if (!isProcessing) onRegenerateAll() },
            isLoading = isProcessing,
            backgroundBrush = TabButton3Brush,
            labelColor = Color.White,
        )
    }

    HorizontalDivider(
        color = Color(0xffE5E7EB),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StepItem(
    label: String,
    step: NoteAudioViewModel.ProcessingStep,
    current: NoteAudioViewModel.ProcessingStep,
) {
    val isActive = current.ordinal >= step.ordinal
    val color = if (isActive) SubTitleColor else Color.LightGray
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .background(
                    brush = if (isActive) Brush.verticalGradient(
                        0f to Color(0xff00D2FF),
                        1f to Color(0xff7532FB)
                    ) else Brush.verticalGradient(0f to Color.LightGray, 1f to Color.LightGray),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 8.dp),
        ) {}
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
        )
    }
}

@Composable
private fun StepDivider() {
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .background(Color(0xFFE5E7EB))
    )
}
