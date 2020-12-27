package com.example.dyanmodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Music")
public class MusicItem {

    private String artist;

    private String song;

    private String ignoredOnGet;

    private String ignoredOnSet;

    @DynamoDBHashKey
    public String get() { return this.artist; }
    public void setId(String artist) { this.artist = artist; }

    @DynamoDBRangeKey
    public String getSong() { return this.song; }
    public void setSong(String artist) { this.song = song; }

    @DynamoDBIgnore
    public String getIgnoredOnGet() { return this.ignoredOnGet; }
    public void setIgnoredOnGet(String val) { this.ignoredOnGet = val; }

    public String getIgnoredOnSet() { return this.ignoredOnSet; }
    @DynamoDBIgnore
    public void setIgnoredOnSet(String val) { this.ignoredOnSet = val;}

}
