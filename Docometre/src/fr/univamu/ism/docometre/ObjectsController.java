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
package fr.univamu.ism.docometre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import fr.univamu.ism.docometre.dacqsystems.DACQConfiguration;

public class ObjectsController {
	
	private static boolean debug = true;
	
	/*
	 * Hash map containing deserialized objects and nb handles per object
	 */
	private static HashMap<Object, Integer> objectHandles = new HashMap<Object, Integer>();
	
	private static void debug(Object object, int nbHandles) {
		if(debug) {
			IResource resource = getResourceForObject(object);
			
			String objectRef = object.getClass().getSimpleName() + "@" + Integer.toHexString(object.hashCode());
			
			if(resource == null) Activator.logInfoMessage("Nb Handle : " + nbHandles + " and no more associated resource for object : " + objectRef, ObjectsController.class); 
			else Activator.logInfoMessage("Nb handle for " + resource.getFullPath().toOSString() + "(" + objectRef + ")" + " : " + nbHandles, ObjectsController.class);
		}
	}
	
	public static Object getDACQConfiguration(Object process) {
		IResource processResource = ObjectsController.getResourceForObject(process);
		String fullPath = ResourceProperties.getAssociatedDACQConfigurationProperty((IFile) processResource);
		IFile daqConfigurationFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath)) ;
		Object dacqConfiguration = null;
		if(daqConfigurationFile.exists()) {
			dacqConfiguration = (DACQConfiguration) ResourceProperties.getObjectSessionProperty(daqConfigurationFile);
			if(dacqConfiguration == null) {
				dacqConfiguration = (DACQConfiguration) ObjectsController.deserialize(daqConfigurationFile);
				ResourceProperties.setObjectSessionProperty(daqConfigurationFile, dacqConfiguration);
			}
		}
//		if(dacqConfiguration != null) ObjectsController.addHandle(dacqConfiguration);
		return dacqConfiguration;
	}
	
	/*
	 * Add an handle to this object
	 */
	public static void addHandle(Object object) {
		Integer nbHandles = objectHandles.get(object);
		if(nbHandles != null) nbHandles++;
		else nbHandles = 1;
		objectHandles.put(object, nbHandles);
		if(debug) debug(object, nbHandles);
	}
	
	/*
	 * Remove an handle from this object
	 */
	public static void removeHandle(Object object) {
		Integer nbHandles = objectHandles.get(object);
		if(nbHandles != null)  {
			nbHandles--;
			if(nbHandles > 0) objectHandles.put(object, nbHandles);
			else {
				objectHandles.remove(object);
				IResource resource = getResourceForObject(object);
				if(resource != null) ResourceProperties.setObjectSessionProperty(resource, null);
			}
			if(debug) debug(object, nbHandles);
		}
	}
	
	/*
	 * Serialize object
	 */
	public static void serialize(Object object) {
		if(object == null) return;
		IFile file = (IFile)getResourceForObject(object);
		serialize(file, object);
	}
	
	public static void serialize(IFile file, Object object) {
		if(object == null) {
			Activator.logErrorMessage("ERROR : you are trying to serialize a null object !");
			return;
		}
		String filePath = file.getLocation().toOSString();
		FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        Exception exception = null;
		try {
            fos = new FileOutputStream(filePath);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            file.refreshLocal(IResource.DEPTH_ZERO, null);
        } catch (IOException | CoreException e) {
            exception = e;
        } finally {
            try {
            	oos.flush();
				oos.close();
	            fos.close();
			} catch (Exception e) {
	           if(exception == null) exception = e;
			} finally {
				if(exception != null) {
					exception.printStackTrace();
					Activator.logErrorMessageWithCause(exception);
					Activator.logErrorMessage(DocometreMessages.ImpossibleToSaveModel + filePath);
				}
			}
        }
	}
	
	/*
	 * Deserialize object
	 */
	public static Object deserialize(IFile file) {
		Object object = null;
		File dataFile = new File(file.getLocation().toOSString());
		if(!dataFile.exists()) return null;
		FileInputStream is = null;
		ObjectInputStream ois = null;
		Exception exception = null;
		try {
			is = new FileInputStream(dataFile);
			ois = new ObjectInputStream(is);
			object = ois.readObject();
		} catch (Exception e) {
			exception = e;
		} finally {
			try {
				ois.close();
				is.close();
			} catch (Exception e) {
				if(exception == null) exception = e;
			} finally {
				if(exception != null) {
					exception.printStackTrace();
					Activator.logErrorMessageWithCause(exception);
					Activator.logErrorMessage(DocometreMessages.ImpossibleToLoadModel + dataFile.getAbsolutePath()); //$NON-NLS-1$
				}
			}
		}
		return object;
	}
	
	/*
	 * Get resource for specific object
	 */
	public static IResource getResourceForObject(Object object) {
		try {
			if(object != null) return getResourceForObject(object, ResourcesPlugin.getWorkspace().getRoot());
			//else Activator.logWarningMessage("WARNING : searching a 'null' object (getResourceForObject) !");
		} catch (CoreException e) {
			e.printStackTrace();
			Activator.logErrorMessageWithCause(e);
		}
		return null;
	}
	
	/*
	 * Get resource for specific object in specific resource container
	 */
	private static IResource getResourceForObject(Object searchedObject, IResource resource) throws CoreException {
		IResource foundResource = null;
		Object object = ResourceProperties.getObjectSessionProperty(resource);
		if(searchedObject.equals(object)) foundResource = resource;
		if(foundResource == null && object == null && resource instanceof IContainer) {
			IResource[] members = ((IContainer)resource).members();
			for (IResource member : members) {
				foundResource = getResourceForObject(searchedObject, member);
				if(foundResource != null) break;
			}
		}
		return foundResource;
	}
}
