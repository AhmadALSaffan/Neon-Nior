package com.neonnoir.presentation.detail

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MoreSpaceGridDecoration(
    private val spanCount: Int,
    private val horizontalSpacing: Int,
    private val verticalSpacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        // Horizontal spacing
        if (includeEdge) {
            outRect.left = horizontalSpacing - column * horizontalSpacing / spanCount
            outRect.right = (column + 1) * horizontalSpacing / spanCount
        } else {
            outRect.left = column * horizontalSpacing / spanCount
            outRect.right = horizontalSpacing - (column + 1) * horizontalSpacing / spanCount
        }

        outRect.top = if (position < spanCount) verticalSpacing else verticalSpacing / 2
        outRect.bottom = verticalSpacing  // 👈 more space under each item
    }
}