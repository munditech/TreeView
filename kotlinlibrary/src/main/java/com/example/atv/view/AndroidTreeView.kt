package com.example.atv.view

import android.content.Context
import android.text.TextUtils
import android.view.ContextThemeWrapper
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.ScrollView
import com.example.atv.R
import com.example.atv.holder.SimpleViewHolder
import com.example.atv.model.TreeNode
import java.util.*

/**
 * Created by Bogdan Melnychuk on 2/10/15.
 */
class AndroidTreeView {
    protected var mRoot: TreeNode? = null
    private var mContext: Context
    private var applyForRoot = false
    private var containerStyle = 0
    private var defaultViewHolderClass: Class<out TreeNode.BaseNodeViewHolder<*>> = SimpleViewHolder::class.java
    private var nodeClickListener: TreeNode.TreeNodeClickListener? = null
    private var nodeLongClickListener: TreeNode.TreeNodeLongClickListener? = null
    private var mSelectionModeEnabled = false
    private var mUseDefaultAnimation = false
    private var use2dScroll = false
    var isAutoToggleEnabled = true
        private set

    constructor(context: Context) {
        mContext = context
    }

    fun setRoot(mRoot: TreeNode?) {
        this.mRoot = mRoot
    }

    constructor(context: Context, root: TreeNode?) {
        mRoot = root
        mContext = context
    }

    fun setDefaultAnimation(defaultAnimation: Boolean) {
        mUseDefaultAnimation = defaultAnimation
    }

    fun setDefaultContainerStyle(style: Int) {
        setDefaultContainerStyle(style, false)
    }

    fun setDefaultContainerStyle(style: Int, applyForRoot: Boolean) {
        containerStyle = style
        this.applyForRoot = applyForRoot
    }

    fun setUse2dScroll(use2dScroll: Boolean) {
        this.use2dScroll = use2dScroll
    }

    fun is2dScrollEnabled(): Boolean {
        return use2dScroll
    }

    fun setUseAutoToggle(enableAutoToggle: Boolean) {
        isAutoToggleEnabled = enableAutoToggle
    }

    fun setDefaultViewHolder(viewHolder: Class<out TreeNode.BaseNodeViewHolder<*>>) {
        defaultViewHolderClass = viewHolder
    }

    fun setDefaultNodeClickListener(listener: TreeNode.TreeNodeClickListener?) {
        nodeClickListener = listener
    }

    fun setDefaultNodeLongClickListener(listener: TreeNode.TreeNodeLongClickListener?) {
        nodeLongClickListener = listener
    }

    fun expandAll() {
        expandNode(mRoot, true)
    }

    fun collapseAll() {
        for (n in mRoot!!.getChildren()) {
            collapseNode(n, true)
        }
    }

    fun getView(style: Int): View {
        val view: ViewGroup
        view = if (style > 0) {
            val newContext = ContextThemeWrapper(mContext, style)
            if (use2dScroll) TwoDScrollView(newContext) else ScrollView(newContext)
        } else {
            if (use2dScroll) TwoDScrollView(mContext) else ScrollView(mContext)
        }
        var containerContext = mContext
        if (containerStyle != 0 && applyForRoot) {
            containerContext = ContextThemeWrapper(mContext, containerStyle)
        }
        val viewTreeItems = LinearLayout(containerContext, null, containerStyle)
        viewTreeItems.id = R.id.tree_items
        viewTreeItems.orientation = LinearLayout.VERTICAL
        view.addView(viewTreeItems)
        mRoot!!.setViewHolder(object : TreeNode.BaseNodeViewHolder<Any?>(mContext) {
            override fun createNodeView(node: TreeNode?, value: Any?): View? {
                return null
            }

            override val nodeItemsView: ViewGroup
                get() = viewTreeItems
        })
        expandNode(mRoot, false)
        return view
    }

    val view: View
        get() = getView(-1)

    fun expandLevel(level: Int) {
        for (n in mRoot!!.getChildren()) {
            expandLevel(n, level)
        }
    }

    private fun expandLevel(node: TreeNode, level: Int) {
        if (node.level <= level) {
            expandNode(node, false)
        }
        for (n in node.getChildren()) {
            expandLevel(n, level)
        }
    }

    fun expandNode(node: TreeNode?) {
        expandNode(node, false)
    }

    fun collapseNode(node: TreeNode) {
        collapseNode(node, false)
    }

