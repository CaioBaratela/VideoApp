package com.example.caioferrari.videoapp.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Date;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */
@Entity(nameInDb = "videos")
public class Videos {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "created_date")
    private Date createdDate;

    @Property(nameInDb = "video_path")
    private String videoPath;

    @Generated(hash = 1082985668)
    public Videos(Long id, Date createdDate, String videoPath) {
        this.id = id;
        this.createdDate = createdDate;
        this.videoPath = videoPath;
    }

    @Generated(hash = 2134900080)
    public Videos() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getVideoPath() {
        return this.videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
    
}
