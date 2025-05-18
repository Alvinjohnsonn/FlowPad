package com.staticconstants.flowpad.backend.db.notes;

import com.staticconstants.flowpad.backend.db.DbRecord;

import java.util.LinkedList;
import java.util.UUID;

public class Note implements DbRecord {

    UUID id;
    long createdTime;
    long lastModifiedTime;
    String filename;
    byte[] serializedText;
    LinkedList<String> folders;

    public Note(String filename, byte[] serializedText, String... folders)
    {
        long time = System.currentTimeMillis();
        this.id = UUID.randomUUID();
        this.createdTime = time;
        this.lastModifiedTime = time;
        this.filename = filename;
        this.serializedText = serializedText;
        this.folders = new LinkedList<>();
        for(String folder : folders)
        {
            this.folders.add(folder);
        }
    }

    private Note (UUID id , long createdTime, long lastModifiedTime, String filename, byte[] serializedText, String... folders)
    {
        this.id = id;
        this.createdTime = createdTime;
        this.lastModifiedTime = lastModifiedTime;
        this.filename = filename;
        this.folders = new LinkedList<>();
        for (String folder : folders) {
            this.folders.add(folder);
        }
    }

    public static Note fromExisting(UUID id , long createdTime, long lastModifiedTime, String filename, byte[] serializedText, String... folders)
    {
        return new Note(id, createdTime, lastModifiedTime, filename, serializedText, folders);
    }

    @Override
    public UUID getId() {
        return id;
    }
}
