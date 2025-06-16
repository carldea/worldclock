package com.carlfx.worldclock;

/**
 * Record holds a time zone text and description
 *
 * @param timeZone Timezone name such as America/New York
 * @param description Timezone description such as Eastern Standard Time
 */
public record TimeZoneRecord(String timeZone, String description) {
}
