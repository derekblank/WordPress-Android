package org.wordpress.android.ui.comments.unified

import org.wordpress.android.R
import org.wordpress.android.fluxc.model.CommentStatus

enum class CommentFilter(val labelResId: Int) {
    ALL(R.string.comment_status_all),
    PENDING(R.string.comment_status_unapproved),
    APPROVED(R.string.comment_status_approved),
    UNREPLIED(R.string.comment_status_unreplied),
    TRASHED(R.string.comment_status_trash),
    SPAM(R.string.comment_status_spam),
    DELETE(R.string.comment_status_trash);

    fun toCommentCacheStatuses(): List<CommentStatus> {
        return when (this) {
            ALL -> listOf(CommentStatus.APPROVED, CommentStatus.UNAPPROVED)
            PENDING -> listOf(CommentStatus.UNAPPROVED)
            APPROVED -> listOf(CommentStatus.APPROVED)
            UNREPLIED -> listOf(CommentStatus.UNREPLIED)
            TRASHED -> listOf(CommentStatus.TRASH)
            SPAM -> listOf(CommentStatus.SPAM)
            DELETE -> listOf(CommentStatus.DELETED)
        }
    }

    fun toCommentStatus(): CommentStatus {
        return when (this) {
            ALL -> CommentStatus.ALL
            PENDING -> CommentStatus.UNAPPROVED
            APPROVED -> CommentStatus.APPROVED
            UNREPLIED -> CommentStatus.UNREPLIED
            TRASHED -> CommentStatus.TRASH
            SPAM -> CommentStatus.SPAM
            DELETE -> CommentStatus.DELETED
        }
    }
}
