package com.frizid.timeline.photoeditor

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.frizid.timeline.R


/**
 * Created by Ahmed Adel on 5/8/17.
 */
class ColorPickerAdapter internal constructor(
    private var context: Context?,
    @NonNull colorPickerColors: List<Int>
) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder?>() {
    private var inflater: LayoutInflater
    private val colorPickerColors: List<Int>
    private var onColorPickerClickListener: OnColorPickerClickListener? = null

    internal constructor(context: Context?) : this(context, getDefaultColors(context!!)) {
        this.context = context
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.color_picker_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorPickerView.setBackgroundColor(colorPickerColors[position])
    }

   override fun getItemCount(): Int {
        return colorPickerColors.size
    }

    private fun buildColorPickerView(view: View, colorCode: Int) {
        view.visibility = View.VISIBLE
        val biggerCircle = ShapeDrawable(OvalShape())
        biggerCircle.intrinsicHeight = 20
        biggerCircle.intrinsicWidth = 20
        biggerCircle.bounds = Rect(0, 0, 20, 20)
        biggerCircle.paint.color = colorCode
        val smallerCircle = ShapeDrawable(OvalShape())
        smallerCircle.intrinsicHeight = 5
        smallerCircle.intrinsicWidth = 5
        smallerCircle.bounds = Rect(0, 0, 5, 5)
        smallerCircle.paint.color = Color.WHITE
        smallerCircle.setPadding(10, 10, 10, 10)
        val drawables = arrayOf<Drawable>(smallerCircle, biggerCircle)
        val layerDrawable = LayerDrawable(drawables)
        view.setBackgroundDrawable(layerDrawable)
    }

    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener?) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView: View

        init {
            colorPickerView = itemView.findViewById(R.id.color_picker_view)
            itemView.setOnClickListener {
                if (onColorPickerClickListener != null) onColorPickerClickListener!!.onColorPickerClickListener(
                    colorPickerColors[getAdapterPosition()]
                )
            }
        }
    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int)
    }

    companion object {
        fun getDefaultColors(context: Context): List<Int> {
            val colorPickerColors = ArrayList<Int>()
            colorPickerColors.add(ContextCompat.getColor(context, R.color.blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.black))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.white))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    context!!,
                    R.color.yellow_green_color_picker
                )
            )
            return colorPickerColors
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        this.colorPickerColors = colorPickerColors
    }


}
