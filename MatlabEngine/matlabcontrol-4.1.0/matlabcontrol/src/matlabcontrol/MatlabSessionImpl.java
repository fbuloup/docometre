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
package matlabcontrol;

/*
 * Copyright (c) 2013, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.rmi.registry.Registry;
import java.util.UUID;

/**
 * Implementation of {@link MatlabSession}. Split into interface and implementation to work properly with RMI.
 * 
 * @since 4.0.0
 * 
 * @author <a href="mailto:nonother@gmail.com">Joshua Kaplan</a>
 */
class MatlabSessionImpl implements MatlabSession
{   
    /**
     * The prefix for all RMI names of bound instances of {@link MatlabSession}.
     */
    private static final String MATLAB_SESSION_PREFIX = "MATLAB_SESSION_";
    
    /**
     * The bound name in the RMI registry for this instance.
     */
    private final String SESSION_ID = MATLAB_SESSION_PREFIX + UUID.randomUUID().toString();

    @Override
    public synchronized boolean connectFromRMI(String receiverID, int port)
    {
        boolean success = false;
        if(MatlabConnector.isAvailableForConnection())
        {
            MatlabConnector.connect(receiverID, port, true);
            success = true;
        }
        
        return success;
    }
    
    /**
     * The unique identifier for this session.
     * 
     * @return 
     */
    String getSessionID()
    {
        return SESSION_ID;
    }
    
    /**
     * Attempts to connect to a running instance of MATLAB. Returns {@code true} if a connection was made,
     * {@code false} otherwise.
     * 
     * @param receiverID
     * @param port
     * @return if connection was made
     */
    static boolean connectToRunningSession(String receiverID, int port)
    {
        boolean establishedConnection = false;
        
        try
        {
            Registry registry = LocalHostRMIHelper.getRegistry(port);
            
            String[] remoteNames = registry.list();
            for(String name : remoteNames)
            {
                if(name.startsWith(MATLAB_SESSION_PREFIX))
                {
                    MatlabSession session = (MatlabSession) registry.lookup(name);
                    if(session.connectFromRMI(receiverID, port))
                    {
                        establishedConnection = true;
                        break;
                    }
                }
            }
        }
        catch(Exception e) { }
        
        return establishedConnection;
    }
}
