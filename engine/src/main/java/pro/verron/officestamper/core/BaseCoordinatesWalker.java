package pro.verron.officestamper.core;

import org.docx4j.wml.P;
import org.docx4j.wml.R;

/**
 * A {@link CoordinatesWalker} that does nothing in the {@link #onRun(R, P)} and {@link #onParagraph(P)} methods.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public abstract class BaseCoordinatesWalker extends CoordinatesWalker {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onRun(R run, P paragraph) {
	}

	/** {@inheritDoc} */
	@Override
	protected void onParagraph(P paragraph) {
	}
}
