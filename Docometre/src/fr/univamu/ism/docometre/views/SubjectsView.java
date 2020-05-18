package fr.univamu.ism.docometre.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SubjectsView extends ViewPart {
	
	public static String ID = "Docometre.SubjectsView";
	private TreeViewer subjectsTreeViewer;

	public SubjectsView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		subjectsTreeViewer = new TreeViewer(parent);

	}

	@Override
	public void setFocus() {
		subjectsTreeViewer.getTree().setFocus();
	}

}
