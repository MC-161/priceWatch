/**
 * M1: scheduling. Targets poll themselves, each on its own interval.
 *
 * <p>Decisions to make and record in the README:
 * <ul>
 *   <li>Spring {@code @Scheduled}, a DB-backed job table, or something else? What does each cost with two instances?
 *   <li>Is "next due" computed or stored?
 *   <li>After downtime, are missed polls skipped or caught up?
 * </ul>
 */
package dev.pricewatch.schedule;
