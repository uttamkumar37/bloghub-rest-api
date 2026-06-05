package com.bloghub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a blog post.
 * Maps to the 'posts' table.
 */
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_author", columnList = "user_id"),
    @Index(name = "idx_posts_created_at", columnList = "created_at"),
    @Index(name = "idx_posts_category_created", columnList = "category, created_at"),
    @Index(name = "idx_posts_deleted_created_id", columnList = "deleted, created_at, id")
})
@SQLDelete(sql = "UPDATE posts SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Category/tag for the post */
    @Column(length = 100)
    private String category;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    /** Many posts belong to one user (author) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /** One post has many comments */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /** Users who liked this post */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> likes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Convenience method to get like count */
    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }
}
