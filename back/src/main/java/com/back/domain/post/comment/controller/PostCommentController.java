package com.back.domain.post.comment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.dto.CommentDeleteRequest;
import com.back.domain.post.comment.dto.CommentModifyRequest;
import com.back.domain.post.comment.service.PostCommentService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostCommentController {

    private final Rq rq;
    private final PostCommentService postCommentService;

    @Operation(summary = "댓글 생성", description = "comment는 공백이나 Null이 될 수 없습니다. comment의 글자 수 제한은 없습니다.")
    @PostMapping("/{post_id}/comment")
    public RsData<Void> createComment(@PathVariable(name = "post_id") Long postId,
                                      @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        Member member = rq.getActor();
        postCommentService.createComment(member, postId, commentCreateRequest);

        return new RsData<>("200", "댓글 작성 완료", null);
    }

    @Operation(summary = "댓글 다건 조회")
    @GetMapping("/post/{post_id}")
    public RsData<List<CommentAllResponse>> getAllPostComment(@PathVariable(name = "post_id") Long postId) {
        List<CommentAllResponse> postAllResponse = postCommentService.getAllPostCommentResponse(postId);
        return new RsData<>("200", "댓글 조회 성공", postAllResponse);
    }

    @Operation(summary = "댓글 삭제", description = "commentId는 공백이나 Null이 될 수 없습니다.")
    @DeleteMapping("/{post_id}/comment")
    public RsData<Void> removePostComment(@PathVariable(name = "post_id") Long postId

            , @RequestBody @Valid CommentDeleteRequest commentDeleteRequest) {
        Member member = rq.getActor();

        postCommentService.removePostComment(postId, commentDeleteRequest, member);

        return new RsData<>("200", "댓글 삭제 성공", null);
    }

    @Operation(summary = "댓글 수정", description = "commentId, content는 공백이나 Null이 될 수 없습니다. content의 글자 수 제한은 없습니다. ")
    @PutMapping("/{post_id}/comment")
    public RsData<Void> updatePostComment(@PathVariable(name = "post_id") Long postId

            , @Valid @RequestBody CommentModifyRequest commentModifyRequest) {
        Member member = rq.getActor();

        postCommentService.updatePostComment(postId, commentModifyRequest, member);

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
    public RsData<CommentAllResponse> getAdoptComment(@PathVariable(name = "post_id") Long postId) {
        CommentAllResponse commentAllResponse = postCommentService.getAdoptedComment(postId);

        return new RsData<>("200", "채택된 댓글 조회 성공", commentAllResponse);
    }

}
