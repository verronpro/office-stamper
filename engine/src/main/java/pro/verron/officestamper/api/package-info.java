/// Core API interfaces and types for the OfficeStamper library.
///
/// Defines the contract for stamping templates with data, including the main
///  [OfficeStamper][pro.verron.officestamper.api.OfficeStamper] interface,
/// configuration, resolvers, processors, and document part abstractions.
///
/// Ensures non-null values by default.
///
/// <h2>Migrating from 3.8.x</h2>
///
/// This package kept its 3.8 name, so existing imports of
/// {@code pro.verron.officestamper.api.<Name>} still resolve.
/// The configuration types ({@code SecurityMode}, {@code OfficeStamperConfiguration},
/// {@code CustomFunction}, the resolver interfaces, the evaluation-context factories,
/// {@code ExceptionResolver}, {@code TraceabilityReporter}, {@code OfficeStamperException})
/// are unchanged. The document-contract types ({@code Comment}, {@code Paragraph},
/// {@code Insert}, {@code PreProcessor}, {@code PostProcessor}, {@code ProcessorContext},
/// {@code Table}, …) were reshaped to be docx4j-free: every docx4j parameter or return
/// type was replaced by a docx-free adapter, so processors written against raw docx4j
/// objects need porting. {@code OfficeStamper} is now the ServiceLoader service interface
/// that provider artifacts implement; {@code DocxPart} was deleted — use {@code OfficePart}.
/// See the compatibility page of the site documentation for the full per-type mapping.
@NullMarked
package pro.verron.officestamper.api;

import org.jspecify.annotations.NullMarked;
