/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2001  Joerg Mueller <joergmueller@bigfoot.com>
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
/*$Id: MindMapLayout.java,v 1.15.14.2.2.1 2005-01-07 15:25:19 dpolivaev Exp $*/

package freemind.view.mindmapview;

import freemind.main.FreeMindMain;
import freemind.modes.MindMapCloud;

import java.awt.IllegalComponentStateException;
import java.awt.LayoutManager;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.swing.JLabel;

/**
 * This class will Layout the Nodes and Edges of an MapView.
 */
public class MindMapLayout implements LayoutManager {

    private final static int BORDER = 30;//width of the border around the map.
    public final static int HGAP = 20;//width of the horizontal gap that contains the edges
    public final static int VGAP = 3;//height of the vertical gap between nodes
    final static int SHIFT = - 2;//height of the vertical shift between node and its closest child
	// minimal width for input field of leaf or folded node (PN)
	
	// the MINIMAL_LEAF_WIDTH is reserved by calculation of the map width
	public final static int MINIMAL_LEAF_WIDTH = 150;
	public final static int MINIMAL_WIDTH = 50;


    private MapView map;
    private int ySize;
    
    // Call xSize and ySize in the same manner
    private int xSize;
    
    // save root node coordinates
	private int rootX = 0;
	private int rootY = 0;

    public MindMapLayout(MapView map) {
        this.map = map;
    // properties "mapxsize" and "mapysize" are ignored
    }

    public void addLayoutComponent(String name, Component comp){ }

    public void removeLayoutComponent(Component comp) {  }

    public void layoutContainer(Container parent) {
       layout(true); }
   




    //
    // Absolute positioning
    //

    /**
	* placeNode throws this exception 
	* if the map size changes require new layout calculations 
     */
	private class NewLayoutIterationNeeded extends Exception {
	}

    /**
     * This funcion resizes the map and do the layout.
     * All tree heights, widths and shifts should be already calculated.
     */
	public void layout(boolean holdSelected) {
		NodeView selected = map.getSelected();
		 holdSelected =  holdSelected &&  
		    (selected != null && selected.getX() != 0 && selected.getY() != 0);
		int oldRootX = getRoot().getX();
		int oldRootY = holdSelected ? selected.getY() : getRoot().getY();
		resizeMap(getRoot().getTreeWidth(), getRoot().getTreeHeight());
        layout(map.getRoot());
		try{
			int rootX = getRoot().getX();
			int rootY = holdSelected ? selected.getY() : getRoot().getY();
			getMapView().scrollBy(rootX - oldRootX, rootY - oldRootY, true );
		}
		catch(IllegalComponentStateException e){
		}

	}

    /**
     * This places the node's subtree if the relative
     * position of every node to its parent is known.
     */
    private void layout(NodeView node) {
        // The overall task of layout is: give me the sizes and I'll set the positions.

        // Relative Y positions of nodes are already calculated.  Here we
        // only calculate relative x positions of nodes.  Whenever we talk
        // about relative coordinates / positions, we always mean relative to
        // the coordinates of node's parent.

        int x = 0;
        int hgap = node.getHGap();
        if ( node.isRoot() ) {
           //System.err.println("layoutroot"+node.getModel()); 
           x = 0; }
        else if ( node.isLeft() ) {
           x = - hgap - node.getPreferredSize().width; }
        else {
           x = node.getParentView().getPreferredSize().width + hgap; }
        
        placeNode(node, x, node.relYPos);

        //Iterations
        for ( ListIterator e = node.getChildrenViews().listIterator(); e.hasNext(); ) {
           layout( (NodeView)e.next() ); }
    }

    /**
     * Set the position and the size of the node.
     * Set the position of the edge of the node (the edge of the node is the edge
     * connecting the node to its parent).
     *
     * Preconditions: Absolute position of the parent is already set correctly.
     *                Relative positions and TreeHeights are up to dare.
     */
    private void placeNode(NodeView node, int relativeX, int relativeY) {
        // The placeNode is currently called only by layout().

        // I don't undertand why I reset the size of the node according to its preferred size.
        // This should not be the task of this method.

        // relativeX, relativeY - already calculated coordinates of node relative to its parent.;
        if (node.isRoot()) {
            node.setExtendedBounds(getRootX(),
                           getRootY()); 
            }
        else {
            //place the node-label
            int x = node.getParentView().getExtendedX() + relativeX;
            int y = node.getParentView().getExtendedY() + relativeY;
            node.setExtendedBounds(x, y);
            
            // It seems that there is a piece of coding ready for having labelled edges.
            // Having labelled edges is a nice thing, as sure as hell, but we do not
            // have the support yet.

            //place the edge-label
            JLabel label = node.getEdge().getLabel();
            Point start = node.getParentView().getOutPoint();
            Point end = node.getInPoint();
        
            if (node.getParentView().isRoot()) {
               if (node.isLeft()) {
                  start = node.getParentView().getInPoint(); }}

            node.getEdge().start = start;
            node.getEdge().end = end;

            int relX = (start.x - end.x) / 2; 
            int absX = start.x - relX;
            
            int relY = (start.y - end.y) / 2;
            int absY = start.y - relY;
                    
            label.setBounds(absX - label.getPreferredSize().width  / 2,
                            absY - label.getPreferredSize().height / 2,
                            label.getPreferredSize().width, label.getPreferredSize().height);
        }
    }

