package com.example.demo.service;

import com.example.demo.model.dto.CommentResponseDTO;
import com.example.demo.model.dto.CreateCommentDTO;
import com.example.demo.model.entity.*;
import com.example.demo.model.exception.BadRequestException;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserCommentReactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.demo.util.Constants.TAGGED_YOU_IN_HIS_COMMENT;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final NotificationRepository notificationRepository;
    private final UserCommentReactionRepository userCommentReactionRepository;

    @Transactional
    public CommentResponseDTO createComment(CreateCommentDTO createCommentDTO, String authToken) {
        long userId = jwtService.extractUserId(authToken);
        validateCommentData(createCommentDTO);
        User author = userService.findUserById(userId);
        Post ownerPost = postService.findPostById(createCommentDTO.getPostId());

        Comment comment = new Comment();
        comment.setPost(ownerPost);
        comment.setUser(author);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setContent(createCommentDTO.getContent());
        ownerPost.getComments().add(comment);
        if(createCommentDTO.getTaggedUsers() != null && !createCommentDTO.getTaggedUsers().isEmpty()){
            addTaggedUsers(createCommentDTO.getTaggedUsers(), comment);
        }

       if(createCommentDTO.getRepliedCommentId() != null){
           Comment parentComment = findById(createCommentDTO.getRepliedCommentId(),
                   "Can't reply to a nonexistent comment!");
           if(!parentComment.getPost().getId().equals(createCommentDTO.getPostId())){
               throw new BadRequestException("The comment you're replying to belongs to another post!");
           }
           comment.setRepliedComment(parentComment);
           commentRepository.save(parentComment);
       }
       commentRepository.save(comment);
        return modelMapper.map(comment, CommentResponseDTO.class);
    }

    private void addTaggedUsers(List<String> taggedUsers, Comment comment) {
        for (String taggedUser : taggedUsers) {
            User user = userService.findUserByUsername(taggedUser);
            comment.getTaggedUsers().add(user);
            Notification notification = Notification.builder()
                    .user(user)
                    .dateCreated(LocalDateTime.now())
                    .notification(comment.getUser().getUsername()+ TAGGED_YOU_IN_HIS_COMMENT)
                    .build();
            notificationRepository.save(notification);
        }
    }

    private Comment findById(Long commentId, String exceptionMessage) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(exceptionMessage));
    }

    private void validateCommentData(CreateCommentDTO createCommentDTO) {
        if(createCommentDTO.getContent() == null){
            throw new BadRequestException("Can't create a comment with no content!");
        }
        if(createCommentDTO.getPostId() == null){
            throw new BadRequestException("Can't comment a nonexistent post");
        }

    }

    public void react(String authToken, long commentId, boolean status) {
        long userId = jwtService.extractUserId(authToken);
        User user = userService.findUserById(userId);
        Comment comment = findById(commentId,"Comment doesn't exist.");

        if (deleteReactionIfStatusMatches(userId, commentId, status)) {
            return;
        }
        UserCommentReaction userCommentReaction = UserCommentReaction.builder()
                .id(new UserCommentReaction.UserCommentReactionKey(userId, commentId))
                .user(user)
                .comment(comment)
                .status(status)
                .build();
        userCommentReactionRepository.save(userCommentReaction);
    }
    private boolean deleteReactionIfStatusMatches(long userId, long commentId, boolean status) {
        Optional<UserCommentReaction> reaction =
                userCommentReactionRepository.findById(new UserCommentReaction.UserCommentReactionKey(userId, commentId));
        if (reaction.isPresent() && reaction.get().isStatus() == status) {
            userCommentReactionRepository.delete(reaction.get());
            return true;
        }
        return false;
    }
}
