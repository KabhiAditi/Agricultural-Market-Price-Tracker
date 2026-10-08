package com.ampt.entity;

public enum Role {
    ENTRY_USER,   // can create/edit price records
    VIEWER        // read-only (default for anonymous/search-only usage)
}
