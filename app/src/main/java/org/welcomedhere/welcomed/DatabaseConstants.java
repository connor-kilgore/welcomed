package org.welcomedhere.welcomed;

public interface DatabaseConstants {
    enum Request {
        ENTRY,
        REVIEW_POPULATE,
        UPVOTE,
        DOWNVOTE,
        REMOVE_VOTE,
        GET_REVIEW_ID,
        GET_REVIEW_KARMA,
        RETRIEVE,
        GET_TOP_TRAITS,
        GET_MATCHING_TRAITS
    }
}
