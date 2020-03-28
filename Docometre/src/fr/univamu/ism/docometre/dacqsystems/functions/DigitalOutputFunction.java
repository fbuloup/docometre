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
package fr.univamu.ism.docometre.dacqsystems.functions;

import java.util.ArrayList;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fr.univamu.ism.docometre.Activator;
import fr.univamu.ism.docometre.DocometreMessages;
import fr.univamu.ism.docometre.dacqsystems.Channel;
import fr.univamu.ism.docometre.dacqsystems.ChannelProperties;
import fr.univamu.ism.docometre.dacqsystems.DACQConfiguration;
import fr.univamu.ism.docometre.dacqsystems.Module;
import fr.univamu.ism.docometre.dacqsystems.Process;
import fr.univamu.ism.docometre.dacqsystems.adwin.ADWinDACQConfigurationProperties;
import fr.univamu.ism.docometre.dacqsystems.adwin.ADWinDigInOutChannelProperties;
import fr.univamu.ism.docometre.dacqsystems.adwin.ADWinDigInOutModule;
import fr.univamu.ism.docometre.dacqsystems.adwin.ADWinModuleProperties;
import fr.univamu.ism.docometre.dacqsystems.adwin.ADWinProcess;
import fr.univamu.ism.docometre.dacqsystems.arduinouno.ArduinoUnoChannelProperties;
import fr.univamu.ism.docometre.dacqsystems.arduinouno.ArduinoUnoDigInOutChannelProperties;
import fr.univamu.ism.docometre.dacqsystems.arduinouno.ArduinoUnoDigInOutModule;
import fr.univamu.ism.docometre.dacqsystems.arduinouno.ArduinoUnoProcess;
import fr.univamu.ism.docometre.dacqsystems.functions.GenericFunction;
import fr.univamu.ism.docometre.scripteditor.actions.FunctionFactory;
import fr.univamu.ism.process.Block;
import fr.univamu.ism.process.ScriptSegmentType;

public class DigitalOutputFunction extends GenericFunction {
	
	public static final String functionFileName = "DIGITAL_OUTPUT.FUN";
	
	private static final long serialVersionUID = 1L;
	
	private static final String channelNameKey = "channelName";
	private static final String channelValueKey = "channelValue";
	private static final String moduleNumberKey = "moduleNumber";
	private static final String channelNumberKey = "channelNumber";
	
	private String channelName;
	private String channelValue;
	
	@Override
	public String getFunctionFileName() {
		return functionFileName;
	}
	
