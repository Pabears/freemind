/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2004  Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 *
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
 *
 * Created on 05.05.2004
 */
/* $Id: NewChildAction.java,v 1.1.2.2.2.1.6.1 2007-04-09 11:43:33 dpolivaev Exp $ */

package freemind.modes.mindmapmode.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import freemind.controller.actions.generated.instance.DeleteNodeAction;
import freemind.controller.actions.generated.instance.NewNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.extensions.PermanentNodeHook;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.MindMapLinkRegistry.ID_Registered;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.modes.mindmapmode.actions.xml.ActorXml;
import freemind.view.mindmapview.NodeView;


public class NewChildAction extends AbstractAction implements ActorXml {
    private final MindMapController c;
    private static Logger logger=null;
    public NewChildAction(MindMapController modeController) {
        super(modeController.getText("new_child"), new ImageIcon(modeController.getResource("images/idea.png")));
        this.c = modeController;
		this.c.getActionFactory().registerActor(this, getDoActionClass());
		if(logger == null) {
		    logger = c.getFrame().getLogger(NewChildAction.class.getName());
		}

    }

    public void actionPerformed(ActionEvent e) {
       this.c.addNew(c.getSelected(), MindMapController.NEW_CHILD, null);
    }
    /* (non-Javadoc)
     * @see freemind.controller.actions.ActorXml#act(freemind.controller.actions.generated.instance.XmlAction)
     */
    public void act(XmlAction action) {
		NewNodeAction addNodeAction = (NewNodeAction) action;
		NodeAdapter parent = this.c.getNodeFromID(addNodeAction.getNode());
		int index = addNodeAction.getIndex();
		MindMapNode newNode = c.newNode("", parent.getMap());
        newNode.setLeft(addNodeAction.getPosition().equals("left"));
		String newId = addNodeAction.getNewId();
		ID_Registered reg = c.getModel().getLinkRegistry().registerLinkTarget(newNode,newId);
		if(!reg.getID().equals(newId)) {
			throw new IllegalArgumentException("Designated id '"+newId+"' was not given to the node. It received '"+reg.getID()+"'.");
		}
		c.insertNodeInto(newNode, parent, index);
		// call hooks:
		for (Iterator i = parent.getActivatedHooks().iterator(); i.hasNext();) {
            PermanentNodeHook hook = (PermanentNodeHook) i.next();
            hook.onNewChild(newNode);
        }
		// done.
    }
    /* (non-Javadoc)
     * @see freemind.controller.actions.ActorXml#getDoActionClass()
     */
    public Class getDoActionClass() {
        return NewNodeAction.class;
    }


	public MindMapNode addNew(final MindMapNode target, final int newNodeMode, final KeyEvent e) {
	   final MindMapNode targetNode = target;
	   MindMapNode newNode = null;

	   boolean targetIsLeft = true;
    switch (newNodeMode) {
		 case MindMapController.NEW_SIBLING_BEFORE:
		 case MindMapController.NEW_SIBLING_BEHIND:
            {
		     if (targetNode.isRoot()) {
		         c.getController().errorMessage(c.getText("new_node_as_sibling_not_possible_for_the_root"));
		         c.setBlocked(false);
		         return null;
		     }
		     MindMapNode parent = targetNode.getParentNode();
		     int childPosition = parent.getChildPosition(targetNode);
		     if (newNodeMode == MindMapController.NEW_SIBLING_BEHIND) {
		         childPosition++;
		     }
		     newNode = addNewNode(parent, childPosition, parent.isLeft());
		     final NodeView nodeView = c.getNodeView(newNode);
		     c.select(nodeView);
		     c.edit.editLater(nodeView, c.getNodeView(target), e, true, false, false);
		     break;
            }

		 case MindMapController.NEW_CHILD:
		 case MindMapController.NEW_CHILD_WITHOUT_FOCUS:
         {
		   final boolean parentFolded = targetNode.isFolded();
		   if (parentFolded) {
			c.setFolded(targetNode,false);
		   }
		   int position = c.getFrame().getProperty("placenewbranches").equals("last") ?
			  targetNode.getChildCount() : 0;
			newNode = addNewNode(targetNode, position);
             final NodeView nodeView = c.getNodeView(newNode);
			   if (newNodeMode == MindMapController.NEW_CHILD) {
				c.select(nodeView);
			   }
		c.edit.editLater(nodeView, c.getNodeView(target), e, true, parentFolded, false);
		   break;
         }
	   }
    	return newNode;
	}

    public MindMapNode addNewNode(MindMapNode parent, int index){
        return addNewNode(parent, index, parent.isNewChildLeft());
    }
    
	public MindMapNode addNewNode(MindMapNode parent, int index, boolean newNodeIsLeft){
		// bug fix from Dimitri.
        c.getModel().getLinkRegistry().registerLinkTarget(parent);
        String newId = c.getModel().getLinkRegistry().generateUniqueID("_");
        c.getActionFactory().startTransaction(c.getText("new_child"));
        NewNodeAction newNodeAction =
            getAddNodeAction(parent, index, newId, newNodeIsLeft);
        // Undo-action
        DeleteNodeAction deleteAction = c.deleteChild.getDeleteNodeAction(newId);
        c.getActionFactory().executeAction(new ActionPair(newNodeAction, deleteAction));
        c.getActionFactory().endTransaction(c.getText("new_child"));
        return (MindMapNode) parent.getChildAt(index);
	}


    public NewNodeAction getAddNodeAction(
        MindMapNode parent,
        int index,
        String newId,
        boolean newNodeIsLeft)
         {
        String pos = newNodeIsLeft ? "left" : "right";
        NewNodeAction newNodeAction = new NewNodeAction();
        newNodeAction.setNode(c.getNodeID(parent));
        newNodeAction.setPosition(pos);
        newNodeAction.setIndex(index);
        newNodeAction.setNewId(newId);
        return newNodeAction;
    }


}