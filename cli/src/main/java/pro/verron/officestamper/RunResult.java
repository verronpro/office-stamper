package pro.verron.officestamper;

import org.jspecify.annotations.Nullable;

/**
 * @param status ok | error
 * @param output nullable
 * @param error  nullable
 */
record RunResult(String name, String status, @Nullable String output, @Nullable String error) {
}