    val saveState: String
        get() {
            val builder = StringBuilder()
            getSaveState(mRoot, builder)
            if (builder.length > 0) {
                builder.setLength(builder.length - 1)
            }
            return builder.toString()
        }

    fun restoreState(saveState: String) {
        if (!TextUtils.isEmpty(saveState)) {
            collapseAll()
            val openNodesArray = saveState.split(NODES_PATH_SEPARATOR.toRegex()).toTypedArray()
            val openNodes: Set<String> = HashSet(Arrays.asList(*openNodesArray))
            restoreNodeState(mRoot, openNodes)
        }
    }

    private fun restoreNodeState(node: TreeNode?, openNodes: Set<String>) {
        for (n in node!!.getChildren()) {
            if (openNodes.contains(n.path)) {
                expandNode(n)
                restoreNodeState(n, openNodes)
            }
        }
    }

    private fun getSaveState(root: TreeNode?, sBuilder: StringBuilder) {
        for (node in root!!.getChildren()) {
            if (node.isExpanded) {
                sBuilder.append(node.path)
                sBuilder.append(NODES_PATH_SEPARATOR)
                getSaveState(node, sBuilder)
            }
        }
    }

    fun toggleNode(node: TreeNode) {
        if (node.isExpanded) {
            collapseNode(node, false)
        } else {
            expandNode(node, false)
        }
    }

    private fun collapseNode(node: TreeNode, includeSubnodes: Boolean) {
        node.setExpanded(false)
        val nodeViewHolder = getViewHolderForNode(node)
        if (mUseDefaultAnimation) {
            collapse(nodeViewHolder!!.nodeItemsView)
        } else {
            nodeViewHolder!!.nodeItemsView!!.visibility = View.GONE
        }
        nodeViewHolder.toggle(false)
        if (includeSubnodes) {
            for (n in node.getChildren()) {
                collapseNode(n, includeSubnodes)
            }
        }
    }

    private fun expandNode(node: TreeNode?, includeSubnodes: Boolean) {
        node!!.setExpanded(true)
        val parentViewHolder = getViewHolderForNode(node)
        parentViewHolder!!.nodeItemsView!!.removeAllViews()
        parentViewHolder.toggle(true)
        for (n in node.getChildren()) {
            addNode(parentViewHolder.nodeItemsView, n)
            if (n.isExpanded || includeSubnodes) {
                expandNode(n, includeSubnodes)
            }
        }
        if (mUseDefaultAnimation) {
            expand(parentViewHolder.nodeItemsView)
        } else {
            parentViewHolder.nodeItemsView!!.visibility = View.VISIBLE
        }
    }

