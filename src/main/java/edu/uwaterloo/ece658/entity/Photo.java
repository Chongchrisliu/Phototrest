/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwaterloo.ece658.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.SEQUENCE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author mier
 */
@Entity
@Table(name = "T_PHOTO")
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "Photo_Seq", allocationSize = 5000, initialValue = 6)
    @GeneratedValue(strategy = SEQUENCE, generator = "Photo_Seq")
    private Long id;

    @Column(unique = true, nullable = false)
    private String s3Key;

    private Integer size;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedTime;

    private String md5;

    private boolean isPrivate = false;

    @ManyToMany(mappedBy = "uploadedPhotos")
    private List<User> uploadedUsers = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "T_TAG_PHOTO",
            joinColumns = {
                @JoinColumn(name = "PHOTO_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "TAG_ID", referencedColumnName = "ID")})
    private List<Tag> tags = new ArrayList();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Date getUploadedTime() {
        return uploadedTime;
    }

    public void setUploadedTime(Date uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    public List<User> getUploadedUsers() {
        return uploadedUsers;
    }

    public void setUploadedUsers(List<User> uploadedUsers) {
        this.uploadedUsers = uploadedUsers;
    }

    public void addUploadedUser(User user) {
        this.uploadedUsers.add(user);
    }

    public void removeUploadedUser(User user) {
        this.uploadedUsers.remove(user);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Photo)) {
            return false;
        }
        Photo other = (Photo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.uwaterloo.ece658.entity.Photo[ id=" + id + " ]";
    }

}
