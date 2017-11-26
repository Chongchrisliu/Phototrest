/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwaterloo.ece658.service;

import edu.uwaterloo.ece658.entity.Photo;
import edu.uwaterloo.ece658.entity.Tag;
import edu.uwaterloo.ece658.entity.User;
import edu.uwaterloo.ece658.session.PhotoFacade;
import edu.uwaterloo.ece658.session.TagFacade;
import edu.uwaterloo.ece658.session.UserFacade;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Daniel
 */
@Stateless
public class DataHandlerService {

    @EJB
    private UserFacade userFacade;

    @EJB
    private SubscriptionService subscriptionService;

    @EJB
    private PhotoFacade photoFacade;
    
    @EJB
    private TagFacade tagFacade;

    @EJB
    private S3Service s3Service;
    
    private Photo createNewPhoto(
            String s3key,
            String Md5,
            Integer photoSize, 
            Date uploadDate, 
            boolean isPrivatePhoto)
    {
        Photo photo = new Photo();        
        photo.setS3Key(s3key);
        photo.setMd5(Md5);
        photo.setSize(photoSize);
        photo.setUploadedTime(uploadDate);
        photo.setIsPrivate(isPrivatePhoto);
        photoFacade.create(photo);
        return photo;
    }
    
    private Tag createNewTag(String tagName) {
        Tag tag = new Tag();
        tag.setName(tagName);
        tagFacade.create(tag);
        return tag;
    }
    
    private Tag initializeTag(String tagName) {
        Tag tag = tagFacade.getNormalTagByName(tagName);
        if (tag == null) {
            tag = createNewTag(tagName);
        }
        return tag;
    }
    
    private List<Tag> initializeTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();
        
        for (String tagName : tagNames) {
            tags.add(initializeTag(tagName));
        }
        return tags;
    }
    
    public boolean updateDatabaseWithPhotoInformation(
            String username, 
            String s3key,
            String Md5,
            Integer photoSize, 
            Date uploadDate,
            boolean isPrivatePhoto,
            List<String> tagNames)
    {
        List<Tag> tags = initializeTags(tagNames);
        User user = userFacade.findUserByUserName(username);
        Photo photo = createNewPhoto(
                        s3key,
                        Md5,
                        photoSize,
                        uploadDate,
                        isPrivatePhoto);
        
        photo.setTags(tags);
        photo.addUploadedUser(user);
        photoFacade.edit(photo);
        
        user.addUploadedPhoto(photo);
        userFacade.edit(user);
        
        for (Tag tag : tags) {
            tag.addPhotoUnderThisTag(photo);
            tagFacade.edit(tag);
        }
        
        // call notify to send messages to users
        if (!isPrivatePhoto) {
            return subscriptionService.notifyUsersBySubscribedTags(tags, photo);
        }
        
        return true;
    }
    
    public void removeTagFromPhoto(String username, String s3key, String tagName) {
        Photo photo = photoFacade.getPhotoByS3Key(s3key);
        User user = userFacade.findUserByUserName(username);
        Tag tag = tagFacade.getNormalTagByName(tagName);
        
        //TODO: throw exception if not found
        if (photo.getUploadedUsers().contains(user)) {
            photo.removeTag(tag);
            tag.removePhotoUnderThisTag(photo);
            tagFacade.edit(tag);
            photoFacade.edit(photo);
        }
    }
    
    public void addTagToPhoto(String username, String s3key, String tagName) {
        Photo photo = photoFacade.getPhotoByS3Key(s3key);
        User user = userFacade.findUserByUserName(username);
        Tag tag = initializeTag(tagName);
        
        // TODO: throw exception if not found
        if (photo.getUploadedUsers().contains(user)) {
            photo.addTag(tag);
            tag.addPhotoUnderThisTag(photo);
            tagFacade.edit(tag);
            photoFacade.edit(photo);
        }
    }
    
    public boolean deleteImage(String username, String s3key) {
        Photo photo = photoFacade.getPhotoByS3Key(s3key);
        User user = userFacade.findUserByUserName(username);
        //TODO: throw exception if not found
        if (photo != null && photo.getUploadedUsers().contains(user)) {
            user.removeUploadedPhoto(photo);
            userFacade.edit(user);
            for (Tag tag : photo.getTags()) {
                tag.removePhotoUnderThisTag(photo);
                tagFacade.edit(tag);
            }
            photoFacade.remove(photo);
            
            return s3Service.deleteImage(s3key);
        }
        
        return false;
    }
    
}
