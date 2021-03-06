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
package fr.univamu.ism.docometre.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fr.univamu.ism.docometre.Activator;
import fr.univamu.ism.docometre.ObjectsController;
import fr.univamu.ism.docometre.ResourceProperties;
import fr.univamu.ism.docometre.ResourceType;
import fr.univamu.ism.docometre.editors.PartNameRefresher;
import fr.univamu.ism.docometre.editors.ResourceEditorInput;
import fr.univamu.ism.docometre.views.ExperimentsView;

public class RenameResourceOperation extends AbstractOperation {

	private IResource resource;
	private String oldName;
	private String newName;
	private boolean refreshUI;

	public RenameResourceOperation(String label, IResource resource, String newName, boolean refreshUI) {
		super(label);
		addContext(ExperimentsView.experimentsViewUndoContext);
		this.resource = resource;
		oldName = resource.getName();
		this.newName = newName;
		this.refreshUI = refreshUI;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return performOperation(newName);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return performOperation(oldName);
	}
	
	private IStatus performOperation(String name) {
		try {
			String fileExtension = "";
			// Detect if it is a dacq default renaming
			boolean setDACQAsDefault = false;
			if(ResourceType.isDACQConfiguration(resource)) {
				String fullPath = ResourceProperties.getDefaultDACQPersistentProperty(resource);
				if(fullPath != null) if(fullPath.equals(resource.getFullPath().toOSString())) setDACQAsDefault = true;
			}
			// Compute file extension
			if(resource.getFileExtension() != null) fileExtension = "." + resource.getFileExtension();
			// Rename resource and get new resource
			resource.move(resource.getParent().getFullPath().append(name + fileExtension), true, null);
			IResource newResource = resource.getParent().findMember(name + fileExtension);
			// Update default DACQ if necessary
			if(setDACQAsDefault) ResourceProperties.setDefaultDACQPersistentProperty(newResource, newResource.getFullPath().toOSString());
			// Refresh experiments view for this resource
			if(refreshUI) ExperimentsView.refresh(newResource.getParent(), new IResource[]{newResource});
			// Update all process resources affected by this path renaming and update experiments view
			updateProcesses(resource, newResource);
			// Update all trials and process test resources affected by this path renaming and update experiments view
			if(ResourceType.isProcess(newResource)) {
				updateTrials(resource, newResource);
				updateProcessTest(resource, newResource);
			}
			// Get editors (dacq configuration and process) to update part names when necessary
			 updateEditorsPartName(resource, newResource);
			resource = newResource;
			return Status.OK_STATUS;
		} catch (CoreException e) {
			e.printStackTrace();
			Activator.logErrorMessageWithCause(e);
		}
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error renaming resource");
	}
	
	/*
	 * This method update all editors part names when renamed resource affects their path, including DACQ associated file
	 */
	private void updateEditorsPartName(IResource resource, IResource newResource) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < editorReferences.length; i++) {
			try {
				ResourceEditorInput resourceEditorInput = ((ResourceEditorInput)editorReferences[i].getEditorInput());
				Object object = resourceEditorInput.getObject();
				IResource editedResource = ObjectsController.getResourceForObject(object);
				if(editedResource == null && object instanceof IResource) {
					if(ResourceType.isLog(newResource) || ResourceType.isParameters(newResource) || ResourceType.isSamples(newResource)) {
						if(resource.equals(object)) {
							editedResource = newResource;
							resourceEditorInput.setObject(newResource);
						}
					}
					
				}
				boolean refreshPartName = false;
				// Mark this part to be updated if its resource has been renamed
				if(editedResource != null) {
					if(editedResource.equals(newResource)) refreshPartName = true;
					if(ResourceType.isProcess(editedResource)) {
						// If editor resource is a process, check if associated DACQ file has been renamed
						String fullPathAssociatedDAQ = ResourceProperties.getAssociatedDACQConfigurationProperty((IFile) editedResource);
						String fullNewPath = newResource.getFullPath().toOSString();
						// If yes mark this editor to be refreshed
						if(fullPathAssociatedDAQ.startsWith(fullNewPath)) refreshPartName = true;
					}
				}
				if(refreshPartName) ((PartNameRefresher)editorReferences[i].getEditor(false)).refreshPartName();
			} catch (PartInitException e) {
				e.printStackTrace();
				Activator.logErrorMessageWithCause(e);
			}
		}
	}
	
	/*
	 * This method update associated processes dacq when renamed resource affects dacq path 
	 * It also refreshes experiment view to reflect these changes
	 */
	private void updateProcesses(IResource oldResource, IResource newResource) {
		String fullOldPath = oldResource.getFullPath().toOSString();
		String fullNewPath = newResource.getFullPath().toOSString();
		IResource[] processes = ResourceProperties.getAllTypedResources(ResourceType.PROCESS, newResource.getProject());
		for (IResource process : processes) {
			String fullPathAssociatedDAQ = ResourceProperties.getAssociatedDACQConfigurationProperty((IFile) process);
			if((fullPathAssociatedDAQ != null) && fullPathAssociatedDAQ.startsWith(fullOldPath)) {
				String newFullPathAssociatedDAQ = fullPathAssociatedDAQ.replaceAll(fullOldPath, fullNewPath);
				ResourceProperties.setAssociatedDACQConfigurationProperty((IFile) process, newFullPathAssociatedDAQ);
				ExperimentsView.refresh(process.getParent(), new IResource[]{newResource});
			}
		}
	}
	
	/*
	 * This method update associated processes when renamed resource affects trials  
	 * It also refreshes experiment view to reflect these changes
	 */
	private void updateTrials(IResource oldResource, IResource newResource) {
		String fullOldPath = oldResource.getFullPath().toOSString();
		String fullNewPath = newResource.getFullPath().toOSString();
		IResource[] trials = ResourceProperties.getAllTypedResources(ResourceType.TRIAL, newResource.getProject());
		for (IResource trial : trials) {
			String fullPathAssociatedProcess = ResourceProperties.getAssociatedProcessProperty(((IFolder) trial));
			if((fullPathAssociatedProcess != null) && fullPathAssociatedProcess.equals(fullOldPath)) {
				ResourceProperties.setAssociatedProcessProperty((IFolder) trial, fullNewPath);
				ExperimentsView.refresh(trial.getParent(), new IResource[]{newResource});
			}
		}
	}
	
	/*
	 * This method update associated process test folder when renamed resource affects  
	 * It also refreshes experiment view to reflect these changes
	 */
	private void updateProcessTest(IResource oldResource, IResource newResource) {
		String fullOldPath = oldResource.getFullPath().toOSString();
		String fullNewPath = newResource.getFullPath().toOSString();
		IResource[] processTests = ResourceProperties.getAllTypedResources(ResourceType.PROCESS_TEST, newResource.getProject());
		for (IResource processTest : processTests) {
			String fullPathAssociatedProcess = ResourceProperties.getAssociatedProcessProperty(processTest);
			if((fullPathAssociatedProcess != null) && fullPathAssociatedProcess.equals(fullOldPath)) {
				ResourceProperties.setAssociatedProcessProperty(processTest, fullNewPath);
				ExperimentsView.refresh(processTest.getParent(), new IResource[]{newResource});
			}
		}
	}
	
}