    /**
	 * @return
	 */
	private int getRootY() {
		return calcYBorderSize() / 2 - getRoot().getTreeShift();
	}

	/**
	 * @return
	 */
	private int getRootX() {
		return calcXBorderSize() / 2 + getRoot().getLeftTreeWidth();
	}

	/**
     *
     */
        private void resizeMap(int width, int height) {
        // (public to reuse from MapView.scrollNodeToVisible  ...)

        // In principle, resize can be caused by:
        // 1) Unfold
        // 2) Insertion of a node
        // 3) Modification of a node in an enlarging way
            boolean bResized = false;
        	int oldXSize = getXSize();
        	int oldYSize = getYSize();
        
			int minXSize = width + calcXBorderSize();
            int minYSize = height  +  calcYBorderSize();

         	if (minXSize != getXSize()) {
        		setXSize(minXSize);
        		bResized = true;
        	}

        	if (minYSize != getYSize()) {
        		setYSize(minYSize);
        		bResized = true;
        	}

        	if (bResized){
        		getMapView().setSize(getXSize(), getYSize());
        	}
    }

    private int calcYBorderSize() {
        int yBorderSize;
        	{
        	Dimension visibleSize = map.getViewportSize();
        	if (visibleSize != null){
        		yBorderSize = Math.max(visibleSize.height, 2 *  BORDER);
        	}
        	else{
        		yBorderSize = 2 *  BORDER;
        		
        	}
        }
        return yBorderSize;
    }

    private int calcXBorderSize() {
        int xBorderSize;
        {
        	Dimension visibleSize = map.getViewportSize();
        	if (visibleSize != null){
        		xBorderSize = Math.max(visibleSize.width, 2 *(BORDER + MINIMAL_LEAF_WIDTH));
        	}
        	else{
        		xBorderSize = 2 *(BORDER + MINIMAL_LEAF_WIDTH);
        		
        	}
        }
        return xBorderSize;
    }


    void updateTreeHeightsAndRelativeYOfDescendantsAndAncestors(NodeView node) {
       updateTreeHeightsAndRelativeYOfDescendants(node);       
       updateTreeHeightsAndRelativeYOfAncestors(node); }

    /**
     * This is called by treeNodesChanged(), treeNodesRemoved() & treeNodesInserted(), so it's the
     * standard mechanism to update the graphical node structure. It updates the parent of the 
     * significant node, and follows recursivly the hierary upwards to root.
     */

    void updateTreeHeightsAndRelativeYOfAncestors(NodeView node) {
		updateTreeGeometry(node);
       if ( !node.isRoot()){
          updateTreeHeightsAndRelativeYOfAncestors(node.getParentView()); }
    }

    //
    // Relative positioning
    //

    // Definiton: relative vertical is either relative Y coord or Treeheight.

    void updateTreeHeightsAndRelativeYOfWholeMap() {
        updateTreeHeightsAndRelativeYOfDescendants(getRoot()); 
		layout(false);
        }

   
    void updateTreeHeightsAndRelativeYOfDescendants(NodeView node) {
	        for (ListIterator e = node.getChildrenViews().listIterator(); e.hasNext();) {
	           updateTreeHeightsAndRelativeYOfDescendants((NodeView)e.next()); }
        updateTreeGeometry(node);
   	}

      /**
	 * @param node
	 * @param l
	 * @param sumOfChildHeights
	 */
	private void updateRelativeYOfChildren(NodeView node, LinkedList childrenViews) {
		int sumOfChildHeights = sumOfChildHeights(node, childrenViews);		
		int pointer = 0;
		ListIterator e = childrenViews.listIterator();
		if (e.hasNext())
		{
	        NodeView child = (NodeView)childrenViews.getFirst();
			pointer  = (node.getPreferredSize().height - sumOfChildHeights) / 2
			+ getShift() + child.getTreeShift();
		}
		
		while (e.hasNext()) {
			NodeView child = (NodeView)e.next();
			int vgap = node.getVGap();
			int iAdditionalCloudHeigth = child.getAdditionalCloudHeigth();
			child.relYPos = pointer + iAdditionalCloudHeigth / 2  - child.getTreeShift(); 
System.out.println(child.getText() + ": relYPos=" + child.relYPos);
			pointer = pointer + child.getTreeHeight() + iAdditionalCloudHeigth + vgap;
		}
	}

