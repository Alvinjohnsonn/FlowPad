package com.staticconstants.flowpad.backend;

import com.staticconstants.flowpad.backend.db.notes.Note;
import com.staticconstants.flowpad.backend.db.users.User;

import java.util.HashMap;

public class LoggedInUser {

    public static User user = null;

    public static HashMap<String, Note> notes = null;

}
