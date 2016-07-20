package es.icarto.gvsig.sixhiara.forms;

import java.io.InputStream;

import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.navtableforms.AbstractForm;

@SuppressWarnings("serial")
public class BarragensForm extends AbstractForm {

	public static final String LAYERNAME = "Barragens";
	public static final String PKFIELD = "cod_barra";
	public static final String ABEILLE = "forms/barragens.xml";
	public static final String METADATA = "rules/barragens.xml";

	public BarragensForm(FLyrVect layer) {
		super(layer);
	}

	@Override
	public FormPanel getFormBody() {
		if (formBody == null) {
			InputStream stream = getClass().getClassLoader()
					.getResourceAsStream(ABEILLE);
			try {
				formBody = new FormPanel(stream);
			} catch (FormException e) {
				e.printStackTrace();
			}
		}
		return formBody;
	}

	@Override
	public String getXMLPath() {
		return this.getClass().getClassLoader().getResource(METADATA).getPath();
	}

	@Override
	protected void fillSpecificValues() {
		super.fillSpecificValues();
	}

	@Override
	protected String getPrimaryKeyValue() {
		return getFormController().getValue(PKFIELD);
	}
}