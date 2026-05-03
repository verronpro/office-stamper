package pro.verron.officestamper.api;

/// Defines security modes for expression evaluation and similar features.
///
/// - RESTRICTED: Safe-by-default mode. Disables risky capabilities (type lookup, bean resolution,
///   constructor invocation, unrestricted static access) and allows only whitelisted/custom functions
///   and safe instance method/property access.
/// - PERMISSIVE: Enables full SpEL capabilities (as provided by the configured evaluation context
///   factory) intended only for trusted templates.
public enum SecurityMode {
    /// Represents the safe-by-default security mode in which potentially risky features
    /// such as type lookup, bean resolution, constructor invocations, and unrestricted
    /// static access are disabled.
    ///
    /// This mode enforces security and allows only whitelisted/custom functions,
    /// as well as safe instance method and property access during expression evaluation.
    /// It is recommended for scenarios involving untrusted templates.
    RESTRICTED,
    /// Represents the permissive security mode that enables full SpEL (Spring Expression Language)
    /// capabilities as provided by the configured evaluation context factory.
    ///
    /// This mode allows potentially risky features such as type lookup, bean resolution,
    /// constructor invocation, and unrestricted static access. It is intended exclusively
    /// for use with trusted templates where security concerns are not a primary issue.
    PERMISSIVE
}
