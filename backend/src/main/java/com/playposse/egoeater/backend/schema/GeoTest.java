package com.playposse.egoeater.backend.schema;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by thoma on 4/10/2017.
 */
@Entity
@Cache
public class GeoTest {

    @Id
    private Long id;
    private String name;
    @Index
    private GeoPt geoPt;

    public GeoTest(String name, double latitude, double longitude) {
        GeoPt geoPt = new GeoPt((float) latitude, (float) longitude);
        this.geoPt = geoPt;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPt getGeoPt() {
        return geoPt;
    }

    public void setGeoPt(GeoPt geoPt) {
        this.geoPt = geoPt;
    }
}
