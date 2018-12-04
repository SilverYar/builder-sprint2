package com.pvasilev.builderapplication.adapters

import android.content.ClipData
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
import com.pvasilev.builderapplication.models.Action
import kotlinx.android.synthetic.main.dialog_action.view.*
import kotlinx.android.synthetic.main.item_action.view.*

class ActionsAdapter(private val actions: MutableList<Action>, private val lastActions: MutableList<() -> Unit>) : androidx.recyclerview.widget.RecyclerView.Adapter<ActionsAdapter.ActionVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_action, parent, false)
        return ActionVH(itemView)
    }

    override fun onBindViewHolder(holder: ActionVH, position: Int) = holder.bind(actions[position])

    override fun getItemCount() = actions.size

    inner class ActionVH(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnTouchListener {

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
                        val action = it.et_action.text.toString()
                    }
                }
                MaterialDialog(itemView.context).customView(R.layout.dialog_action)
                        .title(R.string.title_edit_action)
                        .positiveButton(click = dialogCallback)
                        .negativeButton()
                        .onPreShow {
                            val selectedAction = actions[adapterPosition]
                            it.getCustomView()?.let {
                                it.et_action.text = SpannableStringBuilder(selectedAction.title)
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

        fun bind(action: Action) {
            with(itemView) {
                tv_action.text = action.title
                tv_action.tag = adapterPosition
                setOnTouchListener(this@ActionVH)
                setOnDragListener { v, event ->
                    val source = event.localState as View
                    if (event.action == DragEvent.ACTION_DROP && source.id == R.id.tv_action) {
                        val from = source.tag as Int
                        val to = adapterPosition
                        val tmp = actions[from]
                        actions[from] = actions[to]
                        actions[to] = tmp
                        notifyDataSetChanged()
                        lastActions.add {
                            val tmp = actions[from]
                            actions[from] = actions[to]
                            actions[to] = tmp
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