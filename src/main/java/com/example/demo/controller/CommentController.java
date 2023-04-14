package com.example.demo.controller;

import com.example.demo.model.dto.CommentResponseDTO;
import com.example.demo.model.dto.CreateCommentDTO;
import com.example.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    public CommentController(@Autowired CommentService commentService){
        this.commentService = commentService;
    }
    @PostMapping()
    public CommentResponseDTO createComment(@RequestBody CreateCommentDTO commentDTO,
                                            @RequestHeader("Authorization") String authToken){
        return commentService.createComment(commentDTO, authToken);
    }

}