    private fun addNode(container: ViewGroup?, n: TreeNode) {
        val viewHolder = getViewHolderForNode(n)
        val nodeView = viewHolder!!.view
        container!!.addView(nodeView)
        if (mSelectionModeEnabled) {
            viewHolder.toggleSelectionMode(mSelectionModeEnabled)
        }
        nodeView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (n.clickListener != null) {
                    n.clickListener!!.onClick(n, n.value)
                } else if (nodeClickListener != null) {
                    nodeClickListener!!.onClick(n, n.value)
                }
                if (isAutoToggleEnabled) {
                    toggleNode(n)
                }
            }
        })
        nodeView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(view: View): Boolean {
                if (n.longClickListener != null) {
                    return n.longClickListener!!.onLongClick(n, n.value)
                } else if (nodeLongClickListener != null) {
                    return nodeLongClickListener!!.onLongClick(n, n.value)
                }
                if (isAutoToggleEnabled) {
                    toggleNode(n)
                }
                return false
            }
        })
    }

    fun <E> getSelectedValues(clazz: Class<E>): List<E> {
        val result: MutableList<E> = ArrayList()
        val selected = selected
        for (n in selected) {
            val value = n.value
            if (value != null && value.javaClass == clazz) {
                result.add(value as E)
            }
        }
        return result
    }// TODO fix double iteration over tree

    //------------------------------------------------------------
    //  Selection methods
    var isSelectionModeEnabled: Boolean
        get() = mSelectionModeEnabled
        set(selectionModeEnabled) {
            if (!selectionModeEnabled) {
                // TODO fix double iteration over tree
                deselectAll()
            }
            mSelectionModeEnabled = selectionModeEnabled
            for (node in mRoot!!.getChildren()) {
                toggleSelectionMode(node, selectionModeEnabled)
            }
        }

    private fun toggleSelectionMode(parent: TreeNode, mSelectionModeEnabled: Boolean) {
        toogleSelectionForNode(parent, mSelectionModeEnabled)
        if (parent.isExpanded) {
            for (node in parent.getChildren()) {
                toggleSelectionMode(node, mSelectionModeEnabled)
            }
        }
    }

    val selected: List<TreeNode>
        get() = if (mSelectionModeEnabled) {
            getSelected(mRoot)
        } else {
            ArrayList()
        }

    // TODO Do we need to go through whole tree? Save references or consider collapsed nodes as not selected
    private fun getSelected(parent: TreeNode?): List<TreeNode> {
        val result: MutableList<TreeNode> = ArrayList()
        for (n in parent!!.getChildren()) {
            if (n.isSelected) {
                result.add(n)
            }
            result.addAll(getSelected(n))
        }
        return result
    }

    fun selectAll(skipCollapsed: Boolean) {
        makeAllSelection(true, skipCollapsed)
    }

    fun deselectAll() {
        makeAllSelection(false, false)
    }

    private fun makeAllSelection(selected: Boolean, skipCollapsed: Boolean) {
        if (mSelectionModeEnabled) {
            for (node in mRoot!!.getChildren()) {
                selectNode(node, selected, skipCollapsed)
            }
        }
    }

    fun selectNode(node: TreeNode, selected: Boolean) {
        if (mSelectionModeEnabled) {
            node.isSelected = selected
            toogleSelectionForNode(node, true)
        }
    }

    private fun selectNode(parent: TreeNode, selected: Boolean, skipCollapsed: Boolean) {
        parent.isSelected = selected
        toogleSelectionForNode(parent, true)
        val toContinue = if (skipCollapsed) parent.isExpanded else true
        if (toContinue) {
            for (node in parent.getChildren()) {
                selectNode(node, selected, skipCollapsed)
            }
        }
    }

    private fun toogleSelectionForNode(node: TreeNode, makeSelectable: Boolean) {
        val holder = getViewHolderForNode(node)
        if (holder!!.isInitialized) {
            getViewHolderForNode(node)!!.toggleSelectionMode(makeSelectable)
        }
    }

    private fun getViewHolderForNode(node: TreeNode?): TreeNode.BaseNodeViewHolder<*>? {
        var viewHolder = node!!.viewHolder
        if (viewHolder == null) {
            try {
                val `object`: Any = defaultViewHolderClass.getConstructor(Context::class.java).newInstance(mContext)
                viewHolder = `object` as TreeNode.BaseNodeViewHolder<*>
                node.setViewHolder(viewHolder)
            } catch (e: Exception) {
                throw RuntimeException("Could not instantiate class $defaultViewHolderClass")
            }
        }
        if (viewHolder!!.containerStyle <= 0) {
            viewHolder.containerStyle = containerStyle
        }
        if (viewHolder.treeView == null) {
            viewHolder.setTreeViev(this)
        }
        return viewHolder
    }

    //-----------------------------------------------------------------
    //Add / Remove
    fun addNode(parent: TreeNode, nodeToAdd: TreeNode) {
        parent.addChild(nodeToAdd)
        if (parent.isExpanded) {
            val parentViewHolder = getViewHolderForNode(parent)
            addNode(parentViewHolder!!.nodeItemsView, nodeToAdd)
        }
    }

    fun removeNode(node: TreeNode) {
        if (node.parent != null) {
            val parent = node.parent
            val index = parent!!.deleteChild(node)
            if (parent.isExpanded && index >= 0) {
                val parentViewHolder = getViewHolderForNode(parent)
                parentViewHolder!!.nodeItemsView!!.removeViewAt(index)
            }
        }
    }

    companion object {
        private const val NODES_PATH_SEPARATOR = ";"
        private fun expand(v: View?) {
            v!!.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            val targetHeight = v.measuredHeight
            v.layoutParams.height = 0
            v.visibility = View.VISIBLE
            val a: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    v.layoutParams.height = if (interpolatedTime == 1f) LinearLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            // 1dp/ms
            a.setDuration((targetHeight / v.context.resources.displayMetrics.density) as Long)
            v.startAnimation(a)
        }

        private fun collapse(v: View?) {
            val initialHeight = v!!.measuredHeight
            val a: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        v.visibility = View.GONE
                    } else {
                        v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                        v.requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            // 1dp/ms
            a.setDuration((initialHeight / v.context.resources.displayMetrics.density) as Long)
            v.startAnimation(a)
        }
    }
}