package com.pvasilev.builderapplication.adapters

import android.content.ClipData
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.*
import com.pvasilev.builderapplication.R
import com.pvasilev.builderapplication.models.Model
import com.pvasilev.builderapplication.models.Role
import kotlinx.android.synthetic.main.item_model.view.*
import kotlinx.android.synthetic.main.layout_action.view.*

class ModelsAdapter(private val models: MutableList<Model>, private val roles: List<Role>, private val lastActions: MutableList<() -> Unit>) : androidx.recyclerview.widget.RecyclerView.Adapter<ModelsAdapter.ModelVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelVH {
        val itemVIew = LayoutInflater.from(parent.context).inflate(R.layout.item_model, parent, false)
        return ModelVH(itemVIew)
    }

    override fun onBindViewHolder(holder: ModelVH, position: Int) = holder.bind(models[position])

    override fun getItemCount() = models.size

    fun addModel(model: Model) {
        models.add(model)
        notifyItemInserted(models.size)
    }

    inner class ModelVH(itemVIew: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemVIew), View.OnDragListener, View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
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

        override fun onDrag(v: View, event: DragEvent): Boolean {
            val source = event.localState as View
            if (event.action == DragEvent.ACTION_DROP && source.id == R.id.role_container) {
                val role = roles[source.tag as Int]
                models[v.tag as Int].roles.add(role)
                notifyItemChanged(v.tag as Int)
                lastActions.add {
                    models[v.tag as Int].roles.removeAt(models[v.tag as Int].roles.size - 1)
                    notifyItemChanged((v.tag as Int))
                }
            }
            if (event.action == DragEvent.ACTION_DROP && source.id == R.id.model_container) {
                val from = source.tag as Int
                val to = adapterPosition
                val tmp = models[from]
                models[from] = models[to]
                models[to] = tmp
                notifyDataSetChanged()
                lastActions.add {
                    models[from] = models[to]
                    models[to] = tmp
                    notifyDataSetChanged()
                }
            }
            return true
        }

        fun bind(model: Model) {
            with(itemView) {
                tv_role.text = model.action.title
                actions_container.removeAllViews()
                model.roles.takeLast(3).forEach { action ->
                    val actionView = LayoutInflater.from(context).inflate(R.layout.layout_action, actions_container, false)
                    with(actionView) {
                        tv_action.background = ColorDrawable(action.color)
                    }
                    actions_container.addView(actionView)
                }
                tag = adapterPosition
                setOnDragListener(this@ModelVH)
                setOnTouchListener(this@ModelVH)
            }
        }
    }
}