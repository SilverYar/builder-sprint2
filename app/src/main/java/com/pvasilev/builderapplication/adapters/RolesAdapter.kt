package com.pvasilev.builderapplication.adapters

import android.content.ClipData
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.LinearLayout
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.pvasilev.builderapplication.R
import com.pvasilev.builderapplication.models.Role
import kotlinx.android.synthetic.main.dialog_role.view.*
import kotlinx.android.synthetic.main.item_role.view.*

class RolesAdapter(private val roles: MutableList<Role>, private val lastActions: MutableList<() -> Unit>) : androidx.recyclerview.widget.RecyclerView.Adapter<RolesAdapter.RoleVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RoleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_role, parent, false)
        return RoleVH(itemView)
    }

    override fun onBindViewHolder(holder: RoleVH, position: Int) = holder.bind(roles[position])

    override fun getItemCount() = roles.size

    inner class RoleVH(itemVIew: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemVIew), View.OnTouchListener {

        private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val lp = (itemView.parent as ViewGroup).layoutParams as LinearLayout.LayoutParams
                if (lp.weight.toInt() == 1) {
                    lp.weight = 2.0F
                } else {
                    lp.weight = 1.0F
                }
                (itemView.parent as ViewGroup).layoutParams = lp
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
                            val selectedRole = roles[adapterPosition]
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
        }

        private val gestureDetector = GestureDetector(itemView.context, gestureListener)

        fun bind(role: Role) {
            with(itemView) {
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
}