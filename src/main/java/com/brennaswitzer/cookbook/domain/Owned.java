package com.brennaswitzer.cookbook.domain;

/**
 * @author bboisvert
 */
public interface Owned {

    User getOwner();

    void setOwner(User owner);

}
