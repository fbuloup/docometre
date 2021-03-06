package fr.univamu.ism.docometre.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fr.univamu.ism.docometre.Activator;
import fr.univamu.ism.docometre.DocometreMessages;

public class MatlabPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(DocometreMessages.MatlabPreferences_Description);
	}

	@Override
	protected void createFieldEditors() {
		// Matlab preferences
		BooleanFieldEditor showMatlabWindowFieldEditor = new BooleanFieldEditor(GeneralPreferenceConstants.SHOW_MATLAB_WINDOW, DocometreMessages.MatlabEngineShowMatlabWindow, getFieldEditorParent());
		addField(showMatlabWindowFieldEditor);
		
		IntegerFieldEditor matlabTimeOutFieldEditor = new IntegerFieldEditor(GeneralPreferenceConstants.MATLAB_TIME_OUT, DocometreMessages.MatlabEngineTimeOut, getFieldEditorParent());
		addField(matlabTimeOutFieldEditor);
		
		FileFieldEditor matlabLocationFieldEditor = new FolderPathFieldEditor(GeneralPreferenceConstants.MATLAB_LOCATION, DocometreMessages.MatlabEngineLocation, getFieldEditorParent());
		addField(matlabLocationFieldEditor);
		
		DirectoryFieldEditor matlabScriptsLocationFieldEditor = new DirectoryFieldEditor(GeneralPreferenceConstants.MATLAB_SCRIPT_LOCATION, DocometreMessages.MatlabEngineScriptLocation, getFieldEditorParent());
		addField(matlabScriptsLocationFieldEditor);
	}

}
