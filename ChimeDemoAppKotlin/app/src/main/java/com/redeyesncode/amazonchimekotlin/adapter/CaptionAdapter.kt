package com.redeyesncode.amazonchimekotlin.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.chime.sdk.meetings.audiovideo.TranscriptItemType
import com.redeyesncode.amazonchimekotlin.R
import com.redeyesncode.amazonchimekotlin.data.Caption
import com.redeyesncode.amazonchimekotlin.utils.inflate

import com.redeyesncode.amazonchimekotlin.R.layout.row_caption
import com.redeyesncode.amazonchimekotlin.databinding.RowCaptionBinding
import kotlinx.coroutines.NonDisposableHandle.parent

class CaptionAdapter(
    private val captions: Collection<Caption>,private var context:Context
) :
    RecyclerView.Adapter<CaptionHolder>() {

    private lateinit var binding :RowCaptionBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptionHolder {

        binding = RowCaptionBinding.inflate(LayoutInflater.from(context),parent,false)
        val inflatedView = parent.inflate(R.layout.row_caption, false)
        return CaptionHolder(inflatedView,binding)
    }

    override fun getItemCount(): Int {
        return captions.size
    }

    override fun onBindViewHolder(holder: CaptionHolder, position: Int) {
        holder.bindCaption(captions.elementAt(position), position)
    }
}

class CaptionHolder(inflatedView: View,binding: RowCaptionBinding) :
    RecyclerView.ViewHolder(inflatedView) {

    companion object {
        private const val UPPER_THRESHOLD = 0.3
        private const val LOWER_THRESHOLD = 0.0
        private const val FILTERED_CAPTION_FIRST_INDEX = "["
    }

    private var view: View = inflatedView
    private var binding = RowCaptionBinding.bind(view)

    fun bindCaption(caption: Caption, position: Int) {
        val speakerName = caption.speakerName ?: ""

        val captionTextBackgroundColor = caption.speakerName?.let { R.color.colorWhite } ?: R.color.colorMissingSpeaker
        binding.captionText.setBackgroundResource(captionTextBackgroundColor)
        binding.speakerName.text = speakerName
        binding.captionText.text = caption.content
        val spannable = SpannableString(caption.content)
        caption.entities?.let { contents ->
            // Highlight PII identified and redacted words.
            contents.forEach { word ->
                spannable.setSpan(
                    ForegroundColorSpan(Color.GREEN),
                    caption.content.indexOf(word),
                    caption.content.indexOf(word) + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } ?: run { binding.captionText.setTextColor(Color.BLACK) }
        caption.items?.let { items ->
            // Underline words with low confidence score.
            items.forEach { item ->
                val word = item.content
                val hasLowConfidence = item.confidence?.let { it > LOWER_THRESHOLD && it < UPPER_THRESHOLD } ?: run { false }
                val isCorrectContentType = !word.startsWith(FILTERED_CAPTION_FIRST_INDEX) && item.type != TranscriptItemType.Punctuation
                if (hasLowConfidence && isCorrectContentType && caption.content.contains(word)) {
                    spannable.setSpan(
                        CustomUnderlineSpan(Color.RED, caption.content.indexOf(word), caption.content.indexOf(word) + word.length),
                        caption.content.indexOf(word),
                        caption.content.indexOf(word) + word.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        binding.captionText.text = spannable
        binding.captionText.contentDescription = "caption-$position"
    }
}

private class CustomUnderlineSpan(underlineColor: Int, underlineStart: Int, underlineEnd: Int) : LineBackgroundSpan {

    var color: Int
    var start: Int
    var end: Int
    var underlinePaint: Paint

    init {
        this.color = underlineColor
        this.start = underlineStart
        this.end = underlineEnd
        underlinePaint = Paint()
        underlinePaint.setColor(color)
        underlinePaint.strokeWidth = 3F
        underlinePaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int
    ) {
        if (this.end <= start) return
        if (this.start >= end || this.start < 0) return

        var offsetX = 0
        if (this.start > start) {
            offsetX = paint.measureText(text.subSequence(start, this.start).toString()).toInt()
        }

        val length = paint.measureText(text.subSequence(
            Math.max(start, this.start),
            Math.min(end, this.end)
        ).toString()).toInt()
        canvas.drawLine(offsetX.toFloat(), baseline + 3F, (length + offsetX).toFloat(), baseline + 3F, this.underlinePaint)
    }
}