package com.example.notecast.presentation.ui.common_components

import androidx.compose.animation.AnimatedVisibility
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
import com.example.notecast.presentation.theme.MenuBackgroundBrush
import com.example.notecast.presentation.theme.SubTitleColor
import com.example.notecast.presentation.theme.TabButton3Brush
import com.example.notecast.presentation.viewmodel.NoteAudioViewModel
import com.example.notecast.presentation.ui.notetext.ActionChip
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.getValue

data class ProcessingUIState(
    val progress: Float, // 0f..1f
    val label: String
)

fun mapStepToUI(
    step: NoteAudioViewModel.ProcessingStep,
    hasMindMap: Boolean
): ProcessingUIState = when (step) {
    NoteAudioViewModel.ProcessingStep.TRANSCRIBING ->
        ProcessingUIState(0.1f, "Đang nhận diện giọng nói (ASR)")

    NoteAudioViewModel.ProcessingStep.NORMALIZING ->
        ProcessingUIState(0.3f, "Đang chuẩn hóa văn bản")

    NoteAudioViewModel.ProcessingStep.SUMMARIZING ->
        ProcessingUIState(0.6f, "Đang tóm tắt & trích xuất từ khóa")

    NoteAudioViewModel.ProcessingStep.MINDMAP ->
        ProcessingUIState(0.85f, "Đang tạo Mindmap")

    NoteAudioViewModel.ProcessingStep.DONE ->
        ProcessingUIState(
            1f,
            if (hasMindMap) "Hoàn tất (kèm Mindmap)" else "Hoàn tất"
        )
}

@Composable
fun NoteInfoAndActions(
    isProcessing: Boolean,
    hasMindMap: Boolean,
    currentStep: NoteAudioViewModel.ProcessingStep,
    onRegenerateAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        ActionChip(
            "Xử lý lại",
            painterResource(R.drawable.arrow_up_down),
            onClick = { if (!isProcessing) onRegenerateAll() },
            isLoading = isProcessing,
            enabled = !isProcessing,
            backgroundBrush = MenuBackgroundBrush,
            labelColor = Color.White,
        )
    }
    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        AutoSlidingProcessingBar(
            isProcessing = isProcessing,
            currentStep = currentStep,
            hasMindMap = hasMindMap
        )
    }

    HorizontalDivider(
        color = Color(0xffE5E7EB),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AutoSlidingProcessingBar(
    isProcessing: Boolean,
    currentStep: NoteAudioViewModel.ProcessingStep,
    hasMindMap: Boolean,
) {
    val uiState = mapStepToUI(currentStep, hasMindMap)
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progress,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "processing-progress"
    )
    val fakeProgress by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )

    val finalProgress =
        if (currentStep != NoteAudioViewModel.ProcessingStep.DONE)
            (animatedProgress + fakeProgress).coerceAtMost(0.95f)
        else animatedProgress
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        // Label + %
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.label,
                style = MaterialTheme.typography.bodySmall,
                color = SubTitleColor
            )

            Text(
                text = "${(finalProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = SubTitleColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(finalProgress)
                    .height(8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xff00D2FF),
                                Color(0xff7532FB)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

//@Composable
//private fun StepItem(
//    label: String,
//    step: NoteAudioViewModel.ProcessingStep,
//    current: NoteAudioViewModel.ProcessingStep,
//) {
//    val isActive = current.ordinal >= step.ordinal
//    val color = if (isActive) SubTitleColor else Color.LightGray
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Box(
//            modifier = Modifier
//                .height(20.dp)
//                .background(
//                    brush = if (isActive) Brush.verticalGradient(
//                        0f to Color(0xff00D2FF),
//                        1f to Color(0xff7532FB)
//                    ) else Brush.verticalGradient(0f to Color.LightGray, 1f to Color.LightGray),
//                    shape = RoundedCornerShape(10.dp)
//                )
//                .padding(horizontal = 8.dp),
//        ) {}
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = label,
//            fontSize = 11.sp,
//            color = color,
//        )
//    }
//}
//
//@Composable
//private fun StepDivider() {
//    Spacer(
//        modifier = Modifier
//            .height(1.dp)
//            .background(Color(0xFFE5E7EB))
//    )
//}
