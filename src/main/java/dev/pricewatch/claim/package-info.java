/**
 * M2: concurrent job claiming. Two instances, one job table, each due job claimed exactly once.
 *
 * <p>Order of work:
 * <ol>
 *   <li>Claim naively at a weak isolation level and reproduce the double-claim. Record the logs.
 *   <li>Fix it (row lock, version column, or lease) and prove it under the same load, against real Postgres.
 *   <li>Kill an instance mid-poll; the job must be retried, not orphaned.
 * </ol>
 */
package dev.pricewatch.claim;
