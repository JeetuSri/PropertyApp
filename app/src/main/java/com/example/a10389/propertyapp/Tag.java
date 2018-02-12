package com.example.a10389.propertyapp;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

import java.util.UUID;

/**
 * Created by 10389 on 11/30/2017.
 */

public class Tag implements ChipInterface {
    private String locationTag;

    public String getLocationTag() {
        return locationTag;
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }

    @Override
    public Object getId() {
        return UUID.randomUUID();
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return locationTag;
    }

    @Override
    public String getInfo() {
        return locationTag;
    }

}
