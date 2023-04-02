package com.example.demo.service;

import com.example.demo.model.entity.Hashtag;
import com.example.demo.model.entity.Post;
import com.example.demo.repository.HashtagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TagService {
    private final HashtagRepository hashtagRepository;

    public TagService(@Autowired HashtagRepository hashtagRepository) {
        this.hashtagRepository = hashtagRepository;
    }

    //add nonexistent tags to db, if they exist add them to posts' set
    public void addHashTags(List<String> tagsToAdd, Post post) {
        for (String postHashtag : tagsToAdd) {
            Hashtag hashtag = hashtagRepository.findByTagName(postHashtag);
            if (hashtag == null) {
                hashtag = createHashtag(postHashtag, post);
            }
            post.getHashtags().add(hashtag);
        }
    }

    private Hashtag createHashtag(String hashtagName, Post post) {
        Hashtag hashtag = new Hashtag();
        hashtag.getPosts().add(post);
        hashtag.setTagName(hashtagName);
        return hashtagRepository.save(hashtag);
    }
}