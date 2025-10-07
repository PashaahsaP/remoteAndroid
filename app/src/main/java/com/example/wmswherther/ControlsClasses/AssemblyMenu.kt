package com.example.wmsRemote.ControlsClasses

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.wmsRemote.R

class AssemblyMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        // Подключаем layout внутрь вью
        LayoutInflater.from(context).inflate(R.layout.tasks_menu, this, true)
    }
}