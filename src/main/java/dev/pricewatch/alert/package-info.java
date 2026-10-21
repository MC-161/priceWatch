/**
 * M3: exactly-once alerting. One threshold crossing, one delivered notification, always.
 *
 * <p>Open questions:
 * <ul>
 *   <li>How do "decide" (DB transaction) and "send" (external call) become effectively atomic?
 *   <li>What does an idempotency key do when a send times out?
 *   <li>What state stops an oscillating price from causing an alert storm, and when does it re-arm?
 * </ul>
 */
package dev.pricewatch.alert;
