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
    boolean isNewNote;

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
        this.isNewNote = true;
    }

    private Note (UUID id , long createdTime, long lastModifiedTime, String filename, byte[] serializedText, String... folders)
    {
        this.id = id;
        this.createdTime = createdTime;
        this.lastModifiedTime = lastModifiedTime;
        this.filename = filename;
        this.serializedText = serializedText;
        this.folders = new LinkedList<>();
        for (String folder : folders) {
            this.folders.add(folder);
        }
    }

    private Note (UUID id , long createdTime, long lastModifiedTime, String filename, byte[] serializedText, LinkedList<String> folders)
    {
        this.id = id;
        this.createdTime = createdTime;
        this.lastModifiedTime = lastModifiedTime;
        this.filename = filename;
        this.folders = folders;
    }

    public static Note fromExisting(UUID id , boolean newNote, long createdTime, long lastModifiedTime, String filename, byte[] serializedText, String... folders)
    {
        Note note = new Note(id, createdTime, lastModifiedTime, filename, serializedText, folders);
        note.isNewNote = newNote;
        return note;
    }

    public static Note fromExisting(UUID id , boolean newNote, long createdTime, long lastModifiedTime, String filename, byte[] serializedText, LinkedList<String> folders)
    {
        Note note = new Note(id, createdTime, lastModifiedTime, filename, serializedText, folders);
        note.isNewNote = newNote;
        return note;
    }

    @Override
    public UUID getId() {
        return id;
    }


    public long getLastModifiedTime() {
        return lastModifiedTime;
    }


    public LinkedList<String> getFolders()
    {
        return folders;
    }

    public String getFilename() {
        return filename;
    }
    
    public boolean isNewNote() {
        return isNewNote;
    }

    public void existingNote() { this.isNewNote = false; }
    
    public void setFilename(String newName) {
        this.filename = newName;
    }

    public byte[] getSerializedText() { return serializedText; }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 17;
        result = 31 * result + Long.hashCode(createdTime);
        result = 31 * result + (folders != null ? folders.hashCode() : 19);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Note other = (Note) obj;
        return id != null && id.equals(other.id);
    }

    public void setSerializedText(byte[] serializedText) { this.serializedText = serializedText; }

    public long getCreatedTime() {
        return createdTime;
    }
}
