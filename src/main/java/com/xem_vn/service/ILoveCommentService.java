package com.xem_vn.service;

import com.xem_vn.model.AppUser;
import com.xem_vn.model.Comment;
import com.xem_vn.model.Like;
import com.xem_vn.model.LoveComment;

public interface ILoveCommentService {
    Iterable<LoveComment> findAllByComment(Comment comment);
    Iterable<LoveComment> findAllByAppUser(AppUser user);
    boolean existsByAppUserAndComment(AppUser appUser, Comment comment);
    Long countAllByComment (Comment comment);
    LoveComment getByAppUserAndComment (AppUser user,Comment comment);
    void save(LoveComment loveComment);
    void remove(LoveComment loveComment);
}