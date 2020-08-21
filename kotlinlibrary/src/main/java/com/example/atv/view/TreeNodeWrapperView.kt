package com.example.atv.view

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.example.atv.R

/**
 * Created by Bogdan Melnychuk on 2/10/15.
 */
class TreeNodeWrapperView(context: Context?, private val containerStyle: Int) : LinearLayout(context) {
    private var nodeItemsContainer: LinearLayout? = null
    var nodeContainer: ViewGroup? = null
        private set

    private fun init() {
        orientation = VERTICAL
        nodeContainer = RelativeLayout(context)
        (nodeContainer as RelativeLayout).setLayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        (nodeContainer as RelativeLayout).setId(R.id.node_header)
        val newContext = ContextThemeWrapper(context, containerStyle)
        nodeItemsContainer = LinearLayout(newContext, null, containerStyle)
        nodeItemsContainer!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        nodeItemsContainer!!.id = R.id.node_items
        nodeItemsContainer!!.orientation = VERTICAL
        nodeItemsContainer!!.visibility = GONE
        addView(nodeContainer)
        addView(nodeItemsContainer)
    }

    fun insertNodeView(nodeView: View?) {
        nodeContainer!!.addView(nodeView)
    }

    init {
        init()
    }
}