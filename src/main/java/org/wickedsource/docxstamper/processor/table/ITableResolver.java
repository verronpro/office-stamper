package org.wickedsource.docxstamper.processor.table;

import pro.verron.officestamper.api.StampTable;

/**
 * This interface is used to resolve a table in the template document.
 * The table is passed to the resolveTable method and will be used to fill an existing Tbl object in the document.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.2
 */
public interface ITableResolver {
	/**
	 * Resolves the given table by manipulating the given table in the template
	 *
	 * @param table the table to resolve.
	 */
	void resolveTable(StampTable table);
}
