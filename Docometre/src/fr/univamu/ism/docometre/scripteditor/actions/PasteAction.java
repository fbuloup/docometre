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
package fr.univamu.ism.docometre.scripteditor.actions;

import java.util.ArrayList;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import fr.univamu.ism.docometre.DocometreMessages;
import fr.univamu.ism.docometre.scripteditor.commands.PasteCommand;
import fr.univamu.ism.process.Block;
import fr.univamu.ism.process.ScriptSegment;

public class PasteAction extends SelectionAction {

	public static ScriptSegment scriptSegment;
	private CommandStack commandStack;

	public PasteAction(IWorkbenchPart part, CommandStack commandStack) {
		super(part);
		setLazyEnablementCalculation(true);
		this.commandStack = commandStack;
	}

	@Override
	protected void init() {
		super.init();
		setText(DocometreMessages.PasteAction_Text);
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setId(ActionFactory.PASTE.getId());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean calculateEnabled() {
		ArrayList<Object> objects = (ArrayList<Object>) Clipboard.getDefault().getContents();
		if(objects == null || objects.isEmpty()) return false;
		for (Object object : objects) {
			if(!(object instanceof Block)) return false;
		}
		return true;
	}

	@Override
	public void run() {
//		getWorkbenchPart().getAdapter(GraphicalViewer.class)
//		System.out.print("Running paste command for segment " + scriptSegment.getScriptSegmentType());
//		System.out.println(" from paste action " + this);
		PasteCommand pasteCommand = new PasteCommand(scriptSegment);
		commandStack.execute(pasteCommand);
	}
	
//	public void setScriptSegment(ScriptSegment scriptSegment) {
//		System.out.print("Setting script segment to " + scriptSegment.getScriptSegmentType());
//		System.out.println(" for paste action " + this);
//		PasteAction.scriptSegment = scriptSegment;
//	}
	
}
