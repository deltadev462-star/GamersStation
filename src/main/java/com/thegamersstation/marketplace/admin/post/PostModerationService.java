package com.thegamersstation.marketplace.admin.post;

import com.thegamersstation.marketplace.post.Post;
import com.thegamersstation.marketplace.post.PostRepository;
import com.thegamersstation.marketplace.post.PostMapper;
import com.thegamersstation.marketplace.post.dto.PostDto;
import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostModerationService {
    
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    
    @Transactional(readOnly = true)
    public PageResponseDto<PostDto> getPendingPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findByStatus(Post.PostStatus.WAITING_APPROVAL, pageable);
        return PageResponseDto.of(postsPage.map(postMapper::toDto));
    }
    
    @Transactional(readOnly = true)
    public PageResponseDto<PostDto> getAllPosts(Post.PostStatus status, Pageable pageable) {
        Page<Post> postsPage;
        if (status != null) {
            postsPage = postRepository.findByStatus(status, pageable);
        } else {
            postsPage = postRepository.findAll(pageable);
        }
        return PageResponseDto.of(postsPage.map(postMapper::toDto));
    }
    
    @Transactional
    public PostDto approvePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (post.getStatus() != Post.PostStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("Only pending posts can be approved");
        }
        
        post.setStatus(Post.PostStatus.ACTIVE);
        Post updatedPost = postRepository.save(post);
        return postMapper.toDto(updatedPost);
    }
    
    @Transactional
    public PostDto blockPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (post.getStatus() == Post.PostStatus.DELETED) {
            throw new IllegalStateException("Cannot block deleted post");
        }
        
        post.setStatus(Post.PostStatus.BLOCKED);
        Post updatedPost = postRepository.save(post);
        return postMapper.toDto(updatedPost);
    }
    
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        postRepository.delete(post);
    }
}
