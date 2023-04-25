package com.example.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "users_posts_reactions")
public class UserPostReaction {
    @EmbeddedId
    private UserPostReactionKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    private boolean status;

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserPostReactionKey implements Serializable {
        @Serial
        private static final long serialVersionUID = -5127123850693554092L;
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "post_id")
        private Long postId;
    }
}