	@Override
	public Object getGUI(Object titleAreaDialog, Object parent, Object context) {
		if(!(context instanceof Process)) return null;
		Composite container  = (Composite) parent;
		Composite paramContainer = new Composite(container, SWT.NORMAL);
		paramContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramContainer.setLayout(new GridLayout(2, false));
		
		Label inputLabel = new Label(paramContainer, SWT.NORMAL);
		inputLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		inputLabel.setText(DocometreMessages.SelectedDigitalOutputLabel);
		
		ComboViewer channelComboViewer = new ComboViewer(paramContainer);
		channelComboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		channelComboViewer.setContentProvider(new ArrayContentProvider());
		channelComboViewer.setLabelProvider(new LabelProvider());
		channelComboViewer.setComparator(new ViewerComparator());
		
		Label valueLabel = new Label(paramContainer, SWT.NORMAL);
		valueLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		valueLabel.setText(DocometreMessages.ValueLabel); 
		
		Text valueText = new Text(paramContainer, SWT.BORDER);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		ArrayList<Channel> channels = new ArrayList<>();
		Process process = (Process) context;
		DACQConfiguration dacqConfiguration = process.getDACQConfiguration();
		Module[] modules = dacqConfiguration.getModules();
		for (Module module : modules) {
			if(module instanceof ADWinDigInOutModule) {
				Channel[] channelsArray = module.getChannels();
				for (Channel channel : channelsArray) {
					if(channel.getProperty(ADWinDigInOutChannelProperties.IN_OUT).equals(ADWinDigInOutChannelProperties.OUTPUT)) channels.add(channel);
				}
			}
			else if(module instanceof ArduinoUnoDigInOutModule) {
				Channel[] channelsArray = module.getChannels();
				for (Channel channel : channelsArray) {
					if(channel.getProperty(ArduinoUnoChannelProperties.USED).equalsIgnoreCase("true"))
						if(channel.getProperty(ArduinoUnoDigInOutChannelProperties.IN_OUT).equals(ArduinoUnoDigInOutChannelProperties.OUTPUT)) channels.add(channel);
				}
			}
		}
		
		channelComboViewer.setInput(channels);
		channelComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(channelComboViewer.getCombo().getSelectionIndex() > -1) 
					getTransientProperties().put(channelNameKey, channelComboViewer.getCombo().getText());
			}
		});
		
		valueText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getTransientProperties().put(channelValueKey, valueText.getText());
			}
		});
		
		channelName  = getProperty(channelNameKey, "");
		channelValue = getProperty(channelValueKey, "");
		
		channelComboViewer.getCombo().select(channelComboViewer.getCombo().indexOf(channelName));
		valueText.setText(channelValue);
		
		try {
			KeyStroke keyStroke = KeyStroke.getInstance("CTRL+SPACE");
			DocometreContentProposalProvider proposalProvider = new DocometreContentProposalProvider(dacqConfiguration.getProposal(), valueText);
			proposalProvider.setFiltering(true);
			ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(valueText, new TextContentAdapter(), proposalProvider, keyStroke, null);
			proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
			proposalAdapter.addContentProposalListener(proposalProvider);
			
		} catch (ParseException e1) {
			e1.printStackTrace();
			Activator.logErrorMessageWithCause(e1);
		}
		
		
		addCommentField(paramContainer, 1, context);
		
		return paramContainer;
	}
	
	@Override
	public String getCode(Object context, Object step) {
		String code = "";
		Process process = (Process) context;
		DACQConfiguration dacqConfiguration = process.getDACQConfiguration();
		if(step == ScriptSegmentType.INITIALIZE || step == ScriptSegmentType.LOOP || step == ScriptSegmentType.FINALIZE) {
			if(process instanceof ADWinProcess) {
				// Récupérer la bonne propriété dans le fichier functionFileName en fonction du bon device : Gold ou Pro ET du bon CPU : I ou II
				// FUNCTION_CODE_GOLD_I ou FUNCTION_CODE_GOLD_II ou FUNCTION_CODE_PRO_I ou FUNCTION_CODE_PRO_II
				code = code + "\nREM Digital Output Function\n\n";
				String systemType = dacqConfiguration.getProperty(ADWinDACQConfigurationProperties.SYSTEM_TYPE);
				String cpuType = dacqConfiguration.getProperty(ADWinDACQConfigurationProperties.CPU_TYPE);
				String key = FUNCTION_CODE + "_" + systemType + "_" + cpuType;
				String temporaryCode = FunctionFactory.getProperty(process, functionFileName, key.toUpperCase());
				// Retrieve associated analog input module
				channelName  = getProperty(channelNameKey, "");
				channelValue = getProperty(channelValueKey, "");
				Module[] modules = dacqConfiguration.getModules();
				ADWinDigInOutModule foundModule = null;
				Channel foundChannel = null;
				for (Module module : modules) {
					if(module instanceof ADWinDigInOutModule) {
						ADWinDigInOutModule adwinDigInOutModule = (ADWinDigInOutModule) module;
						Channel[] channels = adwinDigInOutModule.getChannels();
						for (Channel channel : channels) {
							 String localChannelName = channel.getProperty(ChannelProperties.NAME);
							 if(channelName.equals(localChannelName)) {
								 foundModule = adwinDigInOutModule;
								 foundChannel = channel;
								 break;
							 }
						}
					}
				}
				if(foundModule != null && foundChannel != null) {
					String channelNumber = foundChannel.getProperty(ChannelProperties.CHANNEL_NUMBER);
					String moduleNumber = foundModule.getProperty(ADWinModuleProperties.MODULE_NUMBER);
					temporaryCode = temporaryCode.replaceAll(channelNameKey, channelName).replaceAll(channelNumberKey, channelNumber).replaceAll(moduleNumberKey, moduleNumber);
					temporaryCode = temporaryCode.replaceAll(channelValueKey, channelValue);
					code = code + temporaryCode + "\n\n";
				}
			}

			if(process instanceof ArduinoUnoProcess) {
				String indent = (step == ScriptSegmentType.FINALIZE) ? UNO_FINALIZE_INDENT : UNO_DEFAULT_INDENT;
				String temporaryCode = FunctionFactory.getProperty(process, functionFileName, FUNCTION_CODE);
				// Retrieve associated analog input module
				channelName  = getProperty(channelNameKey, "");
				channelValue = getProperty(channelValueKey, "");
				Module[] modules = dacqConfiguration.getModules();
				Channel foundChannel = null;
				for (Module module : modules) {
					if(module instanceof ArduinoUnoDigInOutModule) {
						ArduinoUnoDigInOutModule arduinoDigInOutModule = (ArduinoUnoDigInOutModule) module;
						Channel[] channels = arduinoDigInOutModule.getChannels();
						for (Channel channel : channels) {
							 String localChannelName = channel.getProperty(ChannelProperties.NAME);
							 if(channelName.equals(localChannelName)) {
								 foundChannel = channel;
								 break;
							 }
						}
					}
				}
				if(foundChannel != null) {
					String channelNumber = foundChannel.getProperty(ChannelProperties.CHANNEL_NUMBER);
					temporaryCode = indent + temporaryCode.replaceAll(channelNameKey, channelName).replaceAll(channelNumberKey, channelNumber);
					temporaryCode = temporaryCode.replaceAll(channelValueKey, channelValue);
					temporaryCode = temporaryCode.replaceAll("\n", "\n" + indent);
					code = code + temporaryCode + "\n\n";
				}
			}
		}
		return code;
	}
	
	@Override
	public Block clone() {
		DigitalOutputFunction function = new DigitalOutputFunction();
		super.clone(function);
		return function;
	}

}
