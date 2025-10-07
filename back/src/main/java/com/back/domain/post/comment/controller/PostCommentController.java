package com.back.domain.post.comment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.dto.CommentDeleteRequest;
import com.back.domain.post.comment.dto.CommentModifyRequest;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.service.PostCommentService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post/comment")
@RequiredArgsConstructor
public class PostCommentController {

    private final Rq rq;
    private final PostCommentService postCommentService;

    @Operation(summary = "댓글 생성")
    @PostMapping("/post/{post_id}")
    public RsData<Void> createComment(@PathVariable Long post_id,
                                      @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        Member member = rq.getActor();
        postCommentService.createComment(member, post_id, commentCreateRequest);

        return new RsData<>("200", "댓글 작성 완료", null);
    }

    @Operation(summary = "댓글 다건 조회")
    @GetMapping("/post/{post_id}")
    @Transactional(readOnly = true)
    public RsData<List<CommentAllResponse>> getAllPostComment(@PathVariable Long post_id) {
        List<CommentAllResponse> postAllResponse = postCommentService.getAllPostCommentResponse(post_id);
        return new RsData<>("200", "게시글 다건 조회 성공", postAllResponse);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/post/{post_id}/comment")
    public RsData<Void> removePostComment(@PathVariable @Positive Long post_id
            , @RequestBody @Valid CommentDeleteRequest commentDeleteRequest) {
        Member member = rq.getActor();

        postCommentService.removePostComment(post_id, commentDeleteRequest, member);

        return new RsData<>("200", "게시글 삭제 성공", null);
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/post/{post_id}/comment/")
    public RsData<Void> updatePostComment(@PathVariable Long post_id
            , @Valid @RequestBody CommentModifyRequest commentModifyRequest) {
        Member member = rq.getActor();

        postCommentService.updatePostComment(post_id, commentModifyRequest, member);

        return new RsData<>("200", "댓글 수정 성공", null);
    }
    @Operation(summary = "댓글 채택")
    @PostMapping("isAdopted/{commentId}")
    public RsData<Void> adoptComment(@PathVariable Long commentId) {
        Member member = rq.getActor();
        postCommentService.adoptComment(commentId, member);
        return new RsData<>("200", "댓글 채택 성공", null);
    }

    @Operation(summary = "채택된 댓글 가져오기 ")
    @GetMapping("isAdopted/{post_id}")
    public RsData<CommentAllResponse> getAdoptComment(@PathVariable Long post_id) {
        CommentAllResponse commentAllResponse = postCommentService.getAdoptedComment(post_id);

        return new RsData<>("200", "채택된 댓글 조회 성공", commentAllResponse);
    }

}
