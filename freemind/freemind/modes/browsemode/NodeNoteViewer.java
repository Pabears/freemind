/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2006  Christian Foltin and others
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/* $Id: NodeNoteViewer.java,v 1.1.2.1.2.2.4.1 2006-08-11 20:33:22 dpolivaev Exp $ */
package freemind.modes.browsemode;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import freemind.modes.MindMapNode;
import freemind.modes.common.plugins.NodeNoteBase;

/**
 * @author foltin
 *
 */
public class NodeNoteViewer extends NodeNoteBase {
    static private JComponent noteScrollPane;
    static private JLabel noteViewer;
	protected void nodeRefresh(MindMapNode node) {
		getController().nodeChanged(node);
	}

	protected void receiveFocusAddons() {
	}

	protected void looseFocusAddons() {
	}

    protected Container getNoteViewerComponent() throws Exception {
        if(noteViewer == null){
            noteViewer = new JLabel();
            noteViewer.setBackground(Color.WHITE);
            noteViewer.setVerticalAlignment(JLabel.TOP);
            noteViewer.setOpaque(true);
            noteScrollPane = new JScrollPane(noteViewer);
            noteScrollPane.setPreferredSize(new Dimension(1, 200));
        }
        noteViewer.setText(getMyNodeText());
            
        return noteScrollPane;
    }
}
