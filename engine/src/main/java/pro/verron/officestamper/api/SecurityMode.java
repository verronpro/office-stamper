package pro.verron.officestamper.api;

/// Defines security modes for expression evaluation and similar features.
///
/// - RESTRICTED: Safe-by-default mode. Disables risky capabilities (type lookup, bean resolution,
///   constructor invocation, unrestricted static access) and allows only whitelisted/custom functions
///   and safe instance method/property access.
/// - PERMISSIVE: Enables full SpEL capabilities (as provided by the configured evaluation context
///   factory) intended only for trusted templates.
public enum SecurityMode {
    RESTRICTED,
    PERMISSIVE
}
