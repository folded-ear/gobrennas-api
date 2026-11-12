package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Embeddable;

/**
 * I am a geographic position, with its accuracy in meters.
 */
@Embeddable
public record Coordinates(double latitude,
                          double longitude,
                          double accuracy) {}
