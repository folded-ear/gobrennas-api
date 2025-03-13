package com.brennaswitzer.cookbook.services.storage;

public interface ScratchFileDetails extends FileDetails {

    void moveTo(String targetFilename);

    void remove();

}
