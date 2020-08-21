package com.example.atv.model

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.example.atv.R
import com.example.atv.view.AndroidTreeView
import com.example.atv.view.TreeNodeWrapperView
import java.util.*

/**
 * Created by Bogdan Melnychuk on 2/10/15.
 */
class TreeNode(value: Any?) {
    var id = 0
        private set
    private var mLastId = 0
    var parent: TreeNode? = null
        private set
    private var mSelected = false
    var isSelectable = true
    private val children: MutableList<TreeNode>
    var viewHolder: BaseNodeViewHolder<*>? = null
        private set
    var clickListener: TreeNodeClickListener? = null
        private set
    var longClickListener: TreeNodeLongClickListener? = null
        private set
    val value: Any?
    var isExpanded = false
        private set

    private fun generateId(): Int {
        return ++mLastId
    }

    fun addChild(childNode: TreeNode): TreeNode {
        childNode.parent = this
        childNode.id = generateId()
        children.add(childNode)
        return this
    }

    fun addChildren(vararg nodes: TreeNode): TreeNode {
        for (n in nodes) {
            addChild(n)
        }
        return this
    }

    fun addChildren(nodes: Collection<TreeNode>): TreeNode {
        for (n in nodes) {
            addChild(n)
        }
        return this
    }

    fun deleteChild(child: TreeNode): Int {
        for (i in children.indices) {
            if (child.id == children[i].id) {
                children.removeAt(i)
                return i
            }
        }
        return -1
    }

    fun getChildren(): List<TreeNode> {
        return Collections.unmodifiableList(children)
    }

    fun size(): Int {
        return children.size
    }

    val isLeaf: Boolean
        get() = size() == 0

    fun setExpanded(expanded: Boolean): TreeNode {
        isExpanded = expanded
        return this
    }

    var isSelected: Boolean
        get() = isSelectable && mSelected
        set(selected) {
            mSelected = selected
        }
    val path: String
        get() {
            val path = StringBuilder()
            var node: TreeNode? = this
            while (node!!.parent != null) {
                path.append(node.id)
                node = node.parent
                if (node!!.parent != null) {
                    path.append(NODES_ID_SEPARATOR)
                }
            }
            return path.toString()
        }
    val level: Int
        get() {
            var level = 0
            var root: TreeNode? = this
            while (root!!.parent != null) {
                root = root.parent
                level++
            }
            return level
        }
    val isLastChild: Boolean
        get() {
            if (!isRoot) {
                val parentSize = parent!!.children.size
                if (parentSize > 0) {
                    val parentChildren: List<TreeNode> = parent!!.children
                    return parentChildren[parentSize - 1].id == id
                }
            }
            return false
        }

    fun setViewHolder(viewHolder: BaseNodeViewHolder<*>?): TreeNode {
        this.viewHolder = viewHolder
        if (viewHolder != null) {
            viewHolder.mNode = this
        }
        return this
    }

    fun setClickListener(listener: TreeNodeClickListener?): TreeNode {
        clickListener = listener
        return this
    }

    fun setLongClickListener(listener: TreeNodeLongClickListener?): TreeNode {
        longClickListener = listener
        return this
    }

    val isFirstChild: Boolean
        get() {
            if (!isRoot) {
                val parentChildren: List<TreeNode> = parent!!.children
                return parentChildren[0].id == id
            }
            return false
        }
    val isRoot: Boolean
        get() = parent == null

    fun getRoot(): TreeNode? {
        var root: TreeNode? = this
        while (root!!.parent != null) {
            root = root.parent
        }
        return root
    }

    interface TreeNodeClickListener {
        fun onClick(node: TreeNode?, value: Any?)
    }

    interface TreeNodeLongClickListener {
        fun onLongClick(node: TreeNode?, value: Any?): Boolean
    }

    abstract class BaseNodeViewHolder<E>(protected var context: Context) {
        var treeView: AndroidTreeView? = null
            protected set
        var mNode: TreeNode? = null
        private var mView: View? = null
        var containerStyle = 0
        val view: View?
            get() {
                if (mView != null) {
                    return mView
                }
                val nodeView = nodeView
                val nodeWrapperView = TreeNodeWrapperView(nodeView?.context, containerStyle)
                nodeWrapperView.insertNodeView(nodeView)
                mView = nodeWrapperView
                return mView
            }

        fun setTreeViev(treeViev: AndroidTreeView?) {
            treeView = treeViev
        }

        val nodeView: View?
            get() = createNodeView(mNode, mNode!!.value as E?)
        open val nodeItemsView: ViewGroup?
            get() = view!!.findViewById<View>(R.id.node_items) as ViewGroup
        val isInitialized: Boolean
            get() = mView != null

        abstract fun createNodeView(node: TreeNode?, value: E?): View?
        open fun toggle(active: Boolean) {
            // empty
        }

        fun toggleSelectionMode(editModeEnabled: Boolean) {
            // empty
        }
    }

    companion object {
        const val NODES_ID_SEPARATOR = ":"
        fun root(): TreeNode {
            val root = TreeNode(null)
            root.isSelectable = false
            return root
        }
    }

    init {
        children = ArrayList()
        this.value = value
    }
}