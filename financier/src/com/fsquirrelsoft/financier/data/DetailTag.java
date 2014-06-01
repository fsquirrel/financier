package com.fsquirrelsoft.financier.data;

import java.io.Serializable;

public class DetailTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id = 0;
    private int detailId;
    private int tagId;

    DetailTag() {
    }

    public DetailTag(int detailId, int tagId) {
        this.detailId = detailId;
        this.tagId = tagId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public int getTagId() {
        return tagId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + detailId;
        result = prime * result + tagId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetailTag other = (DetailTag) obj;
        if (detailId != other.detailId)
            return false;
        if (tagId != other.tagId)
            return false;
        return true;
    }

}
