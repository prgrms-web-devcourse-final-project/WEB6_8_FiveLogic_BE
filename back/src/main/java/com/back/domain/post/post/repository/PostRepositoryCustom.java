package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> searchPosts(String keyword, Pageable pageable);
}