		private int sumOfChildHeights( NodeView parent, LinkedList v ) {
    	int parentHeight = parent.getPreferredSize().height;
       if ( v == null || v.size() == 0 ) {
          return parentHeight; }
       int height = 0;
       int vgap = 0;
       for (ListIterator e = v.listIterator(); e.hasNext(); ) {
           NodeView node = (NodeView)e.next();
           vgap = parent.getVGap();
           if (node != null) {
              height += node.getPreferredSize().height + vgap + node.getAdditionalCloudHeigth(); 
           }
       }
       return Math.max(parentHeight, height - vgap); 
    }

    protected void updateTreeGeometry(NodeView node) {
    	if (node.isRoot()){
    		LinkedList leftNodeViews = getRoot().getLeft();
			LinkedList rightNodeViews = getRoot().getRight();

			int leftWidth = calcTreeWidth(node, leftNodeViews);
			int rightWidth = calcTreeWidth(node, rightNodeViews);
			getRoot().setRootTreeWidths(leftWidth, rightWidth);

			updateRelativeYOfChildren(node, leftNodeViews);
			updateRelativeYOfChildren(node, rightNodeViews);
			
			int leftTreeShift = calcTreeShift(node, leftNodeViews);
			int rightTreeShift = calcTreeShift(node, rightNodeViews);
			
			getRoot().setRootTreeShifts(leftTreeShift, rightTreeShift);

			int leftTreeHeight = calcTreeHeight(node, leftTreeShift, leftNodeViews);
			int rightTreeHeight = calcTreeHeight(node, rightTreeShift, rightNodeViews);
			
			getRoot().setRootTreeHeights(leftTreeHeight, rightTreeHeight);
    	}
    	else{
			LinkedList childrenViews = node.getChildrenViews();

			int treeWidth = calcTreeWidth(node, childrenViews);
    		node.setTreeWidth(treeWidth); 

    		updateRelativeYOfChildren(node, childrenViews);

			int treeShift = calcTreeShift(node, childrenViews);
			node.setTreeShift(treeShift);

			int treeHeight = calcTreeHeight(node, treeShift, childrenViews);        	
    		node.setTreeHeight(treeHeight);
    		
    		System.out.println(node.getText()
    				+ ": treeShift=" + treeShift 
    				+ ": treeHeight=" + treeHeight 
					);
    	}

    }

	/**
	 * @param node
	 * @param rightNodeViews
	 * @return
	 */
	private int calcTreeShift(NodeView node, LinkedList childrenViews) {
		try{
			NodeView firstChild = (NodeView) childrenViews.getFirst();
			int childShift = firstChild.relYPos;
			int childTreeShift = firstChild.getTreeShift() + childShift;
			if (childTreeShift > 0)
				return 0;
			else
				return childTreeShift;
		}
		catch(NoSuchElementException e)
		{
			return 0;
		}
	}

	/**
	 * @param leftNodeViews
	 * @return
	 */
	private int calcTreeHeight(NodeView parent, int treeShift, LinkedList childrenViews) {
    	int parentHeight = parent.getPreferredSize().height;
        if ( childrenViews == null || childrenViews.size() == 0 ) {
           return parentHeight; 
        }
        int sumOfChildrenHeights = 0;
        int vgap = 0;
        for (ListIterator e = childrenViews.listIterator(); e.hasNext(); ) {
            NodeView node = (NodeView)e.next();
            vgap = parent.getVGap();
            if (node != null) {
               sumOfChildrenHeights += node.getTreeHeight() + vgap + node.getAdditionalCloudHeigth(); }}
        if (treeShift < 0)
        	parentHeight -= treeShift;
        NodeView firstChild = (NodeView)childrenViews.getFirst();
        if (firstChild.relYPos > 0)
        	sumOfChildrenHeights += firstChild.relYPos;
        return  Math.max(parentHeight, sumOfChildrenHeights);
  	}

	private int calcTreeWidth(NodeView parent, LinkedList childrenViews) {
		int treeWidth = 0;
		for (ListIterator e = childrenViews.listIterator(); e.hasNext(); ) {
			NodeView childNode = (NodeView)e.next();
			if (childNode != null) {
				int childWidth = childNode.getTreeWidth() + childNode.getHGap();
				if(childWidth > treeWidth){
					treeWidth = childWidth;
				}
			}
		}
		return parent.getPreferredSize().width 
		+ 2 * parent.getAdditionalCloudHeigth() 
		+ treeWidth;
	}
	
    //
    // Get Methods
    //

    private RootNodeView getRoot() {
       return (RootNodeView)map.getRoot(); }

    private MapView getMapView() {
       return map; }

    private FreeMindMain getFrame() {
       return map.getController().getFrame(); }


    // This is actually never used.
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(200,200); } //For testing Purposes

    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(getXSize(), getYSize()); }


    private int getXSize() {
        return xSize;
    }

    /**
     * @return
     */
    private int getYSize() {
        return ySize;
    }

    /**
     * @param i
     */
    private void setXSize(int i) {
        xSize = i;
    }

    /**
     * @param i
     */
    private void setYSize(int i) {
        ySize = i;
    }

	private int getShift() {
		return map.getZoomed(SHIFT);
	}


}//class MindMapLayout
