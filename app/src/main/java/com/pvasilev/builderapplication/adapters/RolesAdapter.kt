package com.pvasilev.builderapplication.adapters

import android.content.ClipData
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.pvasilev.builderapplication.R
import com.pvasilev.builderapplication.children
import com.pvasilev.builderapplication.inflate
import com.pvasilev.builderapplication.models.CompositeRole
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dialog_role.view.*
import kotlinx.android.synthetic.main.item_role.view.*
import kotlin.math.abs

class RolesAdapter(private val roles: MutableList<CompositeRole>, private val lastActions: MutableList<() -> Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): RecyclerView.ViewHolder =
            if (itemType == ITEM_CONDITION) {
                val itemView = parent.inflate(R.layout.item_condition)
                ConditionVH(itemView)
            } else {
                val itemView = parent.inflate(R.layout.item_role)
                RoleVH(itemView)
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RoleVH -> holder.bind(roles[position] as CompositeRole.Role)
            is ConditionVH -> holder.bind()
        }
    }

    override fun getItemCount() = roles.size

    override fun getItemViewType(position: Int): Int =
            if (position == 0) {
                ITEM_CONDITION
            } else {
                ITEM_ROLE
            }

    inner class RoleVH(itemVIew: View) : RecyclerView.ViewHolder(itemVIew), View.OnTouchListener {

        private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val listsContainer = itemView.parent.parent as ViewGroup
                val controlsContainer = (listsContainer.parent as ViewGroup).controls_container
                val buttons = controlsContainer.children()
                val rvActions = listsContainer.rv_actions
                val rvHeroes = listsContainer.rv_heroes
                val rvModels = listsContainer.rv_models
                val recyclerViews = listOf(rvActions, rvHeroes, rvModels)
                val weights = if ((rvHeroes.layoutParams as LinearLayout.LayoutParams).weight == 6.0F) {
                    listOf(6.0F, 1.5F, 1.5F)
                } else {
                    listOf(1.5F, 6.0F, 1.5F)
                }
                weights.forEachIndexed { index, weight ->
                    var lp = recyclerViews[index].layoutParams as LinearLayout.LayoutParams
                    lp.weight = weight
                    recyclerViews[index].layoutParams = lp
                    lp = buttons[index].layoutParams as LinearLayout.LayoutParams
                    lp.weight = weight
                    buttons[index].layoutParams = lp
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                val dialogCallback: DialogCallback = {
                    it.getCustomView()?.let {
                        val role = it.et_role.text.toString()
                        val color = it.et_color.text.toString()
                    }
                }
                MaterialDialog(itemView.context).customView(R.layout.dialog_role)
                        .title(R.string.title_edit_role)
                        .positiveButton(click = dialogCallback)
                        .negativeButton()
                        .onPreShow {
                            val selectedRole = roles[adapterPosition] as CompositeRole.Role
                            it.getCustomView()?.let {
                                it.et_role.text = SpannableStringBuilder(selectedRole.name)
                                it.et_color.text = SpannableStringBuilder(selectedRole.color.toString())
                            }
                        }
                        .show()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val clipData = ClipData.newPlainText("", "")
                val shadowBuilder = View.DragShadowBuilder(itemView)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    itemView.startDragAndDrop(clipData, shadowBuilder, itemView, 0)
                } else {
                    itemView.startDrag(clipData, shadowBuilder, itemView, 0)
                }
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val dx = abs(e2.x - e1.x)
                if (itemView.width / dx > 0.5) {
                    val action = roles.removeAt(adapterPosition)
                    val position = adapterPosition
                    notifyItemRemoved(position)
                    lastActions.add {
                        roles.add(position, action)
                        notifyItemInserted(position)
                    }
                }
                return true
            }
        }

        private val gestureDetector = GestureDetector(itemView.context, gestureListener)

        private fun changeHeight() {
            val screenHeight = itemView.resources.displayMetrics.heightPixels;
            val a = screenHeight / 16.0F
            val measuredHeight = 7 * a / 8
            val lp = itemView.layoutParams
            lp.height = measuredHeight.toInt()
            itemView.layoutParams = lp
        }

        fun bind(role: CompositeRole.Role) {
            with(itemView) {
                changeHeight()
                view_badge.background = ColorDrawable(role.color)
                tv_name.text = role.name
                tag = adapterPosition
                setOnTouchListener(this@RoleVH)
                setOnDragListener { v, event ->
                    val source = event.localState as View
                    if (event.action == DragEvent.ACTION_DROP && source.id == R.id.role_container) {
                        val from = source.tag as Int
                        val to = adapterPosition
                        val tmp = roles[from]
                        roles[from] = roles[to]
                        roles[to] = tmp
                        notifyDataSetChanged()
                        lastActions.add {
                            val tmp = roles[from]
                            roles[from] = roles[to]
                            roles[to] = tmp
                            notifyDataSetChanged()
                        }
                    }
                    true
                }
            }
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }
    }

    inner class ConditionVH(itemVIew: View) : RecyclerView.ViewHolder(itemVIew), View.OnTouchListener {

        private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val listsContainer = itemView.parent.parent as ViewGroup
                val controlsContainer = (listsContainer.parent as ViewGroup).controls_container
                val buttons = controlsContainer.children()
                val rvActions = listsContainer.rv_actions
                val rvHeroes = listsContainer.rv_heroes
                val rvModels = listsContainer.rv_models
                val recyclerViews = listOf(rvActions, rvHeroes, rvModels)
                val weights = if ((rvHeroes.layoutParams as LinearLayout.LayoutParams).weight == 6.0F) {
                    listOf(6.0F, 1.5F, 1.5F)
                } else {
                    listOf(1.5F, 6.0F, 1.5F)
                }
                weights.forEachIndexed { index, weight ->
                    var lp = recyclerViews[index].layoutParams as LinearLayout.LayoutParams
                    lp.weight = weight
                    recyclerViews[index].layoutParams = lp
                    lp = buttons[index].layoutParams as LinearLayout.LayoutParams
                    lp.weight = weight
                    buttons[index].layoutParams = lp
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val clipData = ClipData.newPlainText("", "")
                val shadowBuilder = View.DragShadowBuilder(itemView)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    itemView.startDragAndDrop(clipData, shadowBuilder, itemView, 0)
                } else {
                    itemView.startDrag(clipData, shadowBuilder, itemView, 0)
                }
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        }

        private val gestureDetector = GestureDetector(itemView.context, gestureListener)

        fun bind() {
            with(itemView) {
                changeHeight()
                tag = adapterPosition
                setOnTouchListener(this@ConditionVH)

            }
        }

        private fun changeHeight() {
            val screenHeight = itemView.resources.displayMetrics.heightPixels;
            val a = screenHeight / 16.0F
            val measuredHeight = 7 * a / 8
            val lp = itemView.layoutParams
            lp.height = measuredHeight.toInt()
            itemView.layoutParams = lp
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }
    }

    companion object {
        private const val ITEM_CONDITION = 0

        private const val ITEM_ROLE = 1
    }
}