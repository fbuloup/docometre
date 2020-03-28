/*******************************************************************************
 * Copyright or © or Copr. Institut des Sciences du Mouvement 
 * (CNRS & Aix Marseille Université)
 * 
 * The DOCoMETER Software must be used with a real time data acquisition 
 * system marketed by ADwin (ADwin Pro and Gold, I and II) or an Arduino 
 * Uno. This software, created within the Institute of Movement Sciences, 
 * has been developed to facilitate their use by a "neophyte" public in the 
 * fields of industrial computing and electronics.  Students, researchers or 
 * engineers can configure this acquisition system in the best possible 
 * conditions so that it best meets their experimental needs. 
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 * 
 * Contributors:
 *  - Frank Buloup - frank.buloup@univ-amu.fr - initial API and implementation [25/03/2020]
 ******************************************************************************/
package fr.univamu.ism.docometre.dacqsystems.arduinouno.ui.processeditor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;

import fr.univamu.ism.docometre.DocometreApplication;

public class ArduinoUnoSourceViewerConfiguration extends SourceViewerConfiguration {
	
	private PresentationReconciler presentationReconciler;

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		if(presentationReconciler != null) return presentationReconciler;
		
		presentationReconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);
		
		DefaultDamagerRepairer defaultDamagerRepairer = new DefaultDamagerRepairer(ArduinoUnoCodeScanner.getCommentScanner());		
		presentationReconciler.setDamager(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.COMMENT);
		presentationReconciler.setRepairer(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.COMMENT);
		
		defaultDamagerRepairer = new DefaultDamagerRepairer(ArduinoUnoCodeScanner.getIncludeScanner());		
		presentationReconciler.setDamager(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.INCLUDE);
		presentationReconciler.setRepairer(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.INCLUDE);
		
		defaultDamagerRepairer = new DefaultDamagerRepairer(ArduinoUnoCodeScanner.getDefineScanner());		
		presentationReconciler.setDamager(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.DEFINE);
		presentationReconciler.setRepairer(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.DEFINE);
		
		defaultDamagerRepairer = new DefaultDamagerRepairer(ArduinoUnoCodeScanner.getSegmentScanner());		
		presentationReconciler.setDamager(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.SEGMENT);
		presentationReconciler.setRepairer(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.SEGMENT);
		
		defaultDamagerRepairer = new DefaultDamagerRepairer(ArduinoUnoCodeScanner.getReservedWordsScanner());		
		presentationReconciler.setDamager(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.RESERVED_WORDS);
		presentationReconciler.setRepairer(defaultDamagerRepairer, ArduinoUnoRulesPartitionScanner.RESERVED_WORDS);
		
		
		//
		RuleBasedScanner defaultScanner = new RuleBasedScanner();
		TextAttribute attribute = new TextAttribute(DocometreApplication.getColor(DocometreApplication.BLUE),
													DocometreApplication.getColor(DocometreApplication.WHITE), 
													SWT.NORMAL,
													DocometreApplication.getFont(DocometreApplication.COURIER_NEW));
		IToken token = new Token(attribute);
		NumberRule numberRule = new NumberRule(token);		
		defaultScanner.setRules(new IRule[]{numberRule});
		defaultDamagerRepairer = new DefaultDamagerRepairer(defaultScanner);		
		presentationReconciler.setDamager(defaultDamagerRepairer, IDocument.DEFAULT_CONTENT_TYPE);
		presentationReconciler.setRepairer(defaultDamagerRepairer, IDocument.DEFAULT_CONTENT_TYPE);
		//
		return presentationReconciler;
	}
	
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return ArduinoUnoRulesPartitionScanner.PARTITIONS;
	}	

}
