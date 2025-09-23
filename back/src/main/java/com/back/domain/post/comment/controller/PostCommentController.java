package com.back.domain.post.comment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.service.PostCommentService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post/comment")
@RequiredArgsConstructor
public class PostCommentController {
    @Autowired
    private Rq rq;
    @Autowired
    private PostCommentService postCommentService;

    @Operation(summary = "댓글 생성")
    @PostMapping("/{post_id}")
    public RsData<Void> createComment(@PathVariable Long post_id,
                                  @Valid @RequestBody CommentCreateRequest commentCreateRequest
                                  ) {
        Member member = rq.getActor();
        postCommentService.createComment(member, post_id, commentCreateRequest);

        return new RsData<>("200", "댓글 작성 완료" , null);
    }

    @Operation(summary = "댓글 다건 조회")
    @GetMapping("/{post_id}")
    @Transactional(readOnly = true)
    public RsData<List<CommentAllResponse>> getAllPostComment(@PathVariable Long post_id) {
        List<CommentAllResponse> postAllResponse = postCommentService.getAllPostCommentResponse(post_id);
        return new RsData<>("200", "게시글 다건 조회 성공", postAllResponse);
    }
}
