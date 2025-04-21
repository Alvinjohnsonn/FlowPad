package com.staticconstants.flowpad.backend.db.notes;

import com.staticconstants.flowpad.backend.db.DbRecord;

import java.util.LinkedList;
import java.util.UUID;

public class Note implements DbRecord {

    UUID id;
    String filename;
    LinkedList<String> folders;

    public Note(String filename, String... folders)
    {
        this.filename = filename;
        this.folders = new LinkedList<>();
        for(String folder : folders)
        {
            this.folders.add(folder);
        }
    }

    private Note (UUID id , String filename, String... folders)
    {
        this.id = id;
        this.filename = filename;
        this.folders = new LinkedList<>();
        for (String folder : folders) {
            this.folders.add(folder);

        }
    }

    @Override
    public UUID getId() {
        return id;
    }
}
