/// Core API interfaces and types for the OfficeStamper library.
///
/// Defines the contract for stamping templates with data, including the main
///  [OfficeStamper][pro.verron.officestamper.api.OfficeStamper] interface,
/// configuration, resolvers, processors, and document part abstractions.
///
/// Ensures non-null values by default.
///
/// ## Migrating from 3.8.x
///
/// This package kept its 3.8 name, so existing imports of
/// `pro.verron.officestamper.api.<Name>` still resolve.
/// The configuration types (`SecurityMode`, `OfficeStamperConfiguration`,
/// `CustomFunction`, the resolver interfaces, the evaluation-context factories,
/// `ExceptionResolver`, `TraceabilityReporter`, `OfficeStamperException`)
/// are unchanged. The document-contract types (`Comment`, `Paragraph`,
/// `Insert`, `PreProcessor`, `PostProcessor`, `ProcessorContext`,
/// `Table`, …) were reshaped to be docx4j-free: every docx4j parameter or return
/// type was replaced by a docx-free adapter, so processors written against raw docx4j
/// objects need porting. `OfficeStamper` is now the ServiceLoader service interface
/// that provider artifacts implement; `DocxPart` was deleted — use `OfficePart`.
/// `OfficeStamper` and `StreamStamper` are no longer generic — they exchange
/// docx-free `OfficeDocument` adapters exclusively.
/// See the compatibility page of the site documentation for the full per-type mapping.
@NullMarked
package pro.verron.officestamper.api;

import org.jspecify.annotations.NullMarked;
